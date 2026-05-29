package com.sky.service.impl

import com.github.pagehelper.Page
import com.github.pagehelper.PageHelper
import com.sky.constant.DishConstant
import com.sky.constant.MessageConstant
import com.sky.dto.DishDTO
import com.sky.dto.DishPageQueryDTO
import com.sky.entity.Dish
import com.sky.exception.DeletionNotAllowedException
import com.sky.exception.IllegalException
import com.sky.mapper.DishFlavorsMapper
import com.sky.mapper.DishMapper
import com.sky.mapper.SetmealMapper
import com.sky.result.PageResult
import com.sky.service.DishService
import com.sky.vo.DishVO
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DishServiceImpl(
    private val dishMapper: DishMapper,
    private val dishFlavorsMapper: DishFlavorsMapper,
    private val setmealMapper: SetmealMapper,
) : DishService {
    private val log = LoggerFactory.getLogger(DishServiceImpl::class.java)

    /**
     * 批量删除菜品
     * 1. 解析ids参数为id列表
     * 2. 校验是否有起售中的菜品（起售状态），有则不允许删除
     * 3. 校验是否有被套餐关联的菜品，有则不允许删除
     * 4. 删除菜品关联的口味数据
     * 5. 删除菜品
     */
    @Transactional
    override fun deleteBatch(ids: String) {
        // 1. 解析ids字符串（逗号分隔）为Long列表，空串直接返回
        if (ids.isBlank()) return
        val idList = ids.split(",").map { it.trim().toLong() }

        // 2. 校验：起售中的菜品不能删除
        val onSaleCount = dishMapper.countByIdsAndStatus(idList, DishConstant.DishStatus.ON_SALE.code)
        if (onSaleCount > 0) {
            throw DeletionNotAllowedException(MessageConstant.DISH_ON_SALE)
        }

        // 3. 校验：被套餐关联的菜品不能删除
        val linkedCount = setmealMapper.countByDishIds(idList)
        if (linkedCount > 0) {
            throw DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL)
        }

        // 4. 删除菜品关联的所有口味
        dishFlavorsMapper.deleteByDishIds(idList)

        // 5. 删除菜品
        dishMapper.deleteByIds(idList)
    }

    @Transactional
    override fun saveWithFlavor(dishDTO: DishDTO) {
        //校验必填参数
        if (dishDTO.name == null || dishDTO.categoryId == null || dishDTO.price == null || dishDTO.image == null) {
            throw IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL)
        }
        //校验菜品名称长度(数据库varchar(32))
        if (dishDTO.name.length > 32 || dishDTO.name.isEmpty()) {
            throw IllegalException(DishConstant.NAME + MessageConstant.ParamIllegal.TO_LONG_OR_BLANK)
        }
        //校验菜品价格
        if (dishDTO.price < java.math.BigDecimal.ZERO) {
            throw IllegalException(DishConstant.PRICE + MessageConstant.ParamIllegal.NOT_IN_RANGE)
        }

        // 向菜品表插入1条数据
        val dish = Dish()
        BeanUtils.copyProperties(dishDTO, dish)
        dishMapper.insert(dish)//返还生成的主键
        // 向口味表插入n条数据
        val flavors = dishDTO.flavors
        if (flavors != null && flavors.isNotEmpty()) {
            //获取insert语句生成的主键值
            flavors.forEach {it.dishId = dish.id}
            //批量插入
            dishFlavorsMapper.insertBatch(flavors)
        }

    }

    override fun pageQuery(dishPageQueryDTO: DishPageQueryDTO): PageResult {
        //校验参数
        if (dishPageQueryDTO.page <= 0 || dishPageQueryDTO.pageSize <= 0) {
            throw IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL)
        }
        PageHelper.startPage<DishVO>(dishPageQueryDTO.page, dishPageQueryDTO.pageSize)
        val page = dishMapper.selectByPage(dishPageQueryDTO)
        return PageResult(page.total, page.result)
    }
}