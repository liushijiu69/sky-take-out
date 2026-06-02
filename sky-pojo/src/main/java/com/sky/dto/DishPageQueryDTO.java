package com.sky.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

/**
 * 菜品分页查询数据传递对象
 */
@Data
public class DishPageQueryDTO implements Serializable {

    @Min(1)
    private int page;

    @Min(1)
    private int pageSize;

    private String name;

    private Long categoryId;

    private Integer status;

}
