package com.konka.iot.tuya.model.device;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-11 10:53
 * @Description 设备token响应实体
 */
@Data
public class DeviceTokenRespResult implements Serializable {
    // 当前可用区 当前支持 AY EU US
    private String region;
    // 配网token
    private String token;
    // 秘钥
    private String secret;
}
