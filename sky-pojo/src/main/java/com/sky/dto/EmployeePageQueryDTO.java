package com.sky.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 员工分页查询数据传递对象
 */
@Data
public class EmployeePageQueryDTO implements Serializable {

    private String name;

    @NotNull
    @Min(1)
    private Integer page;

    @NotNull
    @Min(0)
    private Integer pageSize;

}
