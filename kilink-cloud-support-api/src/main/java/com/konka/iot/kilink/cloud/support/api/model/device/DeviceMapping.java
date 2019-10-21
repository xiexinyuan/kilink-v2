package com.konka.iot.kilink.cloud.support.api.model.device;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-30 18:02
 * @Description 设备映射表实体
 */
@Data
public class DeviceMapping implements Serializable {
    private int id;
    private String kDeviceId;
    private String tDeviceId;
}
