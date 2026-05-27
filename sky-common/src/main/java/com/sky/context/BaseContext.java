package com.sky.context;

/**
 * 基于 ThreadLocal 的上下文工具类，用于在当前线程中存储和传递当前操作用户的 ID
 */
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前用户 ID
     *
     * @param id 用户 ID
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 获取当前用户 ID
     *
     * @return 用户 ID
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    /**
     * 移除当前用户 ID（防止内存泄漏）
     */
    public static void removeCurrentId() {
        threadLocal.remove();
    }

}
