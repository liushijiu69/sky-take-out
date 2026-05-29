package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 分类数据访问层
 */
@Mapper
public interface CategoryMapper {

    /**
     * 插入分类数据
     * @param category
     */
    @AutoFill(AutoFill.OperationType.INSERT)
    void insert(Category category);

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 根据id删除分类
     * @param id
     */
    void deleteById(Long id);

    /**
     * 修改分类
     * @param category
     */
    @AutoFill(AutoFill.OperationType.UPDATE)
    void update(Category category);

    /**
     * 根据类型查询分类
     * @param type 分类类型(1菜品分类 2套餐分类)，为null时查询全部
     * @return
     */
    List<Category> list(Integer type);

    /**
     * 根据id查询分类
     * @param id 分类id
     * @return 分类实体
     */
    Category selectById(Long id);
}
