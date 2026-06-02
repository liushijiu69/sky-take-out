package com.sky.service

import com.sky.dto.SetmealDTO
import com.sky.dto.SetmealPageQueryDTO
import com.sky.entity.Setmeal
import com.sky.result.PageResult
import com.sky.vo.DishItemVO
import com.sky.vo.SetmealVO

/**
 * 套餐 Service 接口
 */
interface SetmealService {

    /**
     * 条件查询套餐
     * @param setmeal 查询条件（categoryId, status 等）
     * @return 套餐列表
     */
    fun list(setmeal: Setmeal): List<Setmeal>

    /**
     * 根据id查询菜品选项
     * @param id 套餐id
     * @return 菜品选项列表
     */
    fun getDishItemById(id: Long): List<DishItemVO>


    /**
     * 新增套餐（含关联菜品）
     * @param setmealDTO 套餐DTO
     */
    fun saveWithDish(setmealDTO: SetmealDTO)

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO 分页查询参数
     * @return 分页结果
     */
    fun pageQuery(setmealPageQueryDTO: SetmealPageQueryDTO): PageResult

    /**
     * 批量删除套餐
     * @param ids 套餐id字符串，多个id用逗号分隔
     */
    fun deleteBatch(ids: String)

    /**
     * 根据id查询套餐
     * @param id 套餐id
     * @return 套餐视图对象
     */
    fun getById(id: Long): SetmealVO

    /**
     * 修改套餐（含关联菜品）
     * @param setmealDTO 套餐DTO
     */
    fun updateWithDish(setmealDTO: SetmealDTO)

    /**
     * 套餐起售、停售
     * @param status 套餐状态（1起售 0停售）
     * @param id 套餐id
     */
    fun startOrStop(status: Int, id: Long)
}
