package com.konka.iot.kilink.cloud.support.api.model.product;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-20 15:53
 * @Description 产品映射表实体
 */
@Data
public class ProductMapping implements Serializable {
    private int id;
    private String kProductId;
    private String tProductId;
}
