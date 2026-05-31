package com.sky.constant

/**
 * 套餐常量
 */
object SetmealConstant {
     /**
      * 套餐名称
      */
     const val NAME: String = "套餐名称"

     /**
      * 分类id
      */
     const val CATEGORY_ID: String = "分类id"

     /**
      * 套餐价格
      */
     const val PRICE: String = "套餐价格"

     /**
      * 图片路径
      */
     const val IMAGE: String = "图片路径"

     /**
      * 套餐描述
      */
     const val DESCRIPTION: String = "套餐描述"

     /**
      * 套餐售卖状态
      */
     const val STATUS: String = "套餐售卖状态"

     /**
      * 套餐状态枚举
      */
     enum class SetmealStatus(val code: Int, val desc: String) {
          /**
           * 起售
           */
          ON_SALE(1, "起售"),

          /**
           * 停售
           */
          OFF_SALE(0, "停售");

          companion object {
               /**
                * 判断值是否合法
                */
               @JvmStatic
               fun contains(v: Int): Boolean =
                    entries.any { it.code == v }
          }
     }
}
