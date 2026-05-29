package com.sky.service

import com.sky.dto.DishDTO
import com.sky.dto.DishPageQueryDTO
import com.sky.result.PageResult

interface DishService {
    fun saveWithFlavor(dishDTO: DishDTO)
    fun pageQuery(dishPageQueryDTO: DishPageQueryDTO): PageResult
    /**
     * 批量删除菜品
     * @param ids 菜品id字符串，多个id用逗号分隔
     */
    fun deleteBatch(ids: String)
}