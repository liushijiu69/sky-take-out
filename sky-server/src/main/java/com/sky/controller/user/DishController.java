package com.sky.controller.user;

import com.sky.annotation.AutoLog;
import com.sky.constant.DishConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.exception.IllegalException;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * C端-菜品浏览接口
 */
@Tag(name = "C端-菜品浏览接口")
@RestController("userDishController")
@RequestMapping("/user/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     * 根据分类id查询菜品
     * 仅查询起售中的菜品
     * @param categoryId 分类id
     */
    @AutoLog(msg = "C端-根据分类id查询菜品")
    @GetMapping("/list")
    @Operation(summary = "根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalException(MessageConstant.Param.REQUIRED);
        }
        DishDTO dishDTO = new DishDTO();
        dishDTO.setCategoryId(categoryId);
        dishDTO.setStatus(DishConstant.DishStatus.ON_SALE.getCode());
        List<DishVO> list = dishService.listWithFlavor(dishDTO);
        return Result.success(list);
    }
}
