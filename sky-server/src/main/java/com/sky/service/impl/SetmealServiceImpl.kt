package com.sky.service.impl

import com.github.pagehelper.PageHelper
import com.sky.constant.MessageConstant
import com.sky.constant.SetmealConstant
import com.sky.dto.SetmealDTO
import com.sky.dto.SetmealPageQueryDTO
import com.sky.entity.Setmeal
import com.sky.exception.DeletionNotAllowedException
import com.sky.exception.IllegalException
import com.sky.mapper.CategoryMapper
import com.sky.mapper.DishMapper
import com.sky.mapper.SetmealDishMapper
import com.sky.mapper.SetmealMapper
import com.sky.result.PageResult
import com.sky.service.SetmealService
import com.sky.vo.DishItemVO
import com.sky.vo.SetmealVO
import org.springframework.beans.BeanUtils
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 套餐业务实现
 */
@Service
class SetmealServiceImpl(
    private val setmealMapper: SetmealMapper,
    private val setmealDishMapper: SetmealDishMapper,
    private val dishMapper: DishMapper,
    private val categoryMapper: CategoryMapper,
) : SetmealService {

    /**
     * 条件查询套餐
     */
    @Cacheable(cacheNames = ["setmealCache"], key = "#setmeal.categoryId")
    override fun list(setmeal: Setmeal): List<Setmeal> {
        return setmealMapper.list(setmeal)
    }

    /**
     * 根据套餐id查询菜品选项
     */
    override fun getDishItemById(id: Long): List<DishItemVO> {
        return setmealMapper.getDishItemBySetmealId(id)
    }

    /**
     * 新增套餐（含关联菜品）
     * 1. 校验categoryId是否在category表
     * 2. 校验dishId是否存在于dish表（逻辑外键）
     * 3. 向套餐表插入1条数据
     * 4. 将套餐主键赋值给每个setmealDish
     * 5. 批量插入setmeal_dish表
     */
    @CacheEvict(cacheNames = ["setmealCache"], key = "#setmealDTO.categoryId")
    @Transactional
    override fun saveWithDish(setmealDTO: SetmealDTO) {
        // 1. 校验categoryId是否在category表
        if (categoryMapper.selectById(setmealDTO.categoryId) == null) {
            throw IllegalException(MessageConstant.Param.REQUIRED)
        }

        // 2. 校验dishId是否存在于dish表（逻辑外键）
        val dishIds = setmealDTO.setmealDishes.map { it.dishId }.distinct()
        if (dishMapper.countByIds(dishIds) != dishIds.size) {
            throw IllegalException(MessageConstant.Param.REQUIRED)
        }

        // 3. 向套餐表插入1条数据
        val setmeal = Setmeal()
        BeanUtils.copyProperties(setmealDTO, setmeal)
        setmealMapper.insert(setmeal)

        // 4. 将套餐主键赋值给每个setmealDish
        setmealDTO.setmealDishes.forEach { it.setmealId = setmeal.id }
        // 5. 批量插入setmeal_dish表
        setmealDishMapper.insertBatch(setmealDTO.setmealDishes)
    }

    /**
     * 套餐分页查询
     * 1. 分页查询
     * 2. 转为VO返回
     */
    override fun pageQuery(setmealPageQueryDTO: SetmealPageQueryDTO): PageResult {
        PageHelper.startPage<Setmeal>(setmealPageQueryDTO.page, setmealPageQueryDTO.pageSize)
        // 分页查询
        val page = setmealMapper.selectByPage(setmealPageQueryDTO)
        // 转为VO（categoryName 已在 SQL 中映射）
        val records = page.result.map { setmeal ->
            val vo = SetmealVO()
            BeanUtils.copyProperties(setmeal, vo)
            vo.setmealDishes = null
            vo
        }
        return PageResult(page.total, records)
    }

    /**
     * 批量删除套餐
     * 1. 解析ids参数为id列表
     * 2. 校验是否有起售中的套餐，有则不允许删除
     * 3. 删除关联的setmeal_dish数据
     * 4. 删除套餐
     */
    @Transactional
    override fun deleteBatch(ids: String) {
        // 1. 解析ids字符串（逗号分隔）为Long列表，空串直接返回
        if (ids.isBlank()) return
        val idList = ids.split(",").map { it.trim().toLong() }

        // 2. 校验：起售中的套餐不能删除
        val onSaleCount = setmealMapper.countByIdsAndStatus(idList, SetmealConstant.SetmealStatus.ON_SALE.code)
        if (onSaleCount > 0) {
            throw DeletionNotAllowedException(MessageConstant.Setmeal.ON_SALE_CANNOT_DELETE)
        }

        // 3. 删除关联的setmeal_dish数据
        setmealDishMapper.deleteBySetmealIds(idList)

        // 4. 删除套餐
        setmealMapper.deleteByIds(idList)
    }

    /**
     * 根据id查询套餐
     * 1. 查询setmeal表（含categoryName）
     * 2. 查询setmeal_dish表获取关联菜品
     * 3. 组装SetmealVO并返回
     */
    override fun getById(id: Long): SetmealVO {
        // 1. 查询setmeal表
        val setmeal = setmealMapper.selectById(id)
            ?: throw IllegalException(MessageConstant.Server.RESOURCE_NOT_FOUND)
        // 2. 查询关联菜品
        val setmealDishes = setmealDishMapper.selectBySetmealId(id)
        // 3. 组装VO
        val vo = SetmealVO()
        BeanUtils.copyProperties(setmeal, vo)
        vo.setmealDishes = setmealDishes
        return vo
    }

    /**
     * 修改套餐（含关联菜品）
     * 1. 校验categoryId是否在category表
     * 2. 删除原有的setmeal_dish关联数据
     * 3. 插入新的setmeal_dish数据
     * 4. 修改setmeal
     */
    @CacheEvict(cacheNames = ["setmealCache"], allEntries = true)
    @Transactional
    override fun updateWithDish(setmealDTO: SetmealDTO) {
        // 1. 校验categoryId是否在category表
        if (categoryMapper.selectById(setmealDTO.categoryId) == null) {
            throw IllegalException(MessageConstant.Param.REQUIRED)
        }

        // 2. 删除原有的setmeal_dish关联数据
        val dishes = setmealDTO.setmealDishes
        setmealDishMapper.deleteBySetmealIds(listOf(setmealDTO.id))
        // 3. 插入新的setmeal_dish数据
        dishes.forEach { it.setmealId = setmealDTO.id }
        setmealDishMapper.insertBatch(dishes)

        // 4. 修改setmeal
        val setmeal = Setmeal()
        BeanUtils.copyProperties(setmealDTO, setmeal)
        setmealMapper.update(setmeal)
    }

    /**
     * 套餐起售、停售
     * 构造Setmeal对象并调用已有update方法
     */
    @CacheEvict(cacheNames = ["setmealCache"], allEntries =  true)
    override fun startOrStop(status: Int, id: Long) {
        val setmeal = Setmeal()
        setmeal.id = id
        setmeal.status = status
        setmealMapper.update(setmeal)
    }
}
