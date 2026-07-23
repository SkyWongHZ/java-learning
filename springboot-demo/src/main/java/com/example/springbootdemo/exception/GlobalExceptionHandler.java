package com.example.springbootdemo.exception;

import com.example.springbootdemo.enums.BaseStatusCodeEnum;
import com.example.springbootdemo.model.common.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BaseException.class)
    public Response<Void> handleBaseException(BaseException exception) {
        log.warn("business exception: code={}, message={}, errorTip={}",
                exception.getCode(), exception.getUserTip(), exception.getErrorTip());
        return Response.fail(exception.getCode(), exception.getUserTip());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(BaseStatusCodeEnum.VALIDATION_ERROR.getMessage());
        return Response.fail(BaseStatusCodeEnum.VALIDATION_ERROR.getCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Response<Void> handleConstraintViolation(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse(BaseStatusCodeEnum.VALIDATION_ERROR.getMessage());
        return Response.fail(BaseStatusCodeEnum.VALIDATION_ERROR.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    public Response<Void> handleBindException(BindException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(BaseStatusCodeEnum.VALIDATION_ERROR.getMessage());
        return Response.fail(BaseStatusCodeEnum.VALIDATION_ERROR.getCode(), message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Response<Void> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return Response.fail(BaseStatusCodeEnum.VALIDATION_ERROR.getCode(), "请求参数类型不正确");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Response<Void> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        return Response.fail(BaseStatusCodeEnum.JSON_PARSE_ERROR);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Response<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return Response.fail(BaseStatusCodeEnum.METHOD_NOT_SUPPORTED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Response<Void> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        log.warn("data integrity violation", exception);
        return Response.fail(BaseStatusCodeEnum.BUSINESS_ERROR.getCode(), "数据已存在或违反数据库约束");
    }

    @ExceptionHandler(Exception.class)
    public Response<Void> handleUnexpectedException(Exception exception) {
        log.error("unexpected exception", exception);
        return Response.fail(BaseStatusCodeEnum.SYSTEM_ERROR);
    }
}
