package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    List<ShoppingCart> selectByShoppingCart(ShoppingCart shoppingCart);

    void updateNumberById( ShoppingCart shoppingCart);

    void insert(ShoppingCart shoppingCart);


    void delete(ShoppingCart shoppingCart);

}
