package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单拒绝数据传递对象
 */
@Data
public class OrdersRejectionDTO implements Serializable {

    private Long id;

    //订单拒绝原因
    private String rejectionReason;

}
