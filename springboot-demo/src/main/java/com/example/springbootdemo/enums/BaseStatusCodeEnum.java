package com.example.springbootdemo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BaseStatusCodeEnum {

    SUCCESS(1, "成功"),
    BUSINESS_ERROR(2, "业务处理失败"),
    SYSTEM_ERROR(3, "系统异常，请稍后重试"),
    VALIDATION_ERROR(4, "请求参数校验失败"),
    JSON_PARSE_ERROR(5, "请求体格式错误"),
    METHOD_NOT_SUPPORTED(6, "请求方法不支持"),
    USER_NOT_LOGGED_IN(8, "用户未登录"),
    LOGIN_EXPIRED(11, "登录已过期"),
    ACCOUNT_OR_PASSWORD_ERROR(23, "用户名或密码有误"),
    TOO_MANY_PASSWORD_ERRORS(24, "账号或密码错误次数过多，请 30 分钟后再试");

    private final int code;
    private final String message;
}
