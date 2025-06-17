package com.nextera.common.core;

import lombok.Getter;

/**
 * 统一状态码枚举
 *
 * @author Nextera
 */
@Getter
public enum ResultCode {

    // 通用状态码
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    OPERATION_ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // 业务状态码
    USER_NOT_FOUND(10001, "用户不存在"),
    USER_DISABLED(10002, "用户已被禁用"),
    USERNAME_EXISTS(10003, "用户名已存在"),
    EMAIL_EXISTS(10004, "邮箱已存在"),
    PHONE_EXISTS(10005, "手机号已存在"),
    OLD_PASSWORD_ERROR(10006, "原密码错误"),
    PASSWORD_NOT_MATCH(10007, "两次密码不一致"),

    // 认证相关状态码
    LOGIN_FAILED(20001, "登录失败"),
    LOGIN_AUTH_FAILED(20000, "用户名或者密码错误,登录失败"),
    TOKEN_INVALID(20002, "Token无效"),
    TOKEN_EXPIRED(20003, "Token已过期"),
    REFRESH_TOKEN_INVALID(20004, "刷新Token无效"),
    CAPTCHA_ERROR(20005, "验证码错误"),
    CAPTCHA_EXPIRED(20006, "验证码已过期"),

    // 文章相关状态码
    ARTICLE_NOT_FOUND(30001, "文章不存在"),
    ARTICLE_DISABLED(30002, "文章已被禁用"),
    CATEGORY_NOT_FOUND(30003, "分类不存在"),
    CATEGORY_HAS_ARTICLES(30004, "分类下存在文章，无法删除"),

    // 系统相关状态码
    SYSTEM_ERROR(50001, "系统错误"),
    DATABASE_ERROR(50002, "数据库错误"),
    REDIS_ERROR(50003, "Redis错误"),
    FILE_UPLOAD_ERROR(50004, "文件上传失败"),
    FILE_DELETE_ERROR(50005, "文件删除失败"),
    ENCRYPT_ERROR(50006, "加密失败"),
    DECRYPT_ERROR(50007, "解密失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
} 