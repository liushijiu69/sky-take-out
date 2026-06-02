package com.sky.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {

    /** 参数校验（通用） */
    public static class Param {
        public static final String TOO_LONG_OR_BLANK = "参数不符合要求: 太长或为空!";
        public static final String REQUIRED = "参数不符合要求: 必填参数为空或参数有问题!";
        public static final String ILLEGAL = "参数不符合要求: 非法参数!";
        public static final String ALREADY_EXISTS = "参数不符合要求: 已存在!";
        public static final String NOT_IN_RANGE = "->参数不符合要求: 参数不在指定范围!";
        public static final String FILE_NO_NAME = "参数不符合要求: 上传的文件没有filename字段!";
    }

    /** 服务器通用 */
    public static class Server {
        public static final String RESOURCE_NOT_FOUND = "访问的资源不存在!";
        public static final String ERROR = "服务器异常!";
        public static final String UNKNOWN = "未知错误";
        public static final String UPLOAD_FAILED = "文件上传失败";
    }

    /** 员工模块 */
    public static class Employee {
        public static final String NOT_FOUND = "账号不存在";
        public static final String PASSWORD_ERROR = "密码错误";
        public static final String LOCKED = "账号被锁定";
        public static final String NOT_LOGIN = "用户未登录";
    }

    /** 分类模块 */
    public static class Category {
        public static final String LINKED_BY_DISH = "当前分类关联了菜品,不能删除";
        public static final String LINKED_BY_SETMEAL = "当前分类关联了套餐,不能删除";
    }

    /** 菜品模块 */
    public static class Dish {
        public static final String ON_SALE_CANNOT_DELETE = "起售中的菜品不能删除";
        public static final String LINKED_BY_SETMEAL = "当前菜品关联了套餐,不能删除";
    }

    /** 套餐模块 */
    public static class Setmeal {
        public static final String ON_SALE_CANNOT_DELETE = "起售中的套餐不能删除";
        public static final String ENABLE_FAILED = "套餐内包含未启售菜品，无法启售";
    }

    /** 订单模块 */
    public static class Order {
        public static final String STATUS_ERROR = "订单状态错误";
        public static final String NOT_FOUND = "订单不存在";
        public static final String CART_EMPTY = "购物车数据为空，不能下单";
        public static final String ADDRESS_EMPTY = "用户地址为空，不能下单";
    }

    /** 登录相关 */
    public static class Login {
        public static final String FAILED = "登录失败!";
        public static final String PASSWORD_EDIT_FAILED = "密码修改失败";
    }
}
