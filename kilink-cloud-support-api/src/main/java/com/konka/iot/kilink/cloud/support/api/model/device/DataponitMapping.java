package com.konka.iot.kilink.cloud.support.api.model.device;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-30 18:04
 * @Description 数据端点映射表实体
 */
@Data
public class DataponitMapping implements Serializable {
    private int id;
    private String tProductId;
    private String tDatapointCode;
    private int kDatapointIndex;
    private String kDatapointCode;
    private int kDatapointType;
}
