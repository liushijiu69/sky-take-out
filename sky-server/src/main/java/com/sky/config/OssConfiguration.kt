package com.sky.config

import com.sky.properties.AliOssProperties
import com.sky.utils.AliOssUtil
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 配置类,用于创建AliOssUtil对象
 */
@Configuration
class OssConfiguration {
    private val log = LoggerFactory.getLogger(OssConfiguration::class.java)
    @Bean
    @ConditionalOnMissingBean
    fun aliOssUtil(aop: AliOssProperties): AliOssUtil{
        log.info("创建AliOssUtil对象:{}",aop)
        return AliOssUtil(
            aop.endpoint,
            aop.accessKeyId,
            aop.accessKeySecret,
            aop.bucketName
        )
    }
}