package com.nextera.user.service.tcc.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nextera.user.entity.User;
import com.nextera.user.mapper.UserMapper;
import com.nextera.user.service.LocalUserService;
import com.nextera.user.service.tcc.TccActionState;
import com.nextera.user.service.tcc.TccStateManager;
import com.nextera.user.service.tcc.UserTccAction;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户TCC操作实现类
 *
 * @author Scout
 * @date 2025-06-18
 * @since 1.0
 */
@Slf4j
@Service
@LocalTCC
public class UserTccActionImpl implements UserTccAction {

    private final LocalUserService userService;
    private final UserMapper userMapper;
    private final TccStateManager tccStateManager;
    
    // 配置ObjectMapper支持Java 8时间类型
    private final ObjectMapper objectMapper;
    
    private static final String ACTION_NAME = "userLastLoginTimeTccAction";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 构造函数中初始化ObjectMapper
    public UserTccActionImpl(LocalUserService userService, UserMapper userMapper, TccStateManager tccStateManager) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.tccStateManager = tccStateManager;
        
        // 配置ObjectMapper支持Java 8时间类型
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    @TwoPhaseBusinessAction(
            name = "userLastLoginTimeTccAction",
            commitMethod = "commitUpdateLastLoginTime",
            rollbackMethod = "rollbackUpdateLastLoginTime"
    )
    public boolean prepareUpdateLastLoginTime(@BusinessActionContextParameter(paramName = "userId") Long userId) {
        log.info("用户TCC Try阶段开始: userId={}, actionName={}", userId, ACTION_NAME);

        try {
            // 1. 获取当前用户信息，保存原始状态
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.error("用户不存在: userId={}", userId);
                return false;
            }

            LocalDateTime originalLastLoginTime = user.getLastLoginTime();
            LocalDateTime newLastLoginTime = LocalDateTime.now();
            
            // 2. 保存原始状态到状态管理器
            Map<String, Object> originalData = new HashMap<>();
            originalData.put("originalLastLoginTime", originalLastLoginTime);
            originalData.put("userId", userId);
            
            Map<String, Object> businessData = new HashMap<>();
            businessData.put("newLastLoginTime", newLastLoginTime);
            businessData.put("userId", userId);
            
            // 注意：这里无法获取到真实的XID，需要在Try执行过程中通过其他方式保存
            // 先使用临时标识，后续在Confirm/Cancel中会用真实XID更新
            String tempKey = "user_tcc_" + userId + "_" + System.currentTimeMillis();
            tccStateManager.saveState(tempKey, 0L, ACTION_NAME, TccActionState.TccStatus.TRIED,
                    objectMapper.writeValueAsString(businessData),
                    objectMapper.writeValueAsString(originalData));

            log.info("用户TCC Try阶段保存状态成功: userId={}, originalTime={}, newTime={}", 
                    userId, originalLastLoginTime, newLastLoginTime);

            // 3. 执行Try阶段的业务操作（实际更新lastLoginTime）
            boolean result = userService.updateLastLoginTime(userId);
            if (!result) {
                log.error("用户TCC Try阶段业务操作失败: userId={}", userId);
                log.error("!!! 关键修复：用户业务操作失败，清理已保存的TCC状态 !!!");
                
                // 清理已保存的状态，避免后续Confirm找到状态
                try {
                    tccStateManager.removeStateByBusinessKey(ACTION_NAME, userId);
                    log.info("用户TCC Try失败，已清理状态: userId={}", userId);
                } catch (Exception e) {
                    log.error("清理用户TCC状态失败", e);
                }
                
                return false;
            }

            log.info("用户TCC Try阶段成功: userId={}", userId);
            return true;

        } catch (Exception e) {
            log.error("用户TCC Try阶段失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    public boolean commitUpdateLastLoginTime(BusinessActionContext actionContext) {
        String xid = actionContext.getXid();
        
        log.info("用户TCC Confirm阶段开始: xid={}, actionName={}", xid, ACTION_NAME);

        try {
            // 从BusinessActionContext获取业务参数
            Object userIdObj = actionContext.getActionContext("userId");
            if (userIdObj == null) {
                log.error("用户TCC Confirm阶段无法获取userId参数，actionContext: {}", actionContext.getActionContext());
                log.error("!!! 关键错误：无法获取业务参数，Confirm应该失败 !!!");
                return false;
            }
            
            Long userId = Long.valueOf(userIdObj.toString());
            
            // 使用增强的状态查找，支持业务键回退查找
            TccActionState state = tccStateManager.getStateEnhanced(xid, ACTION_NAME, userId);
            if (state != null && TccActionState.TccStatus.COMMITTED.equals(state.getStatus())) {
                log.info("用户TCC Confirm阶段已处理过: userId={}", userId);
                return true;
            }
            
            // 关键检查：Try阶段必须成功才能Confirm
            if (state == null) {
                log.error("!!! 严重错误：用户TCC Confirm阶段未找到Try状态，Try可能失败，拒绝Confirm: userId={} !!!", userId);
                log.error("这表明Try阶段失败了，但Seata仍然调用了Confirm，这是不正确的行为");
                tccStateManager.printAllStates(); // 打印调试信息
                return false;
            }
            
            if (!TccActionState.TccStatus.TRIED.equals(state.getStatus())) {
                log.error("!!! 严重错误：用户TCC状态不是TRIED，当前状态: {}, 拒绝Confirm: userId={} !!!", 
                    state.getStatus(), userId);
                
                // 关键修复：如果状态是ROLLBACKED，但Seata仍在尝试Confirm，需要特殊处理
                if (TccActionState.TccStatus.ROLLBACKED.equals(state.getStatus())) {
                    log.warn("!!! 检测到TCC状态为ROLLBACKED，但Seata仍在尝试Confirm，检查是否为状态不一致问题 !!!");
                    
                    // 检查业务数据是否实际已经成功更新
                    try {
                        // 从数据库重新查询用户信息，检查lastLoginTime是否已更新
                        User currentUser = userMapper.selectById(userId);
                        if (currentUser != null && currentUser.getLastLoginTime() != null) {
                            // 解析原始数据，检查是否真的需要回滚
                            String originalData = state.getOriginalData();
                            if (originalData != null) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> originalMap = objectMapper.readValue(originalData, Map.class);
                                Object originalTimeObj = originalMap.get("originalLastLoginTime");
                                
                                // 如果当前时间不等于原始时间，说明已经更新了
                                if (originalTimeObj != null) {
                                    LocalDateTime originalTime = null;
                                    if (originalTimeObj instanceof String) {
                                        originalTime = LocalDateTime.parse((String) originalTimeObj);
                                    }
                                    
                                    // 如果当前时间比原始时间新，说明业务实际成功了
                                    if (originalTime == null || currentUser.getLastLoginTime().isAfter(originalTime)) {
                                        log.warn("!!! 检测到业务数据已实际更新，但TCC状态为ROLLBACKED，这是状态不一致问题 !!!");
                                        log.warn("原始lastLoginTime: {}, 当前lastLoginTime: {}", originalTime, currentUser.getLastLoginTime());
                                        
                                        // 修正状态为COMMITTED，避免无限重试
                                        tccStateManager.updateStatus(xid, ACTION_NAME, TccActionState.TccStatus.COMMITTED);
                                        log.warn("!!! 已修正TCC状态为COMMITTED，避免无限重试 !!!");
                                        
                                        return true; // 返回true，表示提交成功
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("检查业务数据状态时异常", e);
                    }
                    
                    // 如果检查失败或确实需要回滚，则应用重试逻辑
                    log.warn("!!! 确认需要处理ROLLBACKED状态，应用重试限制逻辑 !!!");
                    
                    // 先增加重试计数
                    state.incrementRetryCount();
                    
                    if (state.getRetryCount() > state.getMaxRetryCount()) {
                        log.error("!!! 用户TCC ROLLBACKED状态处理已达到最大重试次数({})，停止重试: userId={} !!!", 
                            state.getMaxRetryCount(), userId);
                        
                        // 返回true，告诉Seata停止重试
                        return true;
                    } else {
                        // 保存更新后的状态对象，保持重试计数
                        tccStateManager.saveStateObject(state);
                        
                        log.warn("用户TCC ROLLBACKED状态处理重试中，当前重试次数: {}/{}, userId={}", 
                            state.getRetryCount(), state.getMaxRetryCount(), userId);
                        
                        return false;
                    }
                }
                
                return false;
            }
            
            // 新增：检查同一事务中的其他TCC状态，如果有失败的，拒绝Confirm
            try {
                TccActionState articleState = tccStateManager.getState(xid, "articleUpdateTccAction");
                if (articleState != null && TccActionState.TccStatus.FAILED.equals(articleState.getStatus())) {
                    log.error("!!! 关键修复：检测到文章TCC失败，用户TCC拒绝Confirm，触发回滚: userId={} !!!", userId);
                    log.error("文章TCC状态: {}, 用户TCC必须回滚以保持数据一致性", articleState.getStatus());
                    
                    // 检查重试次数，避免无限重试
                    // 先增加重试计数
                    state.incrementRetryCount();
                    
                    if (state.getRetryCount() > state.getMaxRetryCount()) {
                        log.error("!!! 用户TCC Confirm已达到最大重试次数({})，停止重试，强制回滚: userId={} !!!", 
                            state.getMaxRetryCount(), userId);
                        
                        // 直接调用回滚逻辑
                        return rollbackUpdateLastLoginTime(actionContext);
                    } else {
                        // 保存更新后的状态对象，保持重试计数
                        tccStateManager.saveStateObject(state);
                        
                        log.warn("用户TCC Confirm重试中，当前重试次数: {}/{}, userId={}", 
                            state.getRetryCount(), state.getMaxRetryCount(), userId);
                        
                        return false;
                    }
                }
            } catch (Exception e) {
                log.warn("检查文章TCC状态时异常，继续执行", e);
            }
            
            // 在Confirm阶段，业务操作已经在Try阶段完成，这里只需要确认状态
            log.info("用户TCC Confirm阶段：确认用户lastLoginTime更新操作成功: userId={}", userId);
            
            // 更新状态为已提交
            tccStateManager.updateStatus(xid, ACTION_NAME, TccActionState.TccStatus.COMMITTED);

            log.info("用户TCC Confirm阶段成功: userId={}", userId);
            return true;

        } catch (Exception e) {
            log.error("用户TCC Confirm阶段异常: xid={}", xid, e);
            return false;
        }
    }

    @Override
    public boolean rollbackUpdateLastLoginTime(BusinessActionContext actionContext) {
        String xid = actionContext.getXid();
        
        log.info("用户TCC Cancel阶段开始: xid={}", xid);

        try {
            // 从BusinessActionContext获取业务参数
            Object userIdObj = actionContext.getActionContext("userId");
            if (userIdObj == null) {
                log.warn("用户TCC Cancel阶段无法获取userId参数，但返回成功以允许事务回滚完成");
                return true;
            }
            
            Long userId = Long.valueOf(userIdObj.toString());
            
            // 使用增强的状态查找
            TccActionState state = tccStateManager.getStateEnhanced(xid, ACTION_NAME, userId);
            if (state == null) {
                log.warn("用户TCC Cancel阶段未找到状态信息，可能是空回滚: userId={}", userId);
                tccStateManager.printAllStates(); // 打印调试信息
                
                // 关键修复：即使没有找到状态，也要尝试回滚lastLoginTime
                // 因为可能在Try阶段成功了，但状态管理有问题
                log.warn("!!! 关键修复：即使未找到状态，也尝试回滚用户lastLoginTime以确保数据一致性 !!!");
                try {
                    // 获取当前用户信息，如果lastLoginTime是最近更新的，则回滚
                    User currentUser = userMapper.selectById(userId);
                    if (currentUser != null && currentUser.getLastLoginTime() != null) {
                        // 简单策略：将lastLoginTime设置为1小时前，作为回滚
                        LocalDateTime rollbackTime = LocalDateTime.now().minusHours(1);
                        boolean rollbackResult = userService.restoreLastLoginTime(userId, rollbackTime);
                        log.warn("空回滚场景下的强制回滚结果: userId={}, result={}", userId, rollbackResult);
                    }
                } catch (Exception e) {
                    log.error("空回滚场景下的强制回滚失败", e);
                }
                
                return true; // 空回滚场景，返回成功
            }
            
            if (TccActionState.TccStatus.ROLLBACKED.equals(state.getStatus())) {
                log.info("用户TCC Cancel阶段已处理过: userId={}", userId);
                return true;
            }
            
            // 新增：检查是否是因为其他TCC失败导致的回滚
            if (TccActionState.TccStatus.COMMITTED.equals(state.getStatus())) {
                log.error("!!! 关键修复：用户TCC已经COMMITTED，但被要求Cancel，这表明有其他TCC失败 !!!");
                log.error("强制执行回滚操作以保持数据一致性: userId={}", userId);
                // 继续执行回滚逻辑，不要返回
            }

            // 执行回滚操作，恢复原始的lastLoginTime
            String originalDataJson = state.getOriginalData();
            if (originalDataJson != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> originalData = objectMapper.readValue(originalDataJson, Map.class);
                    
                    Object originalTimeObj = originalData.get("originalLastLoginTime");
                    LocalDateTime originalLastLoginTime = null;
                    
                    if (originalTimeObj != null) {
                        // 处理时间反序列化
                        if (originalTimeObj instanceof String) {
                            originalLastLoginTime = LocalDateTime.parse((String) originalTimeObj);
                        } else if (originalTimeObj instanceof LocalDateTime) {
                            originalLastLoginTime = (LocalDateTime) originalTimeObj;
                        }
                    }
                    
                    // 执行回滚操作
                    boolean rollbackResult = userService.restoreLastLoginTime(userId, originalLastLoginTime);
                    if (!rollbackResult) {
                        log.error("用户TCC Cancel阶段回滚失败: userId={}, originalTime={}", 
                                userId, originalLastLoginTime);
                        return false;
                    }
                    
                    log.info("用户TCC Cancel阶段回滚成功: userId={}, 恢复时间: {}", 
                            userId, originalLastLoginTime);
                            
                } catch (Exception e) {
                    log.error("用户TCC Cancel阶段解析原始数据失败: userId={}", userId, e);
                    return false;
                }
            } else {
                log.warn("用户TCC Cancel阶段无原始数据，跳过回滚: userId={}", userId);
            }

            // 更新状态为已取消
            tccStateManager.updateStatus(xid, ACTION_NAME, TccActionState.TccStatus.ROLLBACKED);

            log.info("用户TCC Cancel阶段成功: userId={}", userId);
            return true;

        } catch (Exception e) {
            log.error("用户TCC Cancel阶段失败: xid={}", xid, e);
            return false;
        }
    }
} 