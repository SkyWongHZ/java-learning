package com.example.springbootdemo.web.auth;

import com.example.springbootdemo.enums.BaseStatusCodeEnum;
import com.example.springbootdemo.enums.ClientSystemEnum;
import com.example.springbootdemo.exception.BaseException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class ClientSystemResolver {

    public static final String SYSTEM_HEADER = "system";

    public int resolve(HttpServletRequest request) {
        String value = request.getHeader(SYSTEM_HEADER);
        if (value == null || value.trim().isEmpty()) {
            return ClientSystemEnum.requireValid(null);
        }
        try {
            return ClientSystemEnum.requireValid(Integer.valueOf(value.trim()));
        } catch (NumberFormatException exception) {
            throw new BaseException(
                    BaseStatusCodeEnum.VALIDATION_ERROR,
                    "system 请求头必须是数字");
        }
    }
}
