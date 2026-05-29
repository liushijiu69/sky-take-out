package com.sky.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解,标识方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
//    数据库操作类型
    OperationType value();
    enum OperationType {
        /**
         * 更新操作
         */
        UPDATE,
        /**
         * 插入操作
         */
        INSERT
    }
}

