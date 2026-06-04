package com.sky.service

import com.sky.dto.OrdersCancelDTO
import com.sky.dto.OrdersConfirmDTO
import com.sky.dto.OrdersPageQueryDTO
import com.sky.dto.OrdersPaymentDTO
import com.sky.dto.OrdersRejectionDTO
import com.sky.dto.OrdersSubmitDTO
import com.sky.result.PageResult
import com.sky.vo.OrderPaymentVO
import com.sky.vo.OrderStatisticsVO
import com.sky.vo.OrderSubmitVO
import com.sky.vo.OrderVO

interface OrderService {
    fun submitOrder(ordersSubmitDTO: OrdersSubmitDTO, userId: Long): OrderSubmitVO

    fun orderDetail(id: Long): OrderVO

    fun historyOrders(page: Int, pageSize: Int, status: Int?, userId: Long): PageResult

    fun payment(ordersPaymentDTO: OrdersPaymentDTO): OrderPaymentVO

    fun paySuccess(orderNumber: String)

    fun cancelOrder(id: Long)

    fun repetition(id: Long)

    fun conditionSearch(ordersPageQueryDTO: OrdersPageQueryDTO): PageResult

    fun statistics(): OrderStatisticsVO

    fun confirm(id: Long)

    fun rejection(ordersRejectionDTO: OrdersRejectionDTO)

    fun adminCancelOrder(ordersCancelDTO: OrdersCancelDTO)

    fun delivery(id: Long)

    fun complete(id: Long)
}
