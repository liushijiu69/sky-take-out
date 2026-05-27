package com.sky.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 令牌工具类，提供创建和解析 JWT 的方法
 */
public class JwtUtil {

    /**
     * 创建 JWT 令牌
     *
     * @param secretKey 密钥（至少 32 个字符）
     * @param ttlMillis 过期时间（毫秒）
     * @param claims    要放入令牌的声明数据
     * @return 生成的 JWT 字符串
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        return Jwts.builder()
                .claims(claims)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    /**
     * 解析 JWT 令牌，获取声明数据
     *
     * @param secretKey 密钥
     * @param token     JWT 字符串
     * @return 解析后的 Claims 对象
     */
    public static Claims parseJWT(String secretKey, String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
