package com.sky.aspect

import com.sky.annotation.AutoFill
import com.sky.annotation.AutoFill.OperationType.INSERT
import com.sky.annotation.AutoFill.OperationType.UPDATE
import com.sky.constant.AutoFillConstant
import com.sky.context.BaseContext
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 自定义切面类,实现公共字段自动填充
 */
@Aspect
@Component
class AutoFileAspect {
    private val log = LoggerFactory.getLogger(AutoFileAspect::class.java)

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    fun autoFillPointCut(){}

    /**
     * 前置通知,在通知中进行公共字段赋值
     */
    @Before("autoFillPointCut()")
    fun autoFile(joinPoint: JoinPoint) {
        log.info("开始进行公共字段自动填充...")
        // 获取当前被拦截的方法的数据库操作类型
        val signature = joinPoint.signature as MethodSignature
        val autoFill: AutoFill = signature.method.getAnnotation(AutoFill::class.java)
        val operationType: AutoFill.OperationType = autoFill.value
        // 获取方法参数--实体对象
        // 获取第一个参数,集合为空或长度为0则返回
        val param:Any = joinPoint.args?.firstOrNull() ?: return
        // 准备赋值数据
        val now = LocalDateTime.now()
        val currentId = BaseContext.getCurrentId()
        // 根据不同操作类型,通过反射为对象赋值
        when (operationType) {
            INSERT -> {//为四个公共字段赋值
                param.javaClass.getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime::class.java)
                    .invoke(param, now)
                param.javaClass.getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime::class.java)
                    .invoke(param, now)
                param.javaClass.getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long::class.javaObjectType)
                    .invoke(param, currentId)
                param.javaClass.getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long::class.javaObjectType)
                    .invoke(param, currentId)
            }
            UPDATE -> {//为两个公共字段赋值
                param.javaClass.getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime::class.java)
                    .invoke(param, now)
                param.javaClass.getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long::class.javaObjectType)
                    .invoke(param, currentId)
            }
        }
    }

}