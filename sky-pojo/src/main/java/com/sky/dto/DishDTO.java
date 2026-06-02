package com.sky.dto;

import com.sky.entity.DishFlavor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜品数据传递对象
 */
@Data
public class DishDTO implements Serializable {

    private Long id;

    @NotBlank
    @Size(max = 32)
    private String name;

    @NotNull
    private Long categoryId;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    private String image;

    private String description;

    private Integer status;

    private List<DishFlavor> flavors = new ArrayList<>();

}
