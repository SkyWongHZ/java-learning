package com.example.springbootdemo.model.common;

import com.example.springbootdemo.enums.BaseStatusCodeEnum;
import com.example.springbootdemo.web.TraceConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {

    private int code;
    private String msg;
    private String errorDetail;
    private T data;
    private String tid;

    public static <T> Response<T> success(T data) {
        return new Response<>(
                BaseStatusCodeEnum.SUCCESS.getCode(),
                BaseStatusCodeEnum.SUCCESS.getMessage(),
                null,
                data,
                MDC.get(TraceConstants.TRACE_ID_KEY));
    }

    public static <T> Response<T> fail(BaseStatusCodeEnum status) {
        return fail(status.getCode(), status.getMessage());
    }

    public static <T> Response<T> fail(int code, String message) {
        return new Response<>(code, message, null, null, MDC.get(TraceConstants.TRACE_ID_KEY));
    }
}
