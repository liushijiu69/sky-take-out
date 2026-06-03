package com.sky.controller.user

import com.sky.annotation.AutoLog
import com.sky.constant.MessageConstant
import com.sky.constant.OrderConstant
import com.sky.dto.OrdersPaymentDTO
import com.sky.dto.OrdersSubmitDTO
import com.sky.exception.IllegalException
import com.sky.result.PageResult
import com.sky.result.Result
import com.sky.service.OrderService
import com.sky.vo.OrderPaymentVO
import com.sky.vo.OrderSubmitVO
import com.sky.vo.OrderVO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "C端-订单接口")
@RestController("userOrderController")
@RequestMapping("/user/order")
class OrderController(
    private val orderService: OrderService
) {
    @AutoLog(msg = "用户下单")
    @Operation(summary = "用户下单")
    @PostMapping("/submit")
    fun submit(@Valid @RequestBody ordersSubmitDTO: OrdersSubmitDTO): Result<OrderSubmitVO> {
        ordersSubmitDTO.run {
            if (!OrderConstant.DeliveryStatus.contains(deliveryStatus))
                throw IllegalException(MessageConstant.Param.NOT_IN_RANGE)
            if (!OrderConstant.PayMethod.contains(payMethod))
                throw IllegalException(MessageConstant.Param.NOT_IN_RANGE)
            if (!OrderConstant.TablewareStatus.contains(tablewareStatus))
                throw IllegalException(MessageConstant.Param.NOT_IN_RANGE)
        }
        val orderSubmitVO = orderService.submitOrder(ordersSubmitDTO)
        return Result.success(orderSubmitVO)
    }

    @AutoLog(msg = "查询订单详情")
    @Operation(summary = "查询订单详情")
    @GetMapping("/orderDetail/{id}")
    fun orderDetail(@PathVariable id: Long): Result<OrderVO> {
        val orderVO = orderService.orderDetail(id)
        return Result.success(orderVO)
    }

    @AutoLog(msg = "订单支付")
    @Operation(summary = "订单支付")
    @PutMapping("/payment")
    fun payment(@RequestBody ordersPaymentDTO: OrdersPaymentDTO): Result<OrderPaymentVO> {
        val orderPaymentVO = orderService.payment(ordersPaymentDTO)
        return Result.success(orderPaymentVO)
    }

    @AutoLog(msg = "历史订单查询")
    @Operation(summary = "历史订单查询")
    @GetMapping("/historyOrders")
    fun historyOrders(
        @RequestParam page: Int,
        @RequestParam pageSize: Int,
        @RequestParam(required = false) status: Int?,
    ): Result<PageResult> {
        if (page < 1 || pageSize < 1) {
            throw IllegalException(MessageConstant.Param.NOT_IN_RANGE)
        }
        val pageResult = orderService.historyOrders(page, pageSize, status)
        return Result.success(pageResult)
    }
}
