package com.sky.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类数据传递对象
 */
@Data
public class CategoryDTO implements Serializable {

    private Long id;

    private Integer type;

    @Size(max = 32)
    private String name;

    private Integer sort;

}
