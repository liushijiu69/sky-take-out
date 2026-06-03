package com.sky.utils

import cn.hutool.core.util.IdUtil
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


object IDGenerator {
    fun generate(): String {
        return "${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))}_${IdUtil.fastSimpleUUID()}"
    }
}
