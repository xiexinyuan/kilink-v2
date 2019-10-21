package com.konka.iot.baseframe.common.exception;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-24 19:49
 * @Description 自定义数据校验异常
 */
public class DataCheckException extends Exception {

    private int code;
    private String message;

    public DataCheckException(String message) {
        this.code = 500;
        this.message = message;
    }

    public DataCheckException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode( ) {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage( ) {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
