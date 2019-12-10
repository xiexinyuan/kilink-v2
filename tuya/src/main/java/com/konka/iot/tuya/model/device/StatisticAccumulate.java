package com.konka.iot.tuya.model.device;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-31 16:59
 * @Description TODO
 */
@Data
public class StatisticAccumulate implements Serializable {
    private String thisDay;
    private String sum;
    private Map<String, Object> years;
}
