package com.sky.constant

object DishConstant {
     const val NAME: String = "菜品名称"
     const val CATEGORY_ID: String = "分类id"
     const val PRICE: String = "菜品价格"
     const val IMAGE: String = "图片路径"

     /** 菜品状态 */
     enum class DishStatus(val code: Int, val desc: String) {
          /** 起售 */
          ON_SALE(1, "起售"),
          /** 停售 */
          OFF_SALE(0, "停售")
     }
}
