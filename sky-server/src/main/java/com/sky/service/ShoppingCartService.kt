package com.sky.service

import com.sky.dto.ShoppingCartDTO
import com.sky.vo.ShoppingCartVO

interface ShoppingCartService {

    fun addShoppingCart(shoppingCartDTO: ShoppingCartDTO)
    fun list(userId: Long): List<ShoppingCartVO>
    fun cleanShoppingCart()
    fun deleteShoppingCart(shoppingCartDTO: ShoppingCartDTO)
}