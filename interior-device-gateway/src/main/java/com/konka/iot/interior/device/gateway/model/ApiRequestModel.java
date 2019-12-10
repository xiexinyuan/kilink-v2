package com.konka.iot.interior.device.gateway.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-11-21 16:26
 * @Description TODO
 */
@Data
public class ApiRequestModel<T> implements Serializable {
    private ApiRequestHeader header;
    private T payload;


    @Override
    public String toString( ) {
        return "ApiRequestModel{" +
                "header='" + header + '\'' +
                ", payload=" + payload +
                '}';
    }
}

