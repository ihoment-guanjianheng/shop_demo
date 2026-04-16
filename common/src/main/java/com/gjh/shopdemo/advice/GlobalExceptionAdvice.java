package com.gjh.shopdemo.advice;

import com.gjh.shopdemo.pojo.exception.BaseException;
import com.gjh.shopdemo.pojo.result.ShopResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
