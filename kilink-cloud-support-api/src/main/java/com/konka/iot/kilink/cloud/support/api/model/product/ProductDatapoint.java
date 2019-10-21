package com.konka.iot.kilink.cloud.support.api.model.product;

import lombok.Data;

import java.io.Serializable;


@Data
public class ProductDatapoint implements Serializable {

  /**
   * 数据端点ID
   */
  private String id;
  /**
   * 数据端点名称
   */
  private String name;
  /**
   * 字段名称
   */
  private String field_name;
  /**
   * 端点类型
   * bool/byte/short/int/string
   */
  private String type;
  /**
   * 数据端点索引
   */
  private Integer index;
  /**
   * 描述
   */
  private String description;
  /**
   * 符号
   */
  private String symbol;
  /**
   * 是否收集端点数据
   */
  private Boolean is_collect;
  /**
   * 数据端点取值范围,最小值
   */
  private Integer min;
  /**
   * 数据端点取值范围,最大值
   */
  private Integer max;
  /**
   * 数据端点是否可读
   */
  private Boolean is_read;
  /**
   * 数据端点是否可写
   */
  private Boolean is_write;

  /**
   * 数据端点默认值
   */
  private Object default_value;
  /**
   * 动态计算数据端点表达式
   */
  private String expression;
  /**
   * 数据来源类型
   * 1:设备上报, 2:公式计算, 3:⽤户设置
   */
  private Integer source;

}
