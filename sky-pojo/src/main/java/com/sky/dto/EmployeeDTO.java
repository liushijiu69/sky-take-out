package com.sky.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 员工数据传递对象
 */
@Data
public class EmployeeDTO implements Serializable {

    private Long id;

    @NotBlank
    @Size(max = 32)
    private String username;

    @NotBlank
    @Size(max = 32)
    private String name;

    @NotBlank
    @Pattern(regexp = "\\d{11}")
    private String phone;

    @NotBlank
    @Pattern(regexp = "[01]")
    private String sex;

    @NotBlank
    @Pattern(regexp = "\\d{18}")
    private String idNumber;

}
