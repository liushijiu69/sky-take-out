package com.sky.controller.user
import com.sky.annotation.AutoLog
import com.sky.result.Result
import com.sky.dto.UserLoginDTO
import com.sky.service.UserService
import com.sky.vo.UserLoginVO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "C端-用户接口")
@RestController("userUserController")
@RequestMapping("/user/user")
class UserController(
    private val userService: UserService
) {
    @AutoLog(msg = "微信登录")
    @Operation(summary = "微信登录")
    @PostMapping("/login")
    fun wechatLogin(@Valid @RequestBody userLoginDTO: UserLoginDTO): Result<UserLoginVO> {
        val userLoginVO:UserLoginVO = userService.wechatLogin(userLoginDTO)
        return Result.success(userLoginVO)
    }
}
