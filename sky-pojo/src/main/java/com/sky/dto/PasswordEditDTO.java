package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 密码修改数据传递对象
 */
@Data
public class PasswordEditDTO implements Serializable {

    //员工id
    private Long empId;

    //旧密码
    private String oldPassword;

    //新密码
    private String newPassword;

}
