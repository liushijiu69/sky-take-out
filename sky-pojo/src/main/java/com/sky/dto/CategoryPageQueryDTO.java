package com.sky.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类分页查询数据传递对象
 */
@Data
public class CategoryPageQueryDTO implements Serializable {

    @Min(1)
    private int page;

    @Min(1)
    private int pageSize;

    private String name;

    private Integer type;

}
