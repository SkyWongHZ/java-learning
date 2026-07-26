package com.example.springbootdemo.util;

import com.example.springbootdemo.enums.BaseStatusCodeEnum;
import com.example.springbootdemo.exception.BaseException;

import java.util.Locale;
import java.util.regex.Pattern;

public final class BusinessInputUtils {

    private static final Pattern MAINLAND_PHONE = Pattern.compile("^1[3-9]\\d{9}$");

    private BusinessInputUtils() {
    }

    public static String normalizeCode(
            String value,
            String fieldName,
            int minLength,
            int maxLength) {
        return normalizeRequired(value, fieldName, minLength, maxLength)
                .toUpperCase(Locale.ROOT);
    }

    public static String normalizeRequired(
            String value,
            String fieldName,
            int minLength,
            int maxLength) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw validation(fieldName + "不能为空");
        }
        if (normalized.length() < minLength || normalized.length() > maxLength) {
            throw validation(fieldName + "长度必须为 " + minLength + " 到 " + maxLength + " 个字符");
        }
        return normalized;
    }

    public static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public static String normalizePhone(String value) {
        String normalized = normalizeOptional(value);
        if (normalized != null && !MAINLAND_PHONE.matcher(normalized).matches()) {
            throw validation("手机号格式不正确");
        }
        return normalized;
    }

    private static BaseException validation(String message) {
        return new BaseException(BaseStatusCodeEnum.VALIDATION_ERROR, message);
    }
}
