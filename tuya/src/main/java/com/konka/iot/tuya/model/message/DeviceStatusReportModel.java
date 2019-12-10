package com.konka.iot.tuya.model.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-12 17:16
 * @Description 设备上报状态接收实体
 */
@Data
public class DeviceStatusReportModel implements Serializable {
    // 设备ID
    private String devId;
    // 开发者平台定义产品对应的产品key
    private String productKey;
    // 数据ID
    private String dataId;
    // 状态列表
    List<DeviceStatus> status;

    @Override
    public String toString( ) {
        return "DeviceStatusReportModel{" +
                "devId='" + devId + '\'' +
                ", productKey='" + productKey + '\'' +
                ", dataId='" + dataId + '\'' +
                ", status=" + status +
                '}';
    }
}
