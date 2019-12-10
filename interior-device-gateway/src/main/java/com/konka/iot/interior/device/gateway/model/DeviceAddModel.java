package com.konka.iot.interior.device.gateway.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-29 14:11
 * @Description 添加网关设备请求模型
 */
@Data
public class DeviceAddModel implements Serializable {
    // 当前用户ID
    private String userId;
    // 设备ID
    private String deviceId;
    // 设备ID
    private String mac;
    // 当前用户token
    private String accessToken;
}
