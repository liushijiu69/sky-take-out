package com.sky.exception;

/**
 * 已存在异常
 */
public class AlreadyExistedException extends BaseException {

    public AlreadyExistedException() {
    }

    public AlreadyExistedException(String msg) {
        super(msg);
    }

}
