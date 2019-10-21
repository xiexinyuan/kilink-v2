package com.konka.iot.tuya.model.device;

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
    // 产品ID
    private String productId;
    // 当前用户token
    private String accessToken;
}
