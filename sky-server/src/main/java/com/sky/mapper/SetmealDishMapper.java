package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 套餐菜品关系 Mapper
 */
@Mapper
public interface SetmealDishMapper {

    /**
     * 批量插入套餐菜品关系
     * @param setmealDishes 套餐菜品关系列表
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id集合批量删除套餐菜品关系
     * @param setmealIds 套餐id集合
     */
    void deleteBySetmealIds(@Param("setmealIds") List<Long> setmealIds);

    /**
     * 根据套餐id查询关联菜品列表
     * @param setmealId 套餐id
     * @return 关联菜品列表
     */
    List<SetmealDish> selectBySetmealId(Long setmealId);
}
