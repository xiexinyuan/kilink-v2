package com.konka.iot.interior.device.gateway.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-11-21 18:09
 * @Description TODO
 */
@Data
public class DeviceModel implements Serializable {
    private String devId;
    private String devName;
    private DevInfo devInfo;
    private List<ServicesInfo> services;
}
