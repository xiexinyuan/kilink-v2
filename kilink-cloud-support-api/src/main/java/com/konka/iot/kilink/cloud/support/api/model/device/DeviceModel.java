package com.konka.iot.kilink.cloud.support.api.model.device;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-25 14:18
 * @Description TODO
 */
@Data
public class DeviceModel implements Serializable {
    private String id;
    //必填
    private String mac;
    //非必须
    private String sn;
    //非必须
    private String name;
}
