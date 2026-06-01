package com.sky.service.impl

import com.sky.constant.RedisConstant
import com.sky.constant.ShopConstant
import com.sky.service.ShopService
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class ShopServiceImpl(
    private val redisTemplate: RedisTemplate<String, Any>,
): ShopService {
    @PostConstruct
    override fun init() {
        redisTemplate.opsForValue().set(RedisConstant.SHOP_STATUS, ShopConstant.Status.CLOSE.code)
    }
    @PreDestroy
    override fun cleanup() {
        redisTemplate.delete(RedisConstant.SHOP_STATUS)
    }

    override fun setStatus(status: Int) {
        redisTemplate.opsForValue().set(RedisConstant.SHOP_STATUS, status)
    }

    override fun getStatus(): Int = redisTemplate.opsForValue().get(RedisConstant.SHOP_STATUS) as Int


}