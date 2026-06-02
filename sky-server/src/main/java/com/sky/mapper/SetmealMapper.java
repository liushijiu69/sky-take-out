package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 新增套餐
     * @param setmeal 套餐对象
     */
    @AutoFill(AutoFill.OperationType.INSERT)
    void insert(Setmeal setmeal);

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO 分页查询参数
     * @return 套餐分页结果
     */
    Page<Setmeal> selectByPage(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据id查询套餐
     * @param id 套餐id
     * @return 套餐实体
     */
    Setmeal selectById(Long id);

    /**
     * 修改套餐
     * @param setmeal 套餐实体
     */
    @AutoFill(AutoFill.OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 根据id集合和状态统计套餐数量
     * @param ids    套餐id集合
     * @param status 套餐状态（0停售 1起售）
     * @return 符合条件的套餐数量
     */
    Integer countByIdsAndStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);

    /**
     * 根据id集合批量删除套餐
     * @param ids 套餐id集合
     */
    void deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 动态条件查询套餐
     * @param setmeal 查询条件（name, categoryId, status）
     * @return 套餐列表
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId 套餐id
     * @return 菜品选项列表
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

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
