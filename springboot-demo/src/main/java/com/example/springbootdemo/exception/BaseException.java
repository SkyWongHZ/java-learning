package com.example.springbootdemo.exception;

import com.example.springbootdemo.enums.BaseStatusCodeEnum;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final int code;
    private final String userTip;
    private final String errorTip;

    public BaseException(BaseStatusCodeEnum status) {
        this(status.getCode(), status.getMessage(), status.getMessage());
    }

    public BaseException(BaseStatusCodeEnum status, String userTip) {
        this(status.getCode(), userTip, userTip);
    }

    public BaseException(int code, String userTip) {
        this(code, userTip, userTip);
    }

    public BaseException(int code, String userTip, String errorTip) {
        super(userTip);
        this.code = code;
        this.userTip = userTip;
        this.errorTip = errorTip;
    }
}
