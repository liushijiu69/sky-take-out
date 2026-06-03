package com.sky.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单提交数据传递对象
 */
@Data
public class OrdersSubmitDTO implements Serializable {
    //地址簿id
    @NotNull
    private Long addressBookId;
    //付款方式
    @NotNull
    private int payMethod;
    //备注
    @NotNull
    @Size(max = 100)
    private String remark;
    //预计送达时间
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;
    //配送状态  1立即送出  0选择具体时间
    @NotNull
    private Integer deliveryStatus;
    //餐具数量
    @NotNull
    private Integer tablewareNumber;
    //餐具数量状态  1按餐量提供  0选择具体数量
    @NotNull
    private Integer tablewareStatus;
    //打包费
    @NotNull
    private Integer packAmount;
    //总金额
    @NotNull
    @Digits(integer = 8, fraction = 2, message = "金额格式不正确，最多支持10位整数和2位小数")
    private BigDecimal amount;
}
