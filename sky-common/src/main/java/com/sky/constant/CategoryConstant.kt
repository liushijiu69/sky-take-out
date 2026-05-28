package com.sky.constant

/**
 * 分类常量
 */
object CategoryConstant {
     /**
      * 分类名称
      */
     const val NAME: String = "分类名称"
     /**
      * 分类类型
      */
     const val TYPE: String = "分类类型"
     /**
      * 排序
      */
     const val SORT: String = "排序"
     /**
      * 分类状态
      */
     const val STATUS: String = "分类状态"

     /**
      * 分类类型枚举
      */
     enum class Type(val value: Int) {
          /**
           * 菜品分类
           */
          DISH(1),

          /**
           * 套餐分类
           */
          SETMEAL(2);

          companion object {
               /**
                * 判断值是否合法
                */
               @JvmStatic
               fun contains(v: Int): Boolean =
                    entries.any { it.value == v }
          }
     }

     /**
      * 分类状态枚举
      */
     enum class Status(val value: Int) {
          /**
           * 启用
           */
          ENABLE(1),
          /**
           * 禁用
           */
          DISABLE(0);

          companion object {
               /**
                * 判断值是否合法
                */
               @JvmStatic
               fun contains(v: Int): Boolean =
                    entries.any { it.value == v }
          }
     }
}
