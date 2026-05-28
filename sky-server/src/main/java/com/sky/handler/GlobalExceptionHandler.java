package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.warn("业务异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
    @ExceptionHandler
    public Result exceptionHandler(NoResourceFoundException ex){
        return Result.error(MessageConstant.ServerError.RESOURCE_NOT_FOUND);
    }
    @ExceptionHandler
    public Result exceptionHandler(Exception ex){
        log.error("系统异常：{}", ex.getMessage());
        log.error(ex.getMessage(), ex);
        return Result.error(MessageConstant.ServerError.SERVER_ERROR);
    }

}
