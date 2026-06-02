package com.sky.aspect

import com.sky.annotation.AutoLog
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 自定义切面类,实现日志自动记录
 */
@Aspect
@Component
class AutoLogAspect {

    @Around("execution(* com.sky.controller..*.*(..)) && @annotation(com.sky.annotation.AutoLog)")
    fun logAround(joinPoint: ProceedingJoinPoint): Any? {
        val targetClass = joinPoint.target.javaClass//目标类
        val logger = LoggerFactory.getLogger(targetClass)//日志记录器
        val signature = joinPoint.signature as MethodSignature//方法签名
        val autoLog = signature.method.getAnnotation(AutoLog::class.java)//自定义日志注解
        val msg = autoLog.msg//日志信息
        val args = joinPoint.args//方法参数

        logger.info("{} -> 执行开始,参数:{}", msg, args.contentToString())
        val start = System.currentTimeMillis()//开始时间
        try {
            val result = joinPoint.proceed()//执行方法
            val duration = System.currentTimeMillis() - start//耗时
            logger.info("{} -> 执行结束,耗时:{}ms,返回:{}", msg, duration, result)
            return result//返回结果
        } catch (ex: Throwable) {
            val duration = System.currentTimeMillis() - start//耗时
            logger.error("{} -> 执行异常,耗时:{}ms,异常:{}", msg, duration, ex.message)//记录异常信息
            throw ex//抛出异常
        }
    }
}
