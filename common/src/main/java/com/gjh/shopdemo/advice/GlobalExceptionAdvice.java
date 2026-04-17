package com.gjh.shopdemo.advice;

import com.gjh.shopdemo.pojo.exception.BaseException;
import com.gjh.shopdemo.pojo.result.ShopResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionAdvice{

    @ExceptionHandler(Exception.class)
    public ShopResult handleException(Exception e){
        log.error("捕获系统异常:{}",e.getMessage());
        return ShopResult.fail("系统异常,请稍后再试");
    }

    @ExceptionHandler(BaseException.class)
    public ShopResult handleBaseException(BaseException e){
        log.error("捕获业务异常:{}",e.getMessage());
        return ShopResult.fail(e.getMessage());
    }

    /**
     * 处理参数校验异常
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ShopResult<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验异常:{}", msg);
        return ShopResult.fail(400, msg);
    }

    @ExceptionHandler(BindException.class)
    public ShopResult<Void> handleBindException(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验异常:{}", msg);
        return ShopResult.fail(400, msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ShopResult<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验异常:{}", msg);
        return ShopResult.fail(msg);
    }
}
