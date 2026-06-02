package com.sky.dto;

import com.sky.entity.SetmealDish;
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
 * 套餐数据传递对象
 */
@Data
public class SetmealDTO implements Serializable {

    private Long id;

    @NotNull
    private Long categoryId;

    @NotBlank
    @Size(max = 32)
    private String name;

    @NotNull
    @Positive
    private BigDecimal price;

    private Integer status;

    @NotNull
    private String image;

    private String description;

    private List<SetmealDish> setmealDishes = new ArrayList<>();

}
