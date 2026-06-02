package com.sky.controller.admin
import com.sky.constant.MessageConstant
import com.sky.constant.ShopConstant
import com.sky.exception.IllegalException
import com.sky.result.Result
import com.sky.service.ShopService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 营业状态管理 Controller
 */
@Tag(name = "营业状态相关接口")
@RestController("adminShopController")
@RequestMapping("/admin/shop")
class ShopController(
    private val shopService: ShopService
) {
    private val log = LoggerFactory.getLogger(ShopController::class.java)
    @Operation(summary = "获取营业状态")
    @GetMapping("/status")
    fun getStatus(): Result<Int>{
        log.info("获取店铺营业状态")
        val status : Int =  shopService.getStatus()
        return Result.success(status)
    }

    @Operation(summary = "设置营业状态")
    @PutMapping("/{status}")
    fun setStatus(@PathVariable status: Int): Result<String> {
        //对传入参数进行格式校验
        status.also {
            if (!ShopConstant.Status.contains(it))
                throw IllegalException(ShopConstant.STATUS
                        + status
                        + MessageConstant.Param.NOT_IN_RANGE)
        }
        log.info("设置店铺的营业状态: ${if(status == ShopConstant.Status.OPEN.code) "营业中" else "打烊中"}")
        // 执行业务
        shopService.setStatus(status)
        //返回结果
        return Result.success()
    }
}