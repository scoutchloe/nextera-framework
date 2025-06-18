package com.nextera.user.service.tcc;

import com.nextera.api.article.dto.ArticleCreateRequest;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

/**
 * 文章TCC操作接口
 * 用于处理文章更新的TCC事务
 *
 * @author Scout
 * @date 2025-06-18
 * @since 1.0
 */
@LocalTCC
public interface ArticleTccAction {

    /**
     * Try阶段：检查文章是否存在，预留资源
     *
     * @param articleId 文章ID
     * @param request 更新请求
     * @return 是否成功
     */
    @TwoPhaseBusinessAction(
            name = "articleUpdateTccAction",
            commitMethod = "commitUpdateArticle",
            rollbackMethod = "rollbackUpdateArticle"
    )
    boolean prepareUpdateArticle(
            @BusinessActionContextParameter(paramName = "articleId") Long articleId,
            @BusinessActionContextParameter(paramName = "request") ArticleCreateRequest request
    );

    /**
     * Confirm阶段：执行文章更新
     *
     * @param actionContext 业务上下文
     * @return 是否成功
     */
    boolean commitUpdateArticle(BusinessActionContext actionContext);

    /**
     * Cancel阶段：回滚文章更新
     *
     * @param actionContext 业务上下文
     * @return 是否成功
     */
    boolean rollbackUpdateArticle(BusinessActionContext actionContext);
} 