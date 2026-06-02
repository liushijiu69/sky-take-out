package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 动态条件查询菜品
     * @param dish 查询条件（name, categoryId, status）
     * @return 菜品列表
     */
    List<Dish> list(Dish dish);


    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 根据id集合统计菜品数量
     * @param ids 菜品id集合
     * @return 存在的菜品数量
     */
    Integer countByIds(@Param("ids") List<Long> ids);

    /**
     * 根据分类id查询菜品列表
     * @param categoryId 分类id
     * @return 菜品列表
     */
    List<Dish> selectByCategoryId(Long categoryId);

    @AutoFill(AutoFill.OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> selectByPage(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id集合和状态统计菜品数量
     * @param ids    菜品id集合
     * @param status 菜品状态（0停售 1起售）
     * @return 符合条件的菜品数量
     */
    Integer countByIdsAndStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);

    /**
     * 根据id集合批量删除菜品
     * @param ids 菜品id集合
     */
    void deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 根据id查询菜品
     * @param id 菜品id
     * @return 菜品实体
     */
    Dish selectById(Long id);

    /**
     * 修改菜品
     * @param dish 菜品实体
     */
    @AutoFill(AutoFill.OperationType.UPDATE)
    void update(Dish dish);
}
