package com.konka.iot.tuya.model.message;

import lombok.Data;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-24 18:29
 * @Description 设备其他事件数据上报模型
 */
@Data
public class DeviceOtherMessageModel {
    private String devId;
    private String productKey;
    private String bizCode;
    private BizData bizData;

    @Override
    public String toString( ) {
        return "DeviceOtherMessageModel{" +
                "devId='" + devId + '\'' +
                ", productKey='" + productKey + '\'' +
                ", bizCode='" + bizCode + '\'' +
                ", bizData=" + bizData +
                '}';
    }
}
