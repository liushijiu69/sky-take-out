package com.sky.service.impl

import com.alibaba.fastjson.JSON
import com.sky.constant.JwtClaimsConstant
import com.sky.constant.MessageConstant
import com.sky.dto.UserLoginDTO
import com.sky.entity.User
import com.sky.exception.LoginFailedException
import com.sky.mapper.UserMapper
import com.sky.properties.JwtProperties
import com.sky.properties.WeChatProperties
import com.sky.service.UserService
import com.sky.utils.HttpClientUtil
import com.sky.utils.JwtUtil
import com.sky.vo.UserLoginVO
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserServiceImpl(
    private val jwtProperties: JwtProperties,
    private val weChatProperties: WeChatProperties,
    private val userMapper: UserMapper
) : UserService{
    companion object{
        private const val WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session"
    }

    /**
     * 微信登录
     * @param userLoginDTO
     * @return UserLoginVO
     */
    override fun wechatLogin(userLoginDTO: UserLoginDTO): UserLoginVO {
        //调用微信服务接口,获取openid //判断openid是否为空,如果为空则抛出异常
        val openId = getOpenId(userLoginDTO.code) ?: throw LoginFailedException(MessageConstant.Login.FAILED)
        //根据openid查询数据库，判断该用户是否存在
        val user = userMapper.getByOpenid(openId)
        //如果不存在，创建新用户，保存到数据库，并返回
            ?: User().apply { // 构造新用户对象
                openid = openId
                createTime = LocalDateTime.now()
            }.also { // 保存到数据库
                userMapper.insert(it)
            }

        //如果存在，则直接返回
        // 创建JWT令牌
        val claims = mapOf<String, Any>(
            JwtClaimsConstant.USER_ID to user.id,
        )
        val token = JwtUtil.createJWT(jwtProperties.userSecretKey, jwtProperties.userTtl, claims)
        // 构造并返回VO对象
        return UserLoginVO().apply {
            this.id = user.id
            this.openid = user.openid
            this.token = token
        }
    }

    /**
     * 获取微信登录的openid
     * @param code 微信登录成功后，微信会返回一个code，这个code需要发送给微信服务器，去换取openid
     * @return openid
     */
    private fun getOpenId(code: String): String? = HttpClientUtil.doPost(
        WX_LOGIN,// 请求地址
        mapOf(
            "appid" to weChatProperties.appid,
            "secret" to weChatProperties.secret,
            "js_code" to code,
            "grant_type" to "authorization_code",
        )// 请求参数
    ).let { JSON.parseObject(it) }// 将json字符串转为map
        .getString("openid")
}