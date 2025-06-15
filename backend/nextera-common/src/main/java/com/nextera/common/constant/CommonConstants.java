package com.nextera.common.constant;

/**
 * 通用常量
 *
 * @author Nextera
 */
public interface CommonConstants {

    /**
     * 编码
     */
    String UTF8 = "UTF-8";

    /**
     * JSON类型
     */
    String CONTENT_TYPE_JSON = "application/json";

    /**
     * 成功标识
     */
    String SUCCESS = "success";

    /**
     * 失败标识
     */
    String FAIL = "fail";

    /**
     * 登录成功
     */
    String LOGIN_SUCCESS = "登录成功";

    /**
     * 注销成功
     */
    String LOGOUT_SUCCESS = "注销成功";

    /**
     * 注册成功
     */
    String REGISTER_SUCCESS = "注册成功";

    /**
     * 操作成功
     */
    String OPERATION_SUCCESS = "操作成功";

    /**
     * 标志
     */
    String FROM = "from";

    /**
     * 请求开始时间
     */
    String REQUEST_START_TIME = "REQUEST-START-TIME";

    /**
     * 删除标志
     */
    interface DelFlag {
        /** 正常 */
        String NORMAL = "0";
        /** 删除 */
        String DELETE = "1";
    }

    /**
     * 用户状态
     */
    interface UserStatus {
        /** 正常 */
        String NORMAL = "0";
        /** 停用 */
        String DISABLE = "1";
        /** 删除 */
        String DELETE = "2";
    }

    /**
     * 菜单类型
     */
    interface MenuType {
        /** 目录 */
        String DIR = "M";
        /** 菜单 */
        String MENU = "C";
        /** 按钮 */
        String BUTTON = "F";
    }

    /**
     * 是否状态
     */
    interface YesNo {
        /** 是 */
        String YES = "Y";
        /** 否 */
        String NO = "N";
    }

    /**
     * 性别
     */
    interface Gender {
        /** 男 */
        String MALE = "1";
        /** 女 */
        String FEMALE = "2";
        /** 未知 */
        String UNKNOWN = "0";
    }

    /**
     * 审核状态
     */
    interface AuditStatus {
        /** 待审核 */
        String PENDING = "0";
        /** 审核通过 */
        String APPROVED = "1";
        /** 审核拒绝 */
        String REJECTED = "2";
    }

    /**
     * 发布状态
     */
    interface PublishStatus {
        /** 草稿 */
        String DRAFT = "0";
        /** 已发布 */
        String PUBLISHED = "1";
        /** 已下线 */
        String OFFLINE = "2";
    }

    /**
     * 数据权限类型
     */
    interface DataScope {
        /** 全部数据权限 */
        String DATA_SCOPE_ALL = "1";
        /** 自定义数据权限 */
        String DATA_SCOPE_CUSTOM = "2";
        /** 部门数据权限 */
        String DATA_SCOPE_DEPT = "3";
        /** 部门及以下数据权限 */
        String DATA_SCOPE_DEPT_AND_CHILD = "4";
        /** 仅本人数据权限 */
        String DATA_SCOPE_SELF = "5";
    }

    /**
     * 通用状态码
     */
    interface Status {
        /** 启用 */
        String ENABLE = "1";
        /** 禁用 */
        String DISABLE = "0";
    }

    /**
     * 缓存Key前缀
     */
    interface CachePrefix {
        /** 用户信息缓存 */
        String USER_INFO = "nextera:user:info:";
        /** 用户权限缓存 */
        String USER_PERMISSION = "nextera:user:permission:";
        /** 验证码缓存 */
        String CAPTCHA = "nextera:captcha:";
        /** 登录Token缓存 */
        String LOGIN_TOKEN = "nextera:login:token:";
        /** 刷新Token缓存 */
        String REFRESH_TOKEN = "nextera:refresh:token:";
        /** 文章缓存 */
        String ARTICLE = "nextera:article:";
        /** 分类缓存 */
        String CATEGORY = "nextera:category:";
    }

    /**
     * HTTP Header
     */
    interface Header {
        /** 认证头 */
        String AUTHORIZATION = "Authorization";
        /** Token前缀 */
        String BEARER_PREFIX = "Bearer ";
        /** 用户代理 */
        String USER_AGENT = "User-Agent";
        /** 请求ID */
        String REQUEST_ID = "X-Request-ID";
        /** 租户ID */
        String TENANT_ID = "X-Tenant-ID";
    }
} 