package com.konka.iot.interior.device.gateway.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-11-22 16:39
 * @Description TODO
 */
@Data
public class InteriorDeviceCommand implements Serializable {
      private String timestamp;
      private String devId;
      private String prodId;
      private String sid;
      private Map<String, Object> data;
}
