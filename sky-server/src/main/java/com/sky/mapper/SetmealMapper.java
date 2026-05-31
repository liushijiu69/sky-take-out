package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
