package com.sky.interceptor

import com.sky.constant.JwtClaimsConstant
import com.sky.context.BaseContext
import com.sky.properties.JwtProperties
import com.sky.utils.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class JwtTokenUserInterceptor(
    private val jwtProperties: JwtProperties,
): HandlerInterceptor {
    private val log = LoggerFactory.getLogger(JwtTokenUserInterceptor::class.java)

    /**
     *
     */
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (handler !is HandlerMethod) {
            //当前拦截到的不是动态方法，直接放行
            return true
        }
        //1、从请求头中获取令牌
        val token = request.getHeader(jwtProperties.userTokenName)
        //2、校验令牌
        try {
            log.info("jwt校验:{}", token)
            val claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token)
            val userId = claims.get(JwtClaimsConstant.USER_ID).toString().toLong()
            BaseContext.setCurrentId(userId)
            log.info("当前员工id：{}", userId)
            //3、通过，放行
            return true
        } catch (ex: Exception) {
            //4、不通过，响应401状态码
            response.setStatus(401)
            return false
        }
    }


    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        BaseContext.removeCurrentId()
        super.afterCompletion(request, response, handler, ex)
    }
}