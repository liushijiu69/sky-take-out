package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 套餐分页查询数据传递对象
 */
@Data
public class SetmealPageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

    private String name;

    //分类id
    private Long categoryId;

    //状态 0表示禁用 1表示启用
    private Integer status;

}
