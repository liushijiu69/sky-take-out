package com.sky.controller.admin;

import com.sky.constant.CategoryConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.IllegalException;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/admin/category")
@Tag(name = "分类相关接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     */
    @PostMapping
    @Operation(summary = "新增分类")
    public Result<String> save(@Valid @RequestBody CategoryDTO categoryDTO){
        //校验分类名称
        if (categoryDTO.getName() == null || categoryDTO.getName().isEmpty()) {
            throw new IllegalException(CategoryConstant.NAME + MessageConstant.Param.TOO_LONG_OR_BLANK);
        }
        if (categoryDTO.getName().length() > 32) {
            throw new IllegalException(CategoryConstant.NAME + MessageConstant.Param.TOO_LONG_OR_BLANK);
        }
        //校验分类类型(1菜品分类 2套餐分类)
        if (categoryDTO.getType() == null || !CategoryConstant.Type.contains(categoryDTO.getType())) {
            throw new IllegalException(CategoryConstant.TYPE + MessageConstant.Param.NOT_IN_RANGE);
        }
        //校验排序字段
        if (categoryDTO.getSort() == null || categoryDTO.getSort() < 0) {
            throw new IllegalException(CategoryConstant.SORT + MessageConstant.Param.NOT_IN_RANGE);
        }
        log.info("新增分类：{}", categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 分类分页查询
     */
    @GetMapping("/page")
    @Operation(summary = "分类分页查询")
    public Result<PageResult> page(@Valid CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分页查询：{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除分类
     */
    @DeleteMapping
    @Operation(summary = "删除分类")
    public Result<String> deleteById(@RequestParam Long id){
        log.info("删除分类：{}", id);
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改分类
     */
    @PutMapping
    @Operation(summary = "修改分类")
    public Result<String> update(@Valid @RequestBody CategoryDTO categoryDTO){
        //校验必填参数（id在save时不需要，在update时必须）
        if (categoryDTO.getId() == null) {
            throw new IllegalException(MessageConstant.Param.REQUIRED);
        }
        log.info("修改分类：{}", categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * 启用、禁用分类
     */
    @PostMapping("/status/{status}")
    @Operation(summary = "启用禁用分类")
    public Result<String> startOrStop(@PathVariable("status") Integer status, @RequestParam Long id){
        //校验状态值是否合法(0禁用 1启用)
        if (!CategoryConstant.Status.contains(status)) {
            throw new IllegalException(CategoryConstant.STATUS + status + MessageConstant.Param.NOT_IN_RANGE);
        }
        log.info("启用禁用分类,status:{},id:{}", status, id);
        categoryService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 根据类型查询分类
     */
    @GetMapping("/list")
    @Operation(summary = "根据类型查询分类")
    public Result<List<Category>> list(@RequestParam(required = false) Integer type){
        //校验分类类型(1菜品分类 2套餐分类)
        if (type != null && !CategoryConstant.Type.contains(type)) {
            throw new IllegalException(CategoryConstant.TYPE + MessageConstant.Param.NOT_IN_RANGE);
        }
        log.info("根据类型查询分类,type:{}", type);
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }
}
