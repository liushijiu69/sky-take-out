package com.sky.service.impl

import com.sky.context.BaseContext
import com.sky.dto.ShoppingCartDTO
import com.sky.entity.ShoppingCart
import com.sky.mapper.DishMapper
import com.sky.mapper.SetmealMapper
import com.sky.mapper.ShoppingCartMapper
import com.sky.service.ShoppingCartService
import com.sky.vo.ShoppingCartVO
import org.springframework.beans.BeanUtils
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ShoppingCartServiceImpl(
    private val shoppingCartMapper: ShoppingCartMapper,
    private val dishMapper: DishMapper,
    private val setmealMapper: SetmealMapper,
): ShoppingCartService {
    @CacheEvict(cacheNames = ["shoppingCartCache"], allEntries = true)
    override fun addShoppingCart(shoppingCartDTO: ShoppingCartDTO) {
        //判断当前加入到购物车中的商品是否已存在
        val shoppingCart = ShoppingCart().apply {// 构造购物车对象
            userId = BaseContext.getCurrentId() // 用户id
            dishFlavor = shoppingCartDTO.dishFlavor // 口味
            dishId = shoppingCartDTO.dishId // 菜品id
            setmealId = shoppingCartDTO.setmealId // 套餐id
        }
        //获取当前用户购物车中的数据
        shoppingCartMapper.selectByShoppingCart(shoppingCart)?.firstOrNull()//获取第一条数据
            //如果存在,将数量加一
            ?.apply { number += 1 }
            ?.also { shoppingCartMapper.updateNumberById(it) }//更新数据库
            //不存在,插入一条购物车数据
            ?: run {
                //判断本次添加到购物车的是菜品还是套餐
                val dishId = shoppingCart.dishId// 菜品id
                val setmealId = shoppingCart.setmealId// 套餐id
                //如果菜品id不为 null
                if (dishId != null){
                    //添加的是菜品
                    val dish = dishMapper.selectById(dishId)
                    shoppingCart.apply {//补充菜品属性
                        image = dish.image// 图片
                        name = dish.name// 名称
                        amount = dish.price// 金额
                    }
                }else{//为 null
                    //添加的是套餐
                    val setmeal = setmealMapper.selectById(setmealId)
                    shoppingCart.apply {//补充套餐属性
                        image = setmeal.image// 图片
                        name = setmeal.name// 名称
                        amount = setmeal.price// 金额
                    }
                }
                shoppingCart.apply {//补充购物车对象的属性
                    number = 1// 数量
                    createTime = LocalDateTime.now()// 创建时间
                }
                //购物车构造完毕,插入数据
                shoppingCartMapper.insert(shoppingCart)
            }

    }
    @Cacheable(cacheNames = ["shoppingCartCache:30"], key = "#userId")
    override fun list(userId: Long): List<ShoppingCartVO> {
        //构造查询条件
        val shoppingCart = ShoppingCart().apply { this.userId = userId }
        //查询当前用户的购物车数据
        val shoppingCartList: List<ShoppingCartVO> = shoppingCartMapper.selectByShoppingCart(shoppingCart).map {
            //创建购物车VO对象,用于封装返回数据
            ShoppingCartVO().apply { BeanUtils.copyProperties(it, this) }
        }
        //返回结果
        return shoppingCartList
    }
    @CacheEvict(cacheNames = ["shoppingCartCache"], allEntries = true)
    override fun cleanShoppingCart() {
        shoppingCartMapper.delete(ShoppingCart().apply {userId = BaseContext.getCurrentId()})
    }
    @CacheEvict(cacheNames = ["shoppingCartCache"], allEntries = true)
    override fun deleteShoppingCart(shoppingCartDTO: ShoppingCartDTO) {
        val shoppingCart = ShoppingCart().apply {
            userId = BaseContext.getCurrentId()
            dishFlavor = shoppingCartDTO.dishFlavor
            dishId = shoppingCartDTO.dishId
            setmealId = shoppingCartDTO.setmealId
        }
        shoppingCartMapper.delete(shoppingCart)
    }
}