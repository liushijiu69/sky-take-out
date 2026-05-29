package com.sky.constant

object EmployeeConstant {
     const val USERNAME: String = "账号"
     const val NAME: String = "姓名"
     const val ID_NUMBER: String = "身份证"
     const val PHONE: String = "电话"
     const val SEX: String = "性别"
     const val PASSWORD: String = "密码"
     const val STATUS: String = "状态"
     /**
      * 默认密码
      */
     const val DEFAULT_PASSWORD = "123456"
     /**
      * 状态
      */
      enum class Status(val code: Int, val desc: String) {
           /** 启用 */ ENABLE(1, "启用"),
           /** 禁用 */ DISABLE(0, "禁用");
           companion object {
                @JvmStatic
                fun contains(v: Int): Boolean =
                     entries.any { it.code == v }
           }
      }

      /**
       * 性别
       */
      enum class Sex(val code: String, val desc: String) {
           /** 女 */ FEMALE("0", "女"),
           /** 男 */ MALE("1", "男");
           companion object {
                @JvmStatic
                fun contains(v: String): Boolean =
                     entries.any { it.code == v }
           }
      }

}