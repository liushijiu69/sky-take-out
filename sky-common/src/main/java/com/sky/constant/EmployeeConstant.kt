package com.sky.constant

object EmployeeConstant {
     const val USERNAME: String = "账号"
     const val NAME: String = "姓名"
     const val ID_NUMBER: String = "身份证"
     const val PHONE: String = "电话"
     const val SEX: String = "性别"
     const val PASSWORD: String = "密码"
     /**
      * 默认密码
      */
     const val DEFAULT_PASSWORD = "123456"
     /**
      * 状态
      */
     object Status{
          /**
           * 启用
           */
          const val ENABLE = 1

          /**
           * 禁用
           */
          const val DISABLE = 0
     }

     /**
      * 性别
      */
     enum class Sex(val value: String) {
          FEMALE("0"),
          MALE("1");
          companion object {
               @JvmStatic
               fun contains(v: String): Boolean =
                    entries.any { it.value == v }
          }
     }

}