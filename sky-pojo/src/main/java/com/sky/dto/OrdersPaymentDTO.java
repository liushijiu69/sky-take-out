package com.sky.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 订单支付数据传递对象
 */
@Data
public class OrdersPaymentDTO implements Serializable {
    //订单号
    private String orderNumber;

    //付款方式
    private Integer payMethod;

}
