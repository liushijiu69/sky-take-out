package com.sky.service

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy

interface ShopService {
    @PostConstruct
    fun init (){}
    @PreDestroy
    fun cleanup(){}
    /*业务方法*/
    fun setStatus(status: Int)

    fun getStatus(): Int

}