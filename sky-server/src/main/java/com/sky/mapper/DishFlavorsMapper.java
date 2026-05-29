package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Mapper
public interface DishFlavorsMapper {

    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品id集合批量删除口味
     * @param ids 菜品id集合
     */
    void deleteByDishIds(@Param("ids") List<Long> ids);

    /**
     * 根据菜品id查询口味列表
     * @param dishId 菜品id
     * @return 口味列表
     */
    List<DishFlavor> selectByDishId(Long dishId);
}
