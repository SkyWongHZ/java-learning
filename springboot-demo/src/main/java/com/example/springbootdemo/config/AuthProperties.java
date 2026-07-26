package com.example.springbootdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    @Min(value = 1, message = "Token 有效天数必须大于 0")
    private int tokenTtlDays = 30;

    @Min(value = 2, message = "登录最大失败次数必须大于 1")
    private int maxFailedAttempts = 5;

    @Min(value = 1, message = "登录失败统计窗口必须大于 0")
    private int failureWindowMinutes = 5;

    @Min(value = 1, message = "账号锁定时间必须大于 0")
    private int lockMinutes = 30;

    private String bootstrapUsername = "";
    private String bootstrapPassword = "";
    private String bootstrapDisplayName = "Administrator";
}
