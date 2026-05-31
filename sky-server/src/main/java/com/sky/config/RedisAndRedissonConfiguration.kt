package com.sky.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisAndRedissonConfiguration {
    private val log = LoggerFactory.getLogger(RedisAndRedissonConfiguration::class.java)

    @Value("\${spring.data.redis.host:127.0.0.1}")
    private lateinit var redisHost: String

    @Value("\${spring.data.redis.port:6379}")
    private var redisPort: Int = 6379

    @Value("\${spring.data.redis.database:0}")
    private var redisDatabase: Int = 0

    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String, Any>().apply {
            connectionFactory = redisConnectionFactory
            keySerializer = StringRedisSerializer()
        }
        return redisTemplate
    }

    @Bean(destroyMethod = "shutdown")
    fun redissonClient(): RedissonClient {
        log.info("RedissonClient配置->\tredisHost = ${redisHost} \t redisPort = ${redisPort} \t redisDatabase = ${redisDatabase}")
        val config = Config().apply {
            useSingleServer().apply {
                address = "redis://${redisHost}:${redisPort}"
                database = redisDatabase
            }
            lockWatchdogTimeout = 30000
        }
        return Redisson.create(config).also{
            log.info("RedissonClient配置完成")
        }
    }

}