package com.nextera.user.service.tcc;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

/**
 * 用户TCC操作接口
 * 用于处理用户lastLoginTime更新的TCC事务
 *
 * @author Scout
 * @date 2025-06-18
 * @since 1.0
 */
@LocalTCC
public interface UserTccAction {

    /**
     * Try阶段：检查用户是否存在，预留资源
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    @TwoPhaseBusinessAction(
            name = "userLastLoginTimeTccAction",
            commitMethod = "commitUpdateLastLoginTime",
            rollbackMethod = "rollbackUpdateLastLoginTime"
    )
    boolean prepareUpdateLastLoginTime(
            @BusinessActionContextParameter(paramName = "userId") Long userId
    );

    /**
     * Confirm阶段：执行用户lastLoginTime更新
     *
     * @param actionContext 业务上下文
     * @return 是否成功
     */
    boolean commitUpdateLastLoginTime(BusinessActionContext actionContext);

    /**
     * Cancel阶段：回滚用户lastLoginTime更新
     *
     * @param actionContext 业务上下文
     * @return 是否成功
     */
    boolean rollbackUpdateLastLoginTime(BusinessActionContext actionContext);
} 