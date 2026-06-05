package com.sky.result

import java.io.Serializable

data class WebsocketResult(
    val type: Int,
    val orderId: Long,
    val content: String,
): Serializable {
    companion object{
        enum class Type(val code: Int, val desc: String) {
            ORDER_REMINDERS(1, "订单提醒"),
            CUSTOMER_DEMAND(2, "客户催单"),
        }
        // 创建订单提醒
        fun orderReminders(orderId: Long, content: String): WebsocketResult {
            return WebsocketResult(Type.ORDER_REMINDERS.code, orderId, content)
        }
        // 创建客户催单
        fun customerDemand(orderId: Long, content: String): WebsocketResult {
            return WebsocketResult(Type.CUSTOMER_DEMAND.code, orderId, content)
        }
    }
}