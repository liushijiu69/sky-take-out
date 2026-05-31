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
import com.sky.vo.SetmealVO
import org.springframework.beans.BeanUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 套餐 Service 实现
 */
@Service
class SetmealServiceImpl(
    private val setmealMapper: SetmealMapper,
    private val setmealDishMapper: SetmealDishMapper,
    private val dishMapper: DishMapper,
    private val categoryMapper: CategoryMapper,
) : SetmealService {

    /**
     * 新增套餐（含关联菜品）
     * 1. 校验必填参数
     * 2. 校验套餐名称长度
     * 3. 校验套餐价格
     * 4. 校验售卖状态
     * 5. 校验setmealDishes内部必填字段
     * 6. 校验categoryId是否在category表
     * 7. 校验dishId是否存在于dish表（逻辑外键）
     * 8. 向套餐表插入1条数据
     * 9. 将套餐主键赋值给每个setmealDish
     * 10. 批量插入setmeal_dish表
     */
    @Transactional
    override fun saveWithDish(setmealDTO: SetmealDTO) {
        // 1. 校验必填参数（name, categoryId, price, image, status）
        if (setmealDTO.name == null || setmealDTO.categoryId == null || setmealDTO.price == null
            || setmealDTO.image == null || setmealDTO.status == null) {
            throw IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL)
        }
        // 2. 校验套餐名称长度(数据库varchar(32))
        if (setmealDTO.name.length > 32 || setmealDTO.name.isEmpty()) {
            throw IllegalException(SetmealConstant.NAME + MessageConstant.ParamIllegal.TO_LONG_OR_BLANK)
        }
        // 3. 校验套餐价格 >= 0
        if (setmealDTO.price < java.math.BigDecimal.ZERO) {
            throw IllegalException(SetmealConstant.PRICE + MessageConstant.ParamIllegal.NOT_IN_RANGE)
        }
        // 4. 校验售卖状态是否合法(0停售 1起售)
        if (!SetmealConstant.SetmealStatus.contains(setmealDTO.status)) {
            throw IllegalException(SetmealConstant.STATUS + MessageConstant.ParamIllegal.NOT_IN_RANGE)
        }
        // 5. 校验setmealDishes内部必填字段
        val dishes = setmealDTO.setmealDishes
        if (dishes.isEmpty()) {
            throw IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL)
        }
        dishes.forEach {
            if (it.dishId == null || it.copies == null || it.name == null || it.price == null) {
                throw IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL)
            }
        }
        // 6. 校验categoryId是否在category表
        if (categoryMapper.selectById(setmealDTO.categoryId) == null) {
            throw IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL)
        }

        // 7. 校验dishId是否存在于dish表（逻辑外键）
        val dishIds = dishes.map { it.dishId }.distinct()
        if (dishMapper.countByIds(dishIds) != dishIds.size) {
            throw IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL)
        }
        // 7. 向套餐表插入1条数据
        val setmeal = Setmeal()
        BeanUtils.copyProperties(setmealDTO, setmeal)
        setmealMapper.insert(setmeal)
        // 8. 将套餐主键赋值给每个setmealDish
        dishes.forEach { it.setmealId = setmeal.id }
        // 9. 批量插入setmeal_dish表
        setmealDishMapper.insertBatch(dishes)
    }

    /**
     * 套餐分页查询
     * 1. 校验分页参数
     * 2. 分页查询
     * 3. 转为VO返回
     */
    override fun pageQuery(setmealPageQueryDTO: SetmealPageQueryDTO): PageResult {
        // 1. 校验分页参数
        if (setmealPageQueryDTO.page <= 0 || setmealPageQueryDTO.pageSize <= 0) {
            throw IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL)
        }
        // 2. 分页查询
        PageHelper.startPage<Setmeal>(setmealPageQueryDTO.page, setmealPageQueryDTO.pageSize)
        val page = setmealMapper.selectByPage(setmealPageQueryDTO)
        // 3. 转为VO（categoryName 已在 SQL 中映射）
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
            throw DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE)
        }

        // 3. 删除关联的setmeal_dish数据
        setmealDishMapper.deleteBySetmealIds(idList)

        // 4. 删除套餐
        setmealMapper.deleteByIds(idList)
    }

    /**
     * 根据id查询套餐
     * 1. 校验参数
     * 2. 查询setmeal表（含categoryName）
     * 3. 查询setmeal_dish表获取关联菜品
     * 4. 组装SetmealVO并返回
     */
    override fun getById(id: Long): SetmealVO {
        // 1. 校验参数
        if (id <= 0) {
            throw IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL)
        }
        // 2. 查询setmeal表
        val setmeal = setmealMapper.selectById(id)
            ?: throw IllegalException(MessageConstant.ServerError.RESOURCE_NOT_FOUND)
        // 3. 查询关联菜品
        val setmealDishes = setmealDishMapper.selectBySetmealId(id)
        // 4. 组装VO
        val vo = SetmealVO()
        BeanUtils.copyProperties(setmeal, vo)
        vo.setmealDishes = setmealDishes
        return vo
    }

    /**
     * 修改套餐（含关联菜品）
     * 1. 校验必填参数（id, name, categoryId, price, image）
     * 2. 校验套餐名称长度
     * 3. 校验套餐价格
     * 4. 校验售卖状态（如果传了status）
     * 5. 校验categoryId是否在category表
     * 6. 校验setmealDishes内部必填字段
     * 7. 删除原有的setmeal_dish关联数据
     * 8. 插入新的setmeal_dish数据
     * 9. 修改setmeal
     */
    @Transactional
    override fun updateWithDish(setmealDTO: SetmealDTO) {
        // 1. 校验必填参数（id, name, categoryId, price, image）
        if (setmealDTO.id == null || setmealDTO.name == null || setmealDTO.categoryId == null
            || setmealDTO.price == null || setmealDTO.image == null) {
            throw IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL)
        }
        // 2. 校验套餐名称长度(数据库varchar(32))
        if (setmealDTO.name.length > 32 || setmealDTO.name.isEmpty()) {
            throw IllegalException(SetmealConstant.NAME + MessageConstant.ParamIllegal.TO_LONG_OR_BLANK)
        }
        // 3. 校验套餐价格 >= 0
        if (setmealDTO.price < java.math.BigDecimal.ZERO) {
            throw IllegalException(SetmealConstant.PRICE + MessageConstant.ParamIllegal.NOT_IN_RANGE)
        }
        // 4. 校验售卖状态（如果传了）
        if (setmealDTO.status != null && !SetmealConstant.SetmealStatus.contains(setmealDTO.status)) {
            throw IllegalException(SetmealConstant.STATUS + MessageConstant.ParamIllegal.NOT_IN_RANGE)
        }
        // 5. 校验categoryId是否在category表
        if (categoryMapper.selectById(setmealDTO.categoryId) == null) {
            throw IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL)
        }
        // 6. 校验setmealDishes内部必填字段
        val dishes = setmealDTO.setmealDishes
        if (dishes.isEmpty()) {
            throw IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL)
        }
        dishes.forEach {
            if (it.dishId == null || it.copies == null || it.name == null || it.price == null) {
                throw IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL)
            }
        }
        // 7. 删除原有的setmeal_dish关联数据
        setmealDishMapper.deleteBySetmealIds(listOf(setmealDTO.id))
        // 8. 插入新的setmeal_dish数据
        dishes.forEach { it.setmealId = setmealDTO.id }
        setmealDishMapper.insertBatch(dishes)
        // 9. 修改setmeal
        val setmeal = Setmeal()
        BeanUtils.copyProperties(setmealDTO, setmeal)
        setmealMapper.update(setmeal)
    }

    /**
     * 套餐起售、停售
     * 1. 校验状态值是否合法
     * 2. 校验套餐id
     * 3. 构造Setmeal对象并调用已有update方法
     */
    override fun startOrStop(status: Int, id: Long) {
        // 1. 校验状态值是否合法(0停售 1起售)
        if (!SetmealConstant.SetmealStatus.contains(status)) {
            throw IllegalException(SetmealConstant.STATUS + MessageConstant.ParamIllegal.NOT_IN_RANGE)
        }
        // 2. 校验套餐id
        if (id <= 0) {
            throw IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL)
        }
        // 3. 构造Setmeal对象并更新
        val setmeal = Setmeal()
        setmeal.id = id
        setmeal.status = status
        setmealMapper.update(setmeal)
    }
}
