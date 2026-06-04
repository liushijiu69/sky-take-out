package com.sky.controller.admin

import com.sky.annotation.AutoLog
import com.sky.constant.MessageConstant
import com.sky.dto.OrdersCancelDTO
import com.sky.dto.OrdersConfirmDTO
import com.sky.dto.OrdersPageQueryDTO
import com.sky.dto.OrdersRejectionDTO
import com.sky.exception.IllegalException
import com.sky.result.PageResult
import com.sky.result.Result
import com.sky.service.OrderService
import com.sky.vo.OrderStatisticsVO
import com.sky.vo.OrderVO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "管理端-订单接口")
@RestController("adminOrderController")
@RequestMapping("/admin/order")
class OrderController(
    private val orderService: OrderService,
) {
    @AutoLog(msg = "订单搜索")
    @Operation(summary = "订单搜索")
    @GetMapping("/conditionSearch")
    fun conditionSearch(ordersPageQueryDTO: OrdersPageQueryDTO): Result<PageResult> {
        if (ordersPageQueryDTO.page < 1 || ordersPageQueryDTO.pageSize < 1) {
            throw IllegalException(MessageConstant.Param.NOT_IN_RANGE)
        }
        val pageResult = orderService.conditionSearch(ordersPageQueryDTO)
        return Result.success(pageResult)
    }

    @AutoLog(msg = "订单数量统计")
    @Operation(summary = "订单数量统计")
    @GetMapping("/statistics")
    fun statistics(): Result<OrderStatisticsVO> {
        return Result.success(orderService.statistics())
    }

    @AutoLog(msg = "查询订单详情")
    @Operation(summary = "查询订单详情")
    @GetMapping("/details/{id}")
    fun details(@PathVariable id: Long): Result<OrderVO> {
        return Result.success(orderService.orderDetail(id))
    }

    @AutoLog(msg = "接单")
    @Operation(summary = "接单")
    @PutMapping("/confirm")
    fun confirm(@RequestBody ordersConfirmDTO: OrdersConfirmDTO): Result<String> {
        if (ordersConfirmDTO.id == null) {
            throw IllegalException(MessageConstant.Param.REQUIRED)
        }
        orderService.confirm(ordersConfirmDTO.id)
        return Result.success()
    }

    @AutoLog(msg = "拒单")
    @Operation(summary = "拒单")
    @PutMapping("/rejection")
    fun rejection(@RequestBody ordersRejectionDTO: OrdersRejectionDTO): Result<String> {
        // 参数校验
        if (ordersRejectionDTO.id == null) {// id为空
            throw IllegalException(MessageConstant.Param.REQUIRED)
        }
        if (ordersRejectionDTO.rejectionReason.isNullOrBlank()) {// 拒单原因为空
            throw IllegalException(MessageConstant.Param.REQUIRED)
        }
        // 拒单
        orderService.rejection(ordersRejectionDTO)
        return Result.success()
    }

    @AutoLog(msg = "取消订单")
    @Operation(summary = "取消订单")
    @PutMapping("/cancel")
    fun cancel(@RequestBody ordersCancelDTO: OrdersCancelDTO): Result<String> {
        if (ordersCancelDTO.id == null) {
            throw IllegalException(MessageConstant.Param.REQUIRED)
        }
        if (ordersCancelDTO.cancelReason.isNullOrBlank()) {
            throw IllegalException(MessageConstant.Param.REQUIRED)
        }
        orderService.adminCancelOrder(ordersCancelDTO)
        return Result.success()
    }

    @AutoLog(msg = "派送订单")
    @Operation(summary = "派送订单")
    @PutMapping("/delivery/{id}")
    fun delivery(@PathVariable id: Long): Result<String> {
        orderService.delivery(id)
        return Result.success()
    }

    @AutoLog(msg = "完成订单")
    @Operation(summary = "完成订单")
    @PutMapping("/complete/{id}")
    fun complete(@PathVariable id: Long): Result<String> {
        orderService.complete(id)
        return Result.success()
    }
}
