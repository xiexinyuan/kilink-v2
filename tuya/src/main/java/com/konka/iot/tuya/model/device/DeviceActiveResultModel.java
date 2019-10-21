package com.konka.iot.tuya.model.device;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-16 17:03
 * @Description TODO
 */
@Data
public class DeviceActiveResultModel implements Serializable {
    private String id;
    private Integer code;
    private String message;
    private Boolean isActive;
}
