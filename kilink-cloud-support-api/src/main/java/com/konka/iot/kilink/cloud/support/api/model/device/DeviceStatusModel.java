package com.konka.iot.kilink.cloud.support.api.model.device;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-26 17:29
 * @Description 设备模型
 */
@Data
public class DeviceStatusModel implements Serializable {
    private String id;
    private String mac;
    private String sn;
    private String name;
    private String product_id;
    private Boolean is_active;
    private Boolean is_online;
}
