package com.sky.service.impl

import com.github.pagehelper.PageHelper
import com.sky.constant.DishConstant
import com.sky.constant.MessageConstant
import com.sky.dto.DishDTO
import com.sky.dto.DishPageQueryDTO
import com.sky.entity.Dish
import com.sky.exception.DeletionNotAllowedException
import com.sky.exception.IllegalException
import com.sky.mapper.CategoryMapper
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

/**
 * 菜品业务实现
 */
@Service
class DishServiceImpl(
    private val dishMapper: DishMapper,
    private val dishFlavorsMapper: DishFlavorsMapper,
    private val setmealMapper: SetmealMapper,
    private val categoryMapper: CategoryMapper,
) : DishService {
    private val log = LoggerFactory.getLogger(DishServiceImpl::class.java)

    /**
     * 条件查询菜品和口味
     * 查询起售中的菜品，并关联口味数据
     */
    override fun listWithFlavor(dish: Dish): List<DishVO> {
        //根据分类id查询菜品列表
        val dishList = dishMapper.selectByCategoryId(dish.categoryId)

        return dishList.filter { it.status == 1 }.map { d ->
            DishVO().apply {
                BeanUtils.copyProperties(d, this)
                //根据菜品id查询对应的口味
                flavors = dishFlavorsMapper.selectByDishId(d.id)
            }
        }
    }

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
            throw DeletionNotAllowedException(MessageConstant.Dish.ON_SALE_CANNOT_DELETE)
        }

        // 3. 校验：被套餐关联的菜品不能删除
        val linkedCount = setmealMapper.countByDishIds(idList)
        if (linkedCount > 0) {
            throw DeletionNotAllowedException(MessageConstant.Dish.LINKED_BY_SETMEAL)
        }

        // 4. 删除菜品关联的所有口味
        dishFlavorsMapper.deleteByDishIds(idList)

        // 5. 删除菜品
        dishMapper.deleteByIds(idList)
    }

    /**
     * 新增菜品（含口味）
     * 1. 向菜品表插入1条数据
     * 2. 向口味表插入n条数据
     */
    @Transactional
    override fun saveWithFlavor(dishDTO: DishDTO) {
        // 向菜品表插入1条数据
        val dish = Dish()
        BeanUtils.copyProperties(dishDTO, dish)
        dishMapper.insert(dish)//返还生成的主键
        // 向口味表插入n条数据
        val flavors = dishDTO.flavors
        if (flavors != null && flavors.isNotEmpty()) {
            //获取insert语句生成的主键值
            flavors.forEach { it.dishId = dish.id }
            //批量插入
            dishFlavorsMapper.insertBatch(flavors)
        }
    }

    /**
     * 根据id查询菜品（含口味和分类名称）
     * 1. 查询dish表
     * 2. 查询category表获取分类名称
     * 3. 查询flavor表
     * 4. 组装DishVO并返回
     */
    override fun getById(id: Long): DishVO {
        // 1. 查询dish表
        val dish = dishMapper.selectById(id)
            ?: throw IllegalException(MessageConstant.Server.RESOURCE_NOT_FOUND)
        // 2. 查询category表获取分类名称
        val categoryName = categoryMapper.selectById(dish.categoryId)?.name
        // 3. 查询flavor表
        val flavors = dishFlavorsMapper.selectByDishId(id)
        // 4. 组装DishVO
        val dishVO = DishVO()
        BeanUtils.copyProperties(dish, dishVO)
        dishVO.categoryName = categoryName
        dishVO.flavors = flavors
        return dishVO
    }

    /**
     * 修改菜品（含口味）
     * 1. flavors不为空 → 校验flavors → 删除原口味 → 插入新口味
     * 2. flavors为空 → 跳过flavors处理
     * 3. 修改dish
     */
    @Transactional
    override fun updateWithFlavor(dishDTO: DishDTO) {
        // 1. flavors不为空 → 校验、删除原口味、插入新口味
        val flavors = dishDTO.flavors
        if (flavors != null && flavors.isNotEmpty()) {
            // 校验每个flavor的必填字段
            flavors.forEach {
                if (it.name == null || it.value == null) {
                    throw IllegalException(MessageConstant.Param.REQUIRED)
                }
            }
            // 删除原口味
            dishFlavorsMapper.deleteByDishIds(listOf(dishDTO.id))
            // 插入新口味
            flavors.forEach { it.dishId = dishDTO.id }
            dishFlavorsMapper.insertBatch(flavors)
        }
        // 2. flavors为空 → 跳过（不做任何口味操作）

        // 3. 修改dish
        val dish = Dish()
        BeanUtils.copyProperties(dishDTO, dish)
        dishMapper.update(dish)
    }

    /**
     * 菜品起售、停售
     */
    override fun startOrStop(status: Int, id: Long) {
        // 构造Dish对象并调用已有update方法
        val dish = Dish()
        dish.id = id
        dish.status = status
        dishMapper.update(dish)
    }

    /**
     * 根据分类id查询菜品列表
     */
    override fun listByCategoryId(categoryId: Long): List<Dish> {
        return dishMapper.selectByCategoryId(categoryId)
    }

    /**
     * 菜品分页查询
     */
    override fun pageQuery(dishPageQueryDTO: DishPageQueryDTO): PageResult {
        PageHelper.startPage<DishVO>(dishPageQueryDTO.page, dishPageQueryDTO.pageSize)
        val page = dishMapper.selectByPage(dishPageQueryDTO)
        return PageResult(page.total, page.result)
    }
}
