package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单取消数据传递对象
 */
@Data
public class OrdersCancelDTO implements Serializable {

    private Long id;
    //订单取消原因
    private String cancelReason;

}
