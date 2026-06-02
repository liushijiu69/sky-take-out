package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import jakarta.validation.ConstraintViolationException;
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
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.warn("业务异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获静态资源不存在异常
     */
    @ExceptionHandler
    public Result exceptionHandler(NoResourceFoundException ex){
        return Result.error(MessageConstant.Server.RESOURCE_NOT_FOUND);
    }

    /**
     * 捕获参数校验异常（@Validated + @PathVariable/@RequestParam 校验失败）
     */
    @ExceptionHandler
    public Result handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("参数格式错误：{}", ex.getMessage());
        return Result.error("参数格式错误: " + ex.getMessage());
    }

    /**
     * 捕获其他未知异常
     */
    @ExceptionHandler
    public Result exceptionHandler(Exception ex){
        log.error("系统异常：{}", ex.getMessage());
        log.error(ex.getMessage(), ex);
        return Result.error(MessageConstant.Server.ERROR);
    }

}
