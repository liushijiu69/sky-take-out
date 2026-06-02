package com.sky.controller.user

import com.sky.annotation.AutoLog
import com.sky.constant.MessageConstant
import com.sky.context.BaseContext
import com.sky.dto.ShoppingCartDTO
import com.sky.exception.IllegalException
import com.sky.result.Result
import com.sky.service.ShoppingCartService
import com.sky.vo.ShoppingCartVO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * C端-购物车接口 Controller
 */
@Tag(name = "C端-购物车接口")
@RestController("userShoppingCartController")
@RequestMapping("/user/shoppingCart")
class ShoppingCartController(
    private val shoppingCartService: ShoppingCartService
) {
    @AutoLog(msg = "添加购物车")
    @Operation(summary = "添加购物车")
    @PostMapping("/add")
    fun add(@RequestBody shoppingCartDTO: ShoppingCartDTO): Result<Int> {
        if ((shoppingCartDTO.dishId == null && shoppingCartDTO.setmealId == null)
            || (shoppingCartDTO.dishId != null && shoppingCartDTO.setmealId != null)){
            throw IllegalException(MessageConstant.Param.ILLEGAL)
        }
        shoppingCartService.addShoppingCart(shoppingCartDTO)
        return Result.success()
    }

    @AutoLog(msg = "查看购物车")
    @Operation(summary = "查看购物车")
    @GetMapping("/list")
    fun list(): Result<List<ShoppingCartVO>> {
        val list = shoppingCartService.list(BaseContext.getCurrentId())
        return Result.success(list)
    }

    @AutoLog(msg = "清空购物车")
    @Operation(summary = "清空购物车")
    @DeleteMapping("/clean")
    fun clean(): Result<String> {
        shoppingCartService.cleanShoppingCart()
        return Result.success()
    }

    @AutoLog(msg = "删除购物车")
    @Operation(summary = "删除购物车")
    @PostMapping("/sub")
    fun delete(@RequestBody shoppingCartDTO: ShoppingCartDTO): Result<String> {
        if ((shoppingCartDTO.dishId == null && shoppingCartDTO.setmealId == null)
            || (shoppingCartDTO.dishId != null && shoppingCartDTO.setmealId != null)){
            throw IllegalException(MessageConstant.Param.ILLEGAL)
        }
        shoppingCartService.deleteShoppingCart(shoppingCartDTO)
        return Result.success()
    }
}
