package com.sky.controller.user

import com.sky.annotation.AutoLog
import com.sky.result.Result
import com.sky.service.ShopService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * C端-营业状态 Controller
 */
@Tag(name = "C端-营业状态接口")
@RestController("userShopController")
@RequestMapping("/user/shop")
class ShopController(
    private val shopService: ShopService
) {
    @AutoLog(msg = "C端获取店铺营业状态")
    @Operation(summary = "获取营业状态")
    @GetMapping("/status")
    fun getStatus(): Result<Int> {
        val status = shopService.getStatus()
        return Result.success(status)
    }
}
