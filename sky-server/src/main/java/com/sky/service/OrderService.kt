package com.sky.service

import com.sky.dto.OrdersPaymentDTO
import com.sky.dto.OrdersSubmitDTO
import com.sky.result.PageResult
import com.sky.vo.OrderPaymentVO
import com.sky.vo.OrderSubmitVO
import com.sky.vo.OrderVO

interface OrderService {
    fun submitOrder(ordersSubmitDTO: OrdersSubmitDTO): OrderSubmitVO

    fun orderDetail(id: Long): OrderVO

    fun historyOrders(page: Int, pageSize: Int, status: Int?): PageResult

    fun payment(ordersPaymentDTO: OrdersPaymentDTO): OrderPaymentVO

    fun paySuccess(outTradeNo: String)
}
