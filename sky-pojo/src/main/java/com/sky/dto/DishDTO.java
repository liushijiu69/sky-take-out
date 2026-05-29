package com.sky.dto;

import com.sky.entity.DishFlavor;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜品数据传递对象
 */
@Data
public class DishDTO implements Serializable {
    // | 字段名          | 数据类型          | 说明      | 备注      |
    //| ------------ | ------------- | ------- | ------- |
    //| id           | bigint        | 主键      | 自增      |
    //| name         | varchar(32)   | 菜品名称    | 唯一      |
    //| category\_id | bigint        | 分类id    | 逻辑外键    |
    //| price        | decimal(10,2) | 菜品价格    | <br />  |
    //| image        | varchar(255)  | 图片路径    | <br />  |
    //| description  | varchar(255)  | 菜品描述    | <br />  |
    //| status       | int           | 售卖状态    | 1起售 0停售 |
    //| create\_time | datetime      | 创建时间    | <br />  |
    //| update\_time | datetime      | 最后修改时间  | <br />  |
    //| create\_user | bigint        | 创建人id   | <br />  |
    //| update\_user | bigint        | 最后修改人id | <br />  |

    private Long id;
    //菜品名称
    private String name;
    //菜品分类id
    private Long categoryId;
    //菜品价格
    private BigDecimal price;
    //图片
    private String image;
    //描述信息
    private String description;
    //0 停售 1 起售
    private Integer status;
    //口味
    private List<DishFlavor> flavors = new ArrayList<>();

}
