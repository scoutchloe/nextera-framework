package com.nextera.user.service.tcc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextera.user.config.TccRetryConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TCC状态管理器
 * 用于管理TCC状态的存储和查询，解决幂等、空回滚、悬挂问题
 *
 * @author Scout
 * @date 2025-06-18
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TccStateManager {

    private final TccRetryConfiguration retryConfiguration;

    /**
     * 内存状态存储（生产环境建议使用Redis或数据库）
     */
    private final ConcurrentHashMap<String, TccActionState> stateStorage = new ConcurrentHashMap<>();

    /**
     * 生成状态键（基于XID和ActionName）
     */
    private String generateKey(String xid, String actionName) {
        return xid + ":" + actionName;
    }

    /**
     * 保存TCC状态
     */
    public void saveState(String xid, Long branchId, String actionName, TccActionState.TccStatus status, 
                         String businessData, String originalData) {
        String key = generateKey(xid, actionName);
        TccActionState state = new TccActionState();
        state.setXid(xid);
        state.setBranchId(branchId);
        state.setActionName(actionName);
        state.setStatus(status);
        state.setBusinessData(businessData);
        state.setOriginalData(originalData);
        state.setCreateTime(LocalDateTime.now());
        
        // 设置最大重试次数
        if (retryConfiguration.isEnabled()) {
            state.setMaxRetryCount(retryConfiguration.getMaxRetryCount());
        } else {
            state.setMaxRetryCount(Integer.MAX_VALUE); // 禁用重试控制时设为无限大
        }
        
        stateStorage.put(key, state);
        log.info("保存TCC状态: key={}, xid={}, branchId={}, actionName={}, status={}, maxRetryCount={}, 当前存储大小={}", 
            key, xid, branchId, actionName, status, state.getMaxRetryCount(), stateStorage.size());
    }

    /**
     * 增加重试次数
     */
    public boolean incrementRetryCount(String xid, String actionName) {
        String key = generateKey(xid, actionName);
        TccActionState state = stateStorage.get(key);
        if (state != null) {
            state.incrementRetryCount();
            log.info("增加重试次数: xid={}, actionName={}, retryCount={}", xid, actionName, state.getRetryCount());
            return state.canRetry();
        }
        return false;
    }

    /**
     * 检查是否可以重试
     */
    public boolean canRetry(String xid, String actionName) {
        String key = generateKey(xid, actionName);
        TccActionState state = stateStorage.get(key);
        if (state != null) {
            boolean canRetry = state.canRetry();
            log.info("检查重试次数: xid={}, actionName={}, retryCount={}, canRetry={}", 
                xid, actionName, state.getRetryCount(), canRetry);
            return canRetry;
        }
        return true; // 如果状态不存在，允许重试
    }

    /**
     * 标记为最终失败
     */
    public void markAsFailed(String xid, String actionName) {
        String key = generateKey(xid, actionName);
        TccActionState state = stateStorage.get(key);
        if (state != null) {
            state.setStatus(TccActionState.TccStatus.FAILED);
            log.info("标记TCC状态为最终失败: xid={}, actionName={}, retryCount={}", 
                xid, actionName, state.getRetryCount());
        }
    }

    /**
     * 获取TCC状态
     */
    public TccActionState getState(String xid, String actionName) {
        String key = generateKey(xid, actionName);
        TccActionState state = stateStorage.get(key);
        log.debug("获取TCC状态: key={}, xid={}, actionName={}, found={}, 当前存储大小={}", 
            key, xid, actionName, state != null, stateStorage.size());
        return state;
    }

    /**
     * 更新状态
     */
    public void updateStatus(String xid, String actionName, TccActionState.TccStatus status) {
        String key = generateKey(xid, actionName);
        TccActionState state = stateStorage.get(key);
        if (state != null) {
            state.setStatus(status);
            log.info("更新TCC状态: xid={}, actionName={}, status={}", xid, actionName, status);
        }
    }

    /**
     * 检查是否已经尝试过（防止重复Try）
     */
    public boolean hasTriedBefore(String xid, String actionName) {
        TccActionState state = getState(xid, actionName);
        return state != null && state.getStatus() != TccActionState.TccStatus.INIT;
    }

    /**
     * 检查是否可以回滚（防止空回滚）
     */
    public boolean canRollback(String xid, String actionName) {
        TccActionState state = getState(xid, actionName);
        return state != null && state.getStatus() == TccActionState.TccStatus.TRIED;
    }

    /**
     * 检查是否可以提交（防止悬挂）
     */
    public boolean canCommit(String xid, String actionName) {
        TccActionState state = getState(xid, actionName);
        boolean canCommit = state != null && state.getStatus() == TccActionState.TccStatus.TRIED;
        log.info("检查是否可以提交: xid={}, actionName={}, stateExists={}, status={}, canCommit={}", 
            xid, actionName, state != null, state != null ? state.getStatus() : null, canCommit);
        return canCommit;
    }

    /**
     * 检查是否已经处理过（防止重复处理）
     */
    public boolean isAlreadyProcessed(String xid, String actionName, TccActionState.TccStatus targetStatus) {
        TccActionState state = getState(xid, actionName);
        return state != null && state.getStatus() == targetStatus;
    }

    /**
     * 清理状态（可选，用于清理过期状态）
     */
    public void cleanupState(String xid, String actionName) {
        String key = generateKey(xid, actionName);
        stateStorage.remove(key);
        log.info("清理TCC状态: xid={}, actionName={}", xid, actionName);
    }

    /**
     * 调试方法：打印所有状态
     */
    public void printAllStates() {
        log.info("=== TCC状态管理器调试信息 ===");
        log.info("当前存储的状态总数: {}", stateStorage.size());
        for (Map.Entry<String, TccActionState> entry : stateStorage.entrySet()) {
            TccActionState state = entry.getValue();
            log.info("Key: {}, XID: {}, ActionName: {}, Status: {}, CreateTime: {}", 
                entry.getKey(), state.getXid(), state.getActionName(), state.getStatus(), state.getCreateTime());
        }
        log.info("=== 调试信息结束 ===");
    }

    /**
     * 基于业务数据查找状态（用于解决Try阶段临时键与Confirm阶段真实XID不匹配的问题）
     * 
     * @param actionName 动作名称
     * @param businessId 业务ID（可以是userId或articleId）
     * @param realXid 真实的XID
     * @return TCC状态
     */
    public TccActionState findStateByBusinessKey(String actionName, Long businessId, String realXid) {
        log.info("尝试通过业务键查找TCC状态: actionName={}, businessId={}, realXid={}", actionName, businessId, realXid);
        
        // 先尝试用真实XID查找
        TccActionState state = getState(realXid, actionName);
        if (state != null) {
            log.info("通过真实XID找到TCC状态: {}", state.getStatus());
            return state;
        }
        
        // 如果找不到，则遍历所有状态，寻找匹配的业务数据
        for (Map.Entry<String, TccActionState> entry : stateStorage.entrySet()) {
            TccActionState candidateState = entry.getValue();
            if (actionName.equals(candidateState.getActionName())) {
                try {
                    // 解析业务数据，检查是否包含相同的businessId
                    if (candidateState.getBusinessData() != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> businessData = new ObjectMapper().readValue(candidateState.getBusinessData(), Map.class);
                        
                        // 检查userId或其他业务标识
                        Object userIdObj = businessData.get("userId");
                        Object articleIdObj = businessData.get("articleId");
                        
                        boolean matched = false;
                        if (userIdObj != null && businessId.equals(Long.valueOf(userIdObj.toString()))) {
                            matched = true;
                        } else if (articleIdObj != null && businessId.equals(Long.valueOf(articleIdObj.toString()))) {
                            matched = true;
                        }
                        
                        if (matched) {
                            log.info("通过业务键找到匹配的TCC状态，准备迁移到真实XID: oldKey={}, newXid={}", 
                                entry.getKey(), realXid);
                            
                            // 迁移状态到真实XID
                            candidateState.setXid(realXid);
                            String newKey = generateKey(realXid, actionName);
                            stateStorage.put(newKey, candidateState);
                            
                            // 删除旧的临时键（如果不是同一个键）
                            if (!newKey.equals(entry.getKey())) {
                                stateStorage.remove(entry.getKey());
                                log.info("TCC状态迁移完成: {} -> {}", entry.getKey(), newKey);
                            }
                            
                            return candidateState;
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析业务数据失败: key={}", entry.getKey(), e);
                }
            }
        }
        
        log.warn("未找到匹配的TCC状态: actionName={}, businessId={}", actionName, businessId);
        return null;
    }

    /**
     * 获取TCC状态（增强版，支持业务键回退查找）
     */
    public TccActionState getStateEnhanced(String xid, String actionName, Long businessId) {
        // 首先尝试标准查找
        TccActionState state = getState(xid, actionName);
        if (state != null) {
            return state;
        }
        
        // 如果没找到且提供了businessId，则尝试业务键查找
        if (businessId != null) {
            return findStateByBusinessKey(actionName, businessId, xid);
        }
        
        return null;
    }

    /**
     * 通过业务键删除TCC状态（用于Try阶段失败时的清理）
     */
    public void removeStateByBusinessKey(String actionName, Long businessId) {
        log.info("通过业务键删除TCC状态: actionName={}, businessId={}", actionName, businessId);
        
        String keyToRemove = null;
        for (Map.Entry<String, TccActionState> entry : stateStorage.entrySet()) {
            String key = entry.getKey();
            TccActionState state = entry.getValue();
            
            if (actionName.equals(state.getActionName())) {
                try {
                    // 解析业务数据，检查是否包含相同的businessId
                    if (state.getBusinessData() != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> businessData = new ObjectMapper().readValue(state.getBusinessData(), Map.class);
                        
                        // 检查userId或articleId
                        Object userIdObj = businessData.get("userId");
                        Object articleIdObj = businessData.get("articleId");
                        
                        boolean matched = false;
                        if (userIdObj != null && businessId.equals(Long.valueOf(userIdObj.toString()))) {
                            matched = true;
                        } else if (articleIdObj != null && businessId.equals(Long.valueOf(articleIdObj.toString()))) {
                            matched = true;
                        }
                        
                        if (matched) {
                            keyToRemove = key;
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析业务数据失败，跳过: key={}", key, e);
                }
            }
        }
        
        if (keyToRemove != null) {
            stateStorage.remove(keyToRemove);
            log.info("已删除TCC状态: {}", keyToRemove);
        } else {
            log.warn("未找到要删除的TCC状态: actionName={}, businessId={}", actionName, businessId);
        }
    }

    /**
     * 保存完整的TCC状态对象
     */
    public void saveStateObject(TccActionState state) {
        try {
            String key = generateKey(state.getXid(), state.getActionName());
            stateStorage.put(key, state);
            log.info("保存TCC状态对象成功: key={}, status={}, retryCount={}/{}, 当前存储大小={}", 
                key, state.getStatus(), state.getRetryCount(), state.getMaxRetryCount(), stateStorage.size());
        } catch (Exception e) {
            log.error("保存TCC状态对象失败: xid={}, actionName={}", 
                state.getXid(), state.getActionName(), e);
        }
    }
} 