package com.konka.iot.tuya.model.device;

import lombok.Data;

import java.io.Serializable;

/**
 * 配网失败设备列表
 */
@Data
public class ErrorDevice implements Serializable {
    private String id;
    private String errorCode;
    private String errorMsg;
    private String name;
}
