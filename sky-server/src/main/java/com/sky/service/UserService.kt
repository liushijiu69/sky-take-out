package com.sky.service

import com.sky.vo.UserLoginVO
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy

interface UserService {
    @PostConstruct
    fun init (){}
    @PreDestroy
    fun cleanup(){}
    fun wechatLogin(code: String): UserLoginVO
}