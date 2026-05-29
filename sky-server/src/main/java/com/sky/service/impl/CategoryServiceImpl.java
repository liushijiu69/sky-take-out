package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.CategoryConstant;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.IllegalException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类业务层
 */
@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增分类
     * @param categoryDTO
     */
    public void save(CategoryDTO categoryDTO) {
        //校验参数
        if (categoryDTO.getName() == null || categoryDTO.getType() == null || categoryDTO.getSort() == null) {
            throw new IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL);
        }
        //校验分类名称长度(数据库varchar(32))
        if (categoryDTO.getName().length() > 32 || categoryDTO.getName().isEmpty()) {
            throw new IllegalException(CategoryConstant.NAME + MessageConstant.ParamIllegal.TO_LONG_OR_BLANK);
        }
        //校验分类类型(1菜品分类 2套餐分类)
        if (!CategoryConstant.Type.contains(categoryDTO.getType())) {
            throw new IllegalException(CategoryConstant.TYPE + MessageConstant.ParamIllegal.NOT_IN_RANGE);
        }
        //校验排序字段
        if (categoryDTO.getSort() < 0) {
            throw new IllegalException(CategoryConstant.SORT + MessageConstant.ParamIllegal.NOT_IN_RANGE);
        }

        Category category = new Category();
        //属性拷贝
        BeanUtils.copyProperties(categoryDTO, category);

        //分类状态默认为禁用状态0
        category.setStatus(CategoryConstant.Status.DISABLE.getCode());

//        //设置创建时间、修改时间、创建人、修改人
//        category.setCreateTime(LocalDateTime.now());
//        category.setUpdateTime(LocalDateTime.now());
//        category.setCreateUser(BaseContext.getCurrentId());
//        category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.insert(category);
    }

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        //校验参数
        if (categoryPageQueryDTO.getPage() <= 0 || categoryPageQueryDTO.getPageSize() <= 0) {
            throw new IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL);
        }
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        //下一条sql进行分页，自动加入limit关键字分页
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据id删除分类
     * @param id
     */
    public void deleteById(Long id) {
        //校验参数
        if (id == null) {
            throw new IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL);
        }
        //查询当前分类是否关联了菜品，如果关联了就抛出业务异常
        Integer count = dishMapper.countByCategoryId(id);
        if(count > 0){
            //当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        //查询当前分类是否关联了套餐，如果关联了就抛出业务异常
        count = setmealMapper.countByCategoryId(id);
        if(count > 0){
            //当前分类下有套餐，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        //删除分类数据
        categoryMapper.deleteById(id);
    }

    /**
     * 修改分类
     * @param categoryDTO
     */
    public void update(CategoryDTO categoryDTO) {
        //校验参数
        if (categoryDTO.getId() == null) {
            throw new IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL);
        }
        //校验分类名称长度(数据库varchar(32))
        if (categoryDTO.getName() != null && (categoryDTO.getName().length() > 32 || categoryDTO.getName().isEmpty())) {
            throw new IllegalException(CategoryConstant.NAME + MessageConstant.ParamIllegal.TO_LONG_OR_BLANK);
        }
        //校验分类类型(1菜品分类 2套餐分类)
        if (categoryDTO.getType() != null && !CategoryConstant.Type.contains(categoryDTO.getType())) {
            throw new IllegalException(CategoryConstant.TYPE + MessageConstant.ParamIllegal.NOT_IN_RANGE);
        }
        //校验排序字段
        if (categoryDTO.getSort() != null && categoryDTO.getSort() < 0) {
            throw new IllegalException(CategoryConstant.SORT + MessageConstant.ParamIllegal.NOT_IN_RANGE);
        }

        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);

//        //设置修改时间、修改人
//        category.setUpdateTime(LocalDateTime.now());
//        category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.update(category);
    }

    /**
     * 启用、禁用分类
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        //校验参数
        if (status == null || id == null) {
            throw new IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL);
        }
        //校验状态值是否合法(0禁用 1启用)
        if (!CategoryConstant.Status.contains(status)) {
            throw new IllegalException(CategoryConstant.STATUS + MessageConstant.ParamIllegal.NOT_IN_RANGE);
        }
        //校验通过,更新数据库
        Category category = Category.builder()
                .id(id)
                .status(status)
//                .updateTime(LocalDateTime.now())
//                .updateUser(BaseContext.getCurrentId())
                .build();
        categoryMapper.update(category);
    }

    /**
     * 根据类型查询分类
     * @param type
     */
    public List<Category> list(Integer type) {
        //校验类型
        if (type != null && !CategoryConstant.Type.contains(type)) {
            throw new IllegalException(CategoryConstant.TYPE + MessageConstant.ParamIllegal.NOT_IN_RANGE);
        }
        return categoryMapper.list(type);
    }
}
