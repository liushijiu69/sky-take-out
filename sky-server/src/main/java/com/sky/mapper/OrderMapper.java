package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
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
    Orders selectByNumber(String orderNumber);

    void update(Orders orders);

    /**
     * 通过订单号和订单状态更新订单
     * @param number 订单号
     * @param order 订单信息
     * @param status 订单状态
     * @return 影响的行数，0 表示未匹配到订单（无需处理）
     */
    Integer updateByNumberAndStatus(@Param("orderNumber") String number, @Param("status") Integer status, @Param("order") Orders order);

    List<Orders> pageQueryByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    List<Orders> pageQueryByCondition(OrdersPageQueryDTO dto);

    @Select("select count(*) from orders where status = #{status}")
    Integer countByStatus(Integer status);
}
