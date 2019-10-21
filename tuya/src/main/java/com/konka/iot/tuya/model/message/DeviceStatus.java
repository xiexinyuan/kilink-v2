package com.konka.iot.tuya.model.message;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-12 17:22
 * @Description 上报的设备数据实体
 */
@Data
public class DeviceStatus implements Serializable {
    private String code;
    private Object value;
    private Long t;

    @Override
    public String toString( ) {
        return "DeviceStatus{" +
                "code='" + code + '\'' +
                ", value=" + value +
                ", t=" + t +
                '}';
    }
}
