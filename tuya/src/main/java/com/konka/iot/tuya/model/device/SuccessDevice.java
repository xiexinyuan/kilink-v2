package com.konka.iot.tuya.model.device;

import lombok.Data;

import java.io.Serializable;

/**
 * 配网成功设备列表
 */
@Data
public class SuccessDevice implements Serializable {
    private String id;
    private String productId;
    private String name;
    private Boolean isOnline;
    private String lon;
    private String lat;
    private String ip;
}
