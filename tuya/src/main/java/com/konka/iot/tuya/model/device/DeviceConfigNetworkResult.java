package com.konka.iot.tuya.model.device;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-11 11:14
 * @Description 设备配网请求结果
 */
@Data
public class DeviceConfigNetworkResult implements Serializable{
    private List<SuccessDevice> successDevices;
    private List<ErrorDevice> errorDevices;
}

