package com.konka.iot.interior.device.gateway.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-11-21 18:24
 * @Description TODO
 */
@Data
public class ServicesInfo implements Serializable {
    private String st;
    private String sid;
    private String ts;
    private Map<String, Object> data;
}
