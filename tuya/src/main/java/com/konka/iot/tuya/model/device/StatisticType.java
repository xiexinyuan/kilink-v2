package com.konka.iot.tuya.model.device;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-31 16:56
 * @Description TODO
 */
@Data
public class StatisticType implements Serializable {
    private String code;
    private String stat_type;
}
