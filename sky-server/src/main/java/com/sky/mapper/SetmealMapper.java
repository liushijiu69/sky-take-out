package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    Integer countByCategoryId(Long id);

    /**
     * 根据菜品id集合查询关联的套餐数量
     * @param dishIds 菜品id集合
     * @return 关联的套餐数量
     */
    Integer countByDishIds(@Param("dishIds") List<Long> dishIds);

}
