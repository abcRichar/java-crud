package com.example.cms.common.enums;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证或认证已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "系统内部错误"),

    // Business errors (1xxx)
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    USER_DISABLED(1004, "用户已被禁用"),
    TOKEN_INVALID(1005, "Token 无效"),
    TOKEN_EXPIRED(1006, "Token 已过期"),
    REFRESH_TOKEN_INVALID(1007, "Refresh Token 无效"),
    ROLE_NOT_FOUND(1010, "角色不存在"),
    ROLE_CODE_EXISTS(1011, "角色编码已存在"),
    MENU_NOT_FOUND(1020, "菜单不存在"),
    CATEGORY_NOT_FOUND(1030, "分类不存在"),
    CATEGORY_HAS_CHILDREN(1031, "该分类下有子分类，无法删除"),
    ARTICLE_NOT_FOUND(1040, "文章不存在"),
    NOTICE_NOT_FOUND(1050, "通知不存在"),
    NOTICE_ALREADY_PUBLISHED(1051, "通知已发布");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
