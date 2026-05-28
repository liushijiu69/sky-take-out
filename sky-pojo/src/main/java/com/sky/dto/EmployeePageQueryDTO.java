package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 员工分页查询数据传递对象
 */
@Data
public class EmployeePageQueryDTO implements Serializable {

    //员工姓名
    private String name;

    //页码
    private Integer page;

    //每页显示记录数
    private Integer pageSize;

}
