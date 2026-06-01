package com.sky.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {
    /**
     * 参数不符合要求
     */
    public static class ParamIllegal {
        public static final String TO_LONG_OR_BLANK = "参数不符合要求: 太长或为空!";
        public static final String PARAMETERS_ILLEGAL = "参数不符合要求: 必填参数为空或参数有问题!";
        public static final String ALREADY_EXISTED = "参数不符合要求: 已存在!";
        public static final String NOT_IN_RANGE = "->参数不符合要求: 参数不在指定范围!";
        public static final String FILE_HAS_NO_ORIGINAL_NAME = "参数不符合要求: 上传的文件没有filename字段!";
    }
    public static class ServerError {
        public static final String RESOURCE_NOT_FOUND = "访问的资源不存在!";
        public static final String SERVER_ERROR = "服务器异常!";
        public static final String UNKNOWN_ERROR = "未知错误";
        public static final String File_UPLOAD_ERROR = "文件上传失败";
    }
    public static class LoginError {
        public static final String PASSWORD_ERROR = "密码错误";
        public static final String ACCOUNT_NOT_FOUND = "账号不存在";
        public static final String ACCOUNT_LOCKED = "账号被锁定";
        public static final String USER_NOT_LOGIN = "用户未登录";
    }
    public static final String CATEGORY_BE_RELATED_BY_SETMEAL = "当前分类关联了套餐,不能删除";
    public static final String CATEGORY_BE_RELATED_BY_DISH = "当前分类关联了菜品,不能删除";
    public static final String SHOPPING_CART_IS_NULL = "购物车数据为空，不能下单";
    public static final String ADDRESS_BOOK_IS_NULL = "用户地址为空，不能下单";
    public static final String LOGIN_FAILED = "登录失败!";
    public static final String UPLOAD_FAILED = "文件上传失败";
    public static final String SETMEAL_ENABLE_FAILED = "套餐内包含未启售菜品，无法启售";
    public static final String PASSWORD_EDIT_FAILED = "密码修改失败";
    public static final String DISH_ON_SALE = "起售中的菜品不能删除";
    public static final String SETMEAL_ON_SALE = "起售中的套餐不能删除";
    public static final String DISH_BE_RELATED_BY_SETMEAL = "当前菜品关联了套餐,不能删除";
    public static final String ORDER_STATUS_ERROR = "订单状态错误";
    public static final String ORDER_NOT_FOUND = "订单不存在";



}
