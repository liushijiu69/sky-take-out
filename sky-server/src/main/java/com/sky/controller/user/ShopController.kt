package com.sky.controller.user

import com.sky.constant.ShopConstant
import com.sky.result.Result
import com.sky.service.ShopService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
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
    private val log = LoggerFactory.getLogger(ShopController::class.java)

    @Operation(summary = "获取营业状态")
    @GetMapping("/status")
    fun getStatus(): Result<Int> {
        val status = shopService.getStatus()
        log.info("C端获取店铺营业状态: ${if (status == ShopConstant.Status.OPEN.code) "营业中" else "打烊中"}")
        return Result.success(status)
    }
}
