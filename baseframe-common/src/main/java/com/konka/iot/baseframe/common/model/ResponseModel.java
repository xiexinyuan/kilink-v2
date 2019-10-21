package com.konka.iot.baseframe.common.model;

import lombok.Data;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-11 09:59
 * @Description TODO
 */
@Data
public class ResponseModel<T> extends BaseResponse {

    private T data;

    public ResponseModel(){
        super();
    }

    public ResponseModel(T data){
        super();
        this.data = data;
    }

    public ResponseModel(int code, boolean success, String msg){
        super(code, success, msg);
    }

    public ResponseModel(int code, boolean success, String msg, T data){
        super(code, success, msg);
        this.data = data;
    }

    @Override
    public String toString( ) {
        return "ResponseModel{" +
                "code =" + getCode() +
                ", success=" + getSuccess() +
                ", msg=" + getMsg() +
                ", data=" + data +
                '}';
    }
}
