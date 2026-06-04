package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    List<ShoppingCart> selectByShoppingCart(ShoppingCart shoppingCart);

    void updateNumberById(ShoppingCart shoppingCart);

    void insert(ShoppingCart shoppingCart);

    void insertBatch(List<ShoppingCart> shoppingCartList);

    void delete(ShoppingCart shoppingCart);

}
