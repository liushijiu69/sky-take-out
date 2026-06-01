package com.sky.service.impl

import com.sky.constant.JwtClaimsConstant
import com.sky.entity.User
import com.sky.properties.JwtProperties
import com.sky.service.UserService
import com.sky.utils.JwtUtil
import com.sky.vo.UserLoginVO
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val jwtProperties: JwtProperties
) : UserService{
    override fun wechatLogin(code: String): UserLoginVO {

        val user = User()
        val claims = mapOf<String, Any>(
            JwtClaimsConstant.USER_ID to user.id,
        )
        val token = JwtUtil.createJWT(jwtProperties.userSecretKey, jwtProperties.userTtl, claims)
        return UserLoginVO().apply {
            this.id = user.id
            this.openid = user.openid
            this.token = token
        }
    }
}