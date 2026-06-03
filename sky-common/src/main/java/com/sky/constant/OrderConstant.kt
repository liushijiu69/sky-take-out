package com.sky.constant

object OrderConstant {
     const val ID: String = "主键"
     const val NUMBER: String = "订单号"
     const val STATUS: String = "订单状态"
     const val USER_ID: String = "用户id"
     const val ADDRESS_BOOK_ID: String = "地址id"
     const val ORDER_TIME: String = "下单时间"
     const val CHECKOUT_TIME: String = "付款时间"
     const val PAY_METHOD: String = "支付方式"
     const val PAY_STATUS: String = "支付状态"
     const val AMOUNT: String = "订单金额"
     const val REMARK: String = "备注信息"
     const val PHONE: String = "手机号"
     const val ADDRESS: String = "详细地址信息"
     const val USER_NAME: String = "用户姓名"
     const val CONSIGNEE: String = "收货人"
     const val CANCEL_REASON: String = "订单取消原因"
     const val REJECTION_REASON: String = "拒单原因"
     const val CANCEL_TIME: String = "订单取消时间"
     const val ESTIMATED_DELIVERY_TIME: String = "预计送达时间"
     const val DELIVERY_STATUS: String = "配送状态"
     const val DELIVERY_TIME: String = "送达时间"
     const val PACK_AMOUNT: String = "打包费"
     const val TABLEWARE_NUMBER: String = "餐具数量"
     const val TABLEWARE_STATUS: String = "餐具数量状态"
     /** 订单状态 */
     enum class Status(val code: Int, val desc: String) {
          /** 待付款 */
          PENDING_PAYMENT(1, "待付款"),
          /** 待接单 */
          WAITING_FOR_ORDERS(2, "待接单"),
          /** 已接单 */
          ACCEPTED(3, "已接单"),
          /** 派送中 */
          DELIVERY_IN_PROGRESS(4, "派送中"),
          /** 已完成 */
          COMPLETED(5, "已完成"),
          /** 已取消 */
          CANCELLED(6, "已取消");

          companion object {
               @JvmStatic
               fun contains(v: Int): Boolean = entries.any { it.code == v }
          }
     }
     /** 支付方式 */
     enum class PayMethod(val code: Int, val desc: String) {
          /** 微信支付 */
          WECHAT(1, "微信支付"),
          /** 支付宝支付 */
          ALIPAY(2, "支付宝支付");

          companion object {
               @JvmStatic
               fun contains(v: Int): Boolean = entries.any { it.code == v }
          }
     }
     /** 支付状态 */
     enum class PayStatus(val code: Int, val desc: String) {
          /** 未支付 */
          UN_PAID(0, "未支付"),
          /** 已支付 */
          PAID(1, "已支付"),
          /** 退款 */
          REFUND(2, "退款");

          companion object {
               @JvmStatic
               fun contains(v: Int): Boolean = entries.any { it.code == v }
          }
     }
     /** 配送状态 */
     enum class DeliveryStatus(val code: Int, val desc: String) {
          /** 选择具体时间 */
          SPECIFIC_TIME(0, "选择具体时间"),
          /** 立即送出 */
          IMMEDIATE(1, "立即送出");

          companion object {
               @JvmStatic
               fun contains(v: Int): Boolean = entries.any { it.code == v }
          }
     }
     /** 餐具数量状态 */
     enum class TablewareStatus(val code: Int, val desc: String) {
          /** 选择具体数量 */
          SPECIFIC_NUMBER(0, "选择具体数量"),
          /** 按餐量提供 */
          AS_NEEDED(1, "按餐量提供");

          companion object {
               @JvmStatic
               fun contains(v: Int): Boolean = entries.any { it.code == v }
          }
     }
}
