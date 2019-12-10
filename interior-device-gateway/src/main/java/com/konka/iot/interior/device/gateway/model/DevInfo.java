package com.konka.iot.interior.device.gateway.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-11-21 18:17
 * @Description TODO
 */
@Data
public class DevInfo implements Serializable {
    private String model;
    private String devType;
    private String manu;
    private String hiv;
    private Integer protType;
    private String prodId;
}
