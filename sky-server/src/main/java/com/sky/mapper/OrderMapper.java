package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);

    Orders selectById(Long id);

    @Select("select id, number, status, user_id, address_book_id, order_time, " +
            "checkout_time, pay_method, pay_status, amount, remark, " +
            "phone, address, user_name, consignee, " +
            "cancel_reason, rejection_reason, cancel_time, " +
            "estimated_delivery_time, delivery_status, delivery_time, " +
            "pack_amount, tableware_number, tableware_status " +
            "from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    void update(Orders orders);

    List<Orders> pageQueryByUserId(@Param("userId") Long userId, @Param("status") Integer status);
}
