package com.konka.iot.baseframe.common.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-11 09:50
 * @Description 基础响应实体
 */
@Data
@Builder
public class BaseResponse {
    /**
     * 响应码
     */
    private Integer code;

    /**
     * 操作是否成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String msg;


    public BaseResponse() {
        this.code = 200;
        this.success = true;
        this.msg = "操作成功";
    }

    public BaseResponse(int code, Boolean success, String msg) {
        this.code = code;
        this.success = success;
        this.msg = msg;
    }

    @Override
    public String toString( ) {
        return "BaseResponse{" +
                "code=" + code +
                ", success=" + success +
                ", msg='" + msg + '\'' +
                '}';
    }
}
