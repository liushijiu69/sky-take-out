package com.sky.config

import org.springframework.data.redis.cache.RedisCache
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.cache.RedisCacheWriter
import java.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

/**
 * 自定义 RedisCacheManager，支持在 cacheNames 中通过分隔符动态指定 TTL。
 *
 * 当 @Cacheable 等注解的 cacheNames 包含分隔符时，分隔符后的部分会被解析为
 * 缓存过期时间（单位：秒）；未指定时使用默认过期时间（7 天）。
 *
 * ## 使用示例
 *
 * ```java
 * // 指定过期时间为 3600 秒（1 小时）
 * @Cacheable(cacheNames = "userCache:3600")
 *
 * // 未指定 TTL，使用默认值 7 天
 * @Cacheable(cacheNames = "categoryCache")
 * ```
 *
 * ## 设计说明
 *
 * - 仅覆盖 TTL 逻辑，其余（序列化方式、key 前缀格式等）完全遵循父类实现
 * - 解析 TTL 后会将 cacheNames 中的 TTL 部分剥离，避免污染实际的缓存 key
 *
 * @property cacheWriter Redis 缓存写入器
 * @property defaultCacheConfiguration 默认缓存配置
 */
class CustomRedisCacheManager(
    cacheWriter: RedisCacheWriter,
    defaultCacheConfiguration: RedisCacheConfiguration
) : RedisCacheManager(cacheWriter, defaultCacheConfiguration) {

    /**
     * TTL 分隔符。
     *
     * cacheNames 中以此分隔符分割，前半部分为缓存名称，后半部分为过期秒数。
     * 示例：`userCache:3600` → 缓存名称 = "userCache"，TTL = 3600 秒
     */
    private val customTtlSeparator: String = ":"

    /**
     * 默认过期时间。
     *
     * 当 cacheNames 中未指定 TTL 时使用此值。当前为 12 小时。
     */
    private val defaultTtl: Duration = 12.hours.toJavaDuration()

    /**
     * 创建 RedisCache 实例。
     *
     * 覆盖父类方法，在创建缓存前解析 cacheNames 中的自定义 TTL 参数。
     * 解析完成后将干净的缓存名称（不含 TTL 部分）传给父类。
     *
     * @param name Spring Cache 注解中的 cacheNames 值（可能包含 TTL 后缀）
     * @param cacheConfig 默认缓存配置
     * @return 配置了指定 TTL 的 RedisCache 实例
     */
    override fun createRedisCache(name: String, cacheConfig: RedisCacheConfiguration): RedisCache {
        // 剥离 TTL 后缀，得到纯缓存名称
        val cacheName = name.split(customTtlSeparator)[0]
        // 解析 TTL，未指定时使用默认值
        val ttl = getTtlByCustomName(name) ?: defaultTtl
        // 将 TTL 应用到缓存配置中
        val config = cacheConfig.entryTtl(ttl)
        return super.createRedisCache(cacheName, config)
    }

    /**
     * 从 cacheNames 中解析自定义 TTL。
     *
     * 按分隔符拆分 name，取第二部分作为 TTL 秒数。
     * 若不存在分隔符、第二部分无法解析为数字或数字 <= 0，则返回 null。
     *
     * @param name 原始 cacheNames（可能含 TTL 后缀）
     * @return 解析成功的 Duration，解析失败时返回 null
     */
    private fun getTtlByCustomName(name: String): Duration? {
        val parts = name.split(customTtlSeparator)
        if (parts.size <= 1) return null
        return parts[1].toLongOrNull()?.let {
            if (it > 0) it.minutes.toJavaDuration() else null
        }
    }
}
