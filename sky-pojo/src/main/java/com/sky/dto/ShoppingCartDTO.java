package com.sky.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 购物车数据传递对象
 */
@Data
public class ShoppingCartDTO implements Serializable {

    private Long dishId;
    private Long setmealId;
    private String dishFlavor;

}
