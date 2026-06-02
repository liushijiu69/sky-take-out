package com.sky.service

import com.sky.dto.DishDTO
import com.sky.dto.DishPageQueryDTO
import com.sky.entity.Dish
import com.sky.result.PageResult
import com.sky.vo.DishVO

interface DishService {

    /**
     * 条件查询菜品和口味
     * @param dish 查询条件（categoryId, status 等）
     * @return 菜品视图对象列表（含口味）
     */
    fun listWithFlavor(dish: Dish): List<DishVO>

    fun saveWithFlavor(dishDTO: DishDTO)
    fun pageQuery(dishPageQueryDTO: DishPageQueryDTO): PageResult
    /**
     * 批量删除菜品
     * @param ids 菜品id字符串，多个id用逗号分隔
     */
    fun deleteBatch(ids: String)
    /**
     * 根据id查询菜品（含口味和分类名称）
     * @param id 菜品id
     * @return 菜品视图对象
     */
    fun getById(id: Long): DishVO
    /**
     * 修改菜品（含口味）
     * @param dishDTO 菜品DTO
     */
    fun updateWithFlavor(dishDTO: DishDTO)

    /**
     * 菜品起售、停售
     * @param status 菜品状态（1起售 0停售）
     * @param id 菜品id
     */
    fun startOrStop(status: Int, id: Long)

    /**
     * 根据分类id查询菜品列表
     * @param categoryId 分类id
     * @return 菜品列表
     */
    fun listByCategoryId(categoryId: Long): List<Dish>
}