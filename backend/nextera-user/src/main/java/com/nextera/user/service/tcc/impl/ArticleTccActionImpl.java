package com.nextera.user.service.tcc.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.api.article.dto.ArticleDTO;
import com.nextera.common.core.Result;
import com.nextera.user.client.ArticleServiceClient;
import com.nextera.user.service.tcc.ArticleTccAction;
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

import java.util.HashMap;
import java.util.Map;

/**
 * 文章TCC操作实现类
 *
 * @author Scout
 * @date 2025-06-18
 * @since 1.0
 */
@Slf4j
@Service
@LocalTCC
public class ArticleTccActionImpl implements ArticleTccAction {

    private final ArticleServiceClient articleServiceClient;
    private final TccStateManager tccStateManager;
    private final ObjectMapper objectMapper;
    private final UserTccAction userTccAction;

    private static final String ACTION_NAME = "articleUpdateTccAction";

    // 构造函数中初始化ObjectMapper
    public ArticleTccActionImpl(ArticleServiceClient articleServiceClient, TccStateManager tccStateManager, UserTccAction userTccAction) {
        this.articleServiceClient = articleServiceClient;
        this.tccStateManager = tccStateManager;
        this.userTccAction = userTccAction;
        
        // 配置ObjectMapper支持Java 8时间类型
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    @TwoPhaseBusinessAction(
            name = "articleUpdateTccAction",
            commitMethod = "commitUpdateArticle",
            rollbackMethod = "rollbackUpdateArticle"
    )
    public boolean prepareUpdateArticle(@BusinessActionContextParameter(paramName = "articleId") Long articleId, @BusinessActionContextParameter(paramName = "request") ArticleCreateRequest request) {
        log.info("文章TCC Try阶段开始: articleId={}, actionName={}", articleId, ACTION_NAME);

        try {
            // Try阶段：获取文章原始信息并执行实际的更新操作
            Result<ArticleDTO> articleResult = articleServiceClient.getArticleByIdForTcc(articleId);
            if (!articleResult.isSuccess() || articleResult.getData() == null) {
                log.error("文章不存在或获取失败: articleId={}, result={}", articleId, articleResult.getMessage());
                return false;
            }

            ArticleDTO article = articleResult.getData();
            log.info("文章TCC Try阶段检查成功: articleId={}, title={}", articleId, article.getTitle());
            
            // 保存原始文章信息用于回滚
            String businessKey = "article_" + articleId;
            
            // 保存原始文章数据
            Map<String, Object> originalData = new HashMap<>();
            originalData.put("originalTitle", article.getTitle());
            originalData.put("originalContent", article.getContent());
            originalData.put("originalCategoryId", article.getCategoryId());
            originalData.put("originalSummary", article.getSummary());
            originalData.put("originalStatus", article.getStatus());
            
            // 保存新的请求数据
            Map<String, Object> newData = new HashMap<>();
            newData.put("articleId", articleId); // 添加articleId用于状态查找
            newData.put("newTitle", request.getTitle());
            newData.put("newContent", request.getContent());
            newData.put("newCategoryId", request.getCategoryId());
            newData.put("newSummary", request.getSummary());
            newData.put("newStatus", request.getStatus());
            
            // 在Try阶段执行实际的文章更新操作
            log.info("文章TCC Try阶段：执行文章更新操作");
            Result<Boolean> updateResult = articleServiceClient.updateArticleForTcc(articleId, request, 0L, "system", "127.0.0.1", "TCC-Agent");
            
            if (!updateResult.isSuccess() || !Boolean.TRUE.equals(updateResult.getData())) {
                log.error("文章TCC Try阶段业务操作失败: articleId={}, result={}", 
                    articleId, updateResult.getMessage());
                // 关键修复：Try阶段失败时，不保存状态，确保Confirm阶段无法找到状态而拒绝执行
                log.error("文章TCC Try阶段失败，不保存状态，确保后续Confirm阶段被拒绝");
                return false;
            }
            
            log.info("文章TCC Try阶段：业务操作执行成功: articleId={}", articleId);
            
            // 关键修复：只有在业务操作成功后才保存状态
            try {
                // 转换为JSON字符串
                String originalDataJson = objectMapper.writeValueAsString(originalData);
                String businessDataJson = objectMapper.writeValueAsString(newData);
                
                // 保存状态（使用临时XID，在实际执行时会被覆盖）
                String tempXid = businessKey + "_" + System.currentTimeMillis();
                tccStateManager.saveState(tempXid, 0L, ACTION_NAME, TccActionState.TccStatus.TRIED, 
                    businessDataJson, originalDataJson);
                    
                log.info("文章TCC Try阶段：状态保存成功，articleId={}", articleId);
            } catch (Exception e) {
                log.error("保存TCC状态失败: articleId={}", articleId, e);
                // 如果状态保存失败，返回false，确保事务回滚
                log.error("文章TCC Try阶段状态保存失败，返回false确保事务回滚");
                return false;
            }
            
            log.info("文章TCC Try阶段成功: articleId={}", articleId);
            return true;

        } catch (Exception e) {
            log.error("文章TCC Try阶段失败: articleId={}", articleId, e);
            return false;
        }
    }

    @Override
    public boolean commitUpdateArticle(BusinessActionContext actionContext) {
        String xid = actionContext.getXid();
        
        log.info("文章TCC Confirm阶段开始: xid={}", xid);

        try {
            // 从BusinessActionContext获取业务参数
            Object articleIdObj = actionContext.getActionContext("articleId");
            Object requestObj = actionContext.getActionContext("request");
            
            if (articleIdObj == null) {
                log.error("文章TCC Confirm阶段无法获取articleId参数，actionContext: {}", actionContext.getActionContext());
                log.error("可用的ActionContext参数: {}", actionContext.getActionContext().keySet());
                return false;
            }
            
            Long articleId = Long.valueOf(articleIdObj.toString());
            String businessKey = "article_" + articleId;
            
            // 如果可以从ActionContext获取request参数，则使用；否则从状态管理器获取
            ArticleCreateRequest request = null;
            if (requestObj != null) {
                try {
                    if (requestObj instanceof ArticleCreateRequest) {
                        request = (ArticleCreateRequest) requestObj;
                    } else {
                        request = objectMapper.readValue(requestObj.toString(), ArticleCreateRequest.class);
                    }
                    log.info("文章TCC Confirm阶段从ActionContext获取request参数成功");
                } catch (Exception e) {
                    log.warn("文章TCC Confirm阶段从ActionContext解析request参数失败，将从状态管理器获取", e);
                }
            }
            
            log.info("文章TCC Confirm阶段获取参数: articleId={}, businessKey={}", articleId, businessKey);
            
            // 使用增强的状态查找，支持业务键回退查找
            TccActionState state = tccStateManager.getStateEnhanced(xid, ACTION_NAME, articleId);
            if (state != null && TccActionState.TccStatus.COMMITTED.equals(state.getStatus())) {
                log.info("文章TCC Confirm阶段已处理过: articleId={}", articleId);
                return true;
            }

            // 关键检查：Try阶段必须成功才能Confirm
            if (state == null) {
                log.error("!!! 严重错误：文章TCC Confirm阶段未找到Try状态，Try可能失败，拒绝Confirm: articleId={} !!!", articleId);
                log.error("这表明Try阶段失败了，或者状态未正确保存，拒绝Confirm操作");
                log.error("XID: {}, Action: {}, 当前所有状态:", xid, ACTION_NAME);
                tccStateManager.printAllStates(); // 打印调试信息
                
                // 关键修复：创建一个FAILED状态，表明这个资源应该被回滚
                // 这样可以让状态管理器知道这个事务分支失败了
                try {
                    log.error("!!! 创建FAILED状态，标记文章TCC为失败，触发回滚 !!!");
                    TccActionState failedState = new TccActionState();
                    failedState.setXid(xid);
                    failedState.setBranchId(0L);
                    failedState.setActionName(ACTION_NAME);
                    failedState.setStatus(TccActionState.TccStatus.FAILED);
                    failedState.setBusinessData("{\"articleId\":" + articleId + ",\"reason\":\"Try阶段失败\"}");
                    failedState.setOriginalData("{\"failed\":true}");
                    failedState.setRetryCount(0); // 初始化重试计数
                    failedState.setMaxRetryCount(3); // 最大重试3次
                    failedState.setCreateTime(java.time.LocalDateTime.now());
                    
                    tccStateManager.saveStateObject(failedState);
                } catch (Exception e) {
                    log.error("保存失败状态异常", e);
                }
                
                // 双保险：再次验证文章是否真的被更新了（通过检查updateTime等）
                // 注意：在TCC Confirm阶段调用Dubbo可能有Seata上下文问题，先注释掉
                try {
                    log.info("跳过文章状态检查（避免Dubbo+Seata上下文问题）");
                    // Result<ArticleDTO> currentArticleResult = articleServiceClient.getArticleByIdForTcc(articleId);
                    // if (currentArticleResult.isSuccess() && currentArticleResult.getData() != null) {
                    //     log.error("当前文章状态检查 - articleId: {}, title: {}", 
                    //         articleId, currentArticleResult.getData().getTitle());
                    // }
                } catch (Exception e) {
                    log.error("获取当前文章状态失败", e);
                }
                
                return false;
            }
            
            if (!TccActionState.TccStatus.TRIED.equals(state.getStatus())) {
                log.error("!!! 严重错误：文章TCC状态不是TRIED，当前状态: {}, 拒绝Confirm: articleId={} !!!", 
                    state.getStatus(), articleId);
                log.error("状态详情: {}", state);
                
                // 关键修复：对于FAILED状态，需要先增加重试计数，然后检查是否超限
                if (TccActionState.TccStatus.FAILED.equals(state.getStatus())) {
                    // 先增加重试计数
                    state.incrementRetryCount();
                    
                    // 检查是否已达到最大重试次数
                    if (state.getRetryCount() > state.getMaxRetryCount()) {
                        log.error("!!! 文章TCC Confirm已达到最大重试次数({})，停止重试，直接失败: articleId={} !!!", 
                            state.getMaxRetryCount(), articleId);
                        
                        // 主动触发用户TCC回滚
                        log.error("!!! 关键修复：文章TCC最终失败，主动触发用户TCC回滚 !!!");
                        triggerUserTccRollback(xid);
                        
                        // 返回true，告诉Seata这个分支已经"处理完毕"，不要再重试
                        return true;
                    } else {
                        // 保存更新后的状态对象，保持重试计数
                        tccStateManager.saveStateObject(state);
                        
                        log.warn("文章TCC Confirm重试中，当前重试次数: {}/{}, articleId={}", 
                            state.getRetryCount(), state.getMaxRetryCount(), articleId);
                        
                        // 主动触发用户TCC回滚
                        log.error("!!! 关键修复：文章TCC失败，主动触发用户TCC回滚 !!!");
                        triggerUserTccRollback(xid);
                        
                        return false;
                    }
                }
                
                // 对于其他非TRIED状态，直接拒绝
                return false;
            }
            
            // 额外验证：检查状态中的业务数据是否完整
            if (state.getBusinessData() == null || state.getBusinessData().trim().isEmpty()) {
                log.error("!!! 严重错误：文章TCC状态中的业务数据为空，拒绝Confirm: articleId={} !!!", articleId);
                return false;
            }
            
            log.info("文章TCC Confirm阶段状态验证通过: articleId={}, status={}", articleId, state.getStatus());

            // 获取请求参数，优先从ActionContext获取，其次从状态管理器获取
            request = reconstructRequestFromContext(actionContext, state);
            if (request != null) {
                log.info("文章TCC Confirm阶段成功重构request参数");
            }

            if (request == null) {
                log.error("文章TCC Confirm阶段无法获取request参数");
                return false;
            }

            // 在Confirm阶段仅进行状态确认，不执行实际业务操作
            // 按照Seata 2.3最佳实践，实际的业务操作应该在Try阶段完成
            log.info("文章TCC Confirm阶段：确认文章更新操作成功");
            
            // 更新状态为已提交
            tccStateManager.updateStatus(xid, ACTION_NAME, TccActionState.TccStatus.COMMITTED);
            log.info("文章TCC Confirm阶段：状态已确认为已提交: articleId={}", articleId);

            // 更新状态为已提交
            tccStateManager.updateStatus(xid, ACTION_NAME, TccActionState.TccStatus.COMMITTED);

            log.info("文章TCC Confirm阶段成功: articleId={}", articleId);
            return true;

        } catch (Exception e) {
            log.error("文章TCC Confirm阶段异常: xid={}", xid, e);
            return false;
        }
    }

    @Override
    public boolean rollbackUpdateArticle(BusinessActionContext actionContext) {
        String xid = actionContext.getXid();
        
        log.info("文章TCC Cancel阶段开始: xid={}", xid);

        try {
            // 从BusinessActionContext获取业务参数
            Object articleIdObj = actionContext.getActionContext("articleId");
            if (articleIdObj == null) {
                log.error("文章TCC Cancel阶段无法获取articleId参数，actionContext: {}", actionContext.getActionContext());
                // 对于Cancel阶段，即使无法获取参数也要返回成功，避免阻塞事务回滚
                log.warn("文章TCC Cancel阶段无法获取参数，但返回成功以允许事务回滚完成");
                return true;
            }
            
            Long articleId = Long.valueOf(articleIdObj.toString());
            String businessKey = "article_" + articleId;
            
            log.info("文章TCC Cancel阶段获取参数: articleId={}, businessKey={}", articleId, businessKey);
            
            // 使用增强的状态查找
            TccActionState state = tccStateManager.getStateEnhanced(xid, ACTION_NAME, articleId);
            if (state == null) {
                log.warn("文章TCC Cancel阶段未找到状态信息，可能是空回滚: articleId={}", articleId);
                tccStateManager.printAllStates(); // 打印调试信息
                return true; // 空回滚场景，返回成功
            }
            
            if (TccActionState.TccStatus.ROLLBACKED.equals(state.getStatus())) {
                log.info("文章TCC Cancel阶段已处理过: articleId={}", articleId);
                return true;
            }

            // 执行回滚操作，恢复原始数据
            log.info("文章TCC Cancel阶段：执行回滚操作，恢复原始数据: articleId={}", articleId);
            
            // 构造回滚请求，使用原始数据
            String originalDataJson = state.getOriginalData();
            if (originalDataJson != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> originalData = objectMapper.readValue(originalDataJson, Map.class);
                    
                    ArticleCreateRequest rollbackRequest = new ArticleCreateRequest();
                    rollbackRequest.setTitle((String) originalData.get("originalTitle"));
                    rollbackRequest.setContent((String) originalData.get("originalContent"));
                    Object categoryIdObj = originalData.get("originalCategoryId");
                    if (categoryIdObj instanceof Number) {
                        rollbackRequest.setCategoryId(((Number) categoryIdObj).longValue());
                    }
                    rollbackRequest.setSummary((String) originalData.get("originalSummary"));
                    rollbackRequest.setStatus(((Number) originalData.get("originalStatus")).intValue());
                    
                    // 执行回滚操作
                    Result<Boolean> rollbackResult = articleServiceClient.updateArticleForTcc(articleId, rollbackRequest, 0L, "system", "127.0.0.1", "TCC-Rollback");
                    if (!rollbackResult.isSuccess() || !Boolean.TRUE.equals(rollbackResult.getData())) {
                        log.error("文章TCC Cancel阶段回滚失败: articleId={}, result={}", 
                            articleId, rollbackResult.getMessage());
                        return false;
                    }
                    
                    log.info("文章TCC Cancel阶段回滚成功: articleId={}", articleId);
                } catch (Exception e) {
                    log.error("文章TCC Cancel阶段解析原始数据失败: articleId={}", articleId, e);
                    return false;
                }
            } else {
                log.warn("文章TCC Cancel阶段无原始数据，跳过回滚: articleId={}", articleId);
            }

            // 更新状态为已取消
            tccStateManager.updateStatus(xid, ACTION_NAME, TccActionState.TccStatus.ROLLBACKED);

            log.info("文章TCC Cancel阶段成功: articleId={}", articleId);
            return true;

        } catch (Exception e) {
            log.error("文章TCC Cancel阶段失败: xid={}", xid, e);
            return false;
        }
    }

    /**
     * 从BusinessActionContext和状态管理器重构请求参数
     */
    private ArticleCreateRequest reconstructRequestFromContext(BusinessActionContext actionContext, TccActionState state) {
        try {
            // 优先从ActionContext获取
            Object requestObj = actionContext.getActionContext("request");
            if (requestObj != null) {
                if (requestObj instanceof ArticleCreateRequest) {
                    return (ArticleCreateRequest) requestObj;
                } else {
                    // 尝试从JSON字符串反序列化
                    return objectMapper.readValue(requestObj.toString(), ArticleCreateRequest.class);
                }
            }
            
            // 其次从状态管理器获取
            if (state != null && state.getBusinessData() != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> businessData = objectMapper.readValue(state.getBusinessData(), Map.class);
                    ArticleCreateRequest request = new ArticleCreateRequest();
                    request.setTitle((String) businessData.get("newTitle"));
                    request.setContent((String) businessData.get("newContent"));
                    if (businessData.get("newCategoryId") != null) {
                        Object categoryIdObj = businessData.get("newCategoryId");
                        if (categoryIdObj instanceof Number) {
                            request.setCategoryId(((Number) categoryIdObj).longValue());
                        }
                    }
                    if (businessData.get("newSummary") != null) {
                        request.setSummary((String) businessData.get("newSummary"));
                    }
                    if (businessData.get("newStatus") != null) {
                        request.setStatus(((Number) businessData.get("newStatus")).intValue());
                    }
                    return request;
                } catch (Exception ex) {
                    log.error("从状态管理器解析业务数据失败", ex);
                }
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("重构请求参数失败", e);
            return null;
        }
    }

    /**
     * 主动触发用户TCC回滚
     * 当文章TCC失败时，确保用户TCC也被回滚以保持数据一致性
     */
    private void triggerUserTccRollback(String xid) {
        try {
            log.error("开始主动触发用户TCC回滚: xid={}", xid);
            
            // 检查用户TCC状态
            TccActionState userState = tccStateManager.getState(xid, "userLastLoginTimeTccAction");
            if (userState == null) {
                log.warn("未找到用户TCC状态，可能已回滚或未执行: xid={}", xid);
                return;
            }
            
            if (TccActionState.TccStatus.COMMITTED.equals(userState.getStatus())) {
                log.error("!!! 检测到用户TCC已提交，需要强制回滚 !!!");
                
                // 从状态中获取用户ID
                String businessData = userState.getBusinessData();
                if (businessData != null) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = objectMapper.readValue(businessData, Map.class);
                        Object userIdObj = data.get("userId");
                        if (userIdObj != null) {
                            Long userId = Long.valueOf(userIdObj.toString());
                            log.error("强制回滚用户lastLoginTime: userId={}", userId);
                            
                            // 构造BusinessActionContext进行强制回滚
                            // 注意：这是紧急修复措施，正常情况下应该由Seata处理
                            BusinessActionContext mockContext = new BusinessActionContext();
                            mockContext.setXid(xid);
                            Map<String, Object> actionContext = new HashMap<>();
                            actionContext.put("userId", userId);
                            mockContext.setActionContext(actionContext);
                            
                            // 调用用户TCC的回滚方法
                            boolean rollbackResult = userTccAction.rollbackUpdateLastLoginTime(mockContext);
                            if (rollbackResult) {
                                log.info("用户TCC强制回滚成功: userId={}", userId);
                            } else {
                                log.error("用户TCC强制回滚失败: userId={}", userId);
                            }
                        }
                    } catch (Exception e) {
                        log.error("解析用户TCC业务数据失败", e);
                    }
                }
            } else {
                log.info("用户TCC状态为: {}, 无需强制回滚", userState.getStatus());
            }
            
        } catch (Exception e) {
            log.error("主动触发用户TCC回滚失败: xid={}", xid, e);
        }
    }
} 