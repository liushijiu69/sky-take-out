package com.sky.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "员工登录时传递的数据模型")
public class EmployeeLoginDTO implements Serializable {

    @NotBlank
    @Size(max = 32)
    @Schema(description = "用户名")
    private String username;

    @NotBlank
    @Size(max = 64)
    @Schema(description = "密码")
    private String password;

}
