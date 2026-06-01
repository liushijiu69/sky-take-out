package com.sky.constant

object ShopConstant {
    const val STATUS = "店铺状态status="
    enum class Status(val code: Int,val desc: String) {
        OPEN(1, "营业中"),
        CLOSE(0, "打烊中");
        companion object {
            fun contains(v: Int): Boolean = DishConstant.DishStatus.entries.any { it.code == v }
        }
    }
}