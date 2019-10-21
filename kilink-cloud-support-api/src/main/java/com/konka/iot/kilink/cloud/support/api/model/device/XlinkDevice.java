package com.konka.iot.kilink.cloud.support.api.model.device;

import lombok.Data;

import java.io.Serializable;

@Data
public class XlinkDevice implements Serializable {

  private String id;
  private String name;
  private String product_id;
  private String is_online;
  private String is_active;
  private String role;
  private String last_login;
  private String subscribe_date;
  private String firmware_mod;
  private String active_code;
  private String active_date;
  private String groups;
  private String mcu_version;
  private String firmware_version;
  private String source;
  private String mac;
  private String mcu_mod;
  private String access_key;
  private String authority;
  private String authorize_code;

  @Override
  public String toString( ) {
    return "XlinkDevice{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", product_id='" + product_id + '\'' +
            ", is_online='" + is_online + '\'' +
            ", is_active='" + is_active + '\'' +
            ", role='" + role + '\'' +
            ", last_login='" + last_login + '\'' +
            ", subscribe_date='" + subscribe_date + '\'' +
            ", firmware_mod='" + firmware_mod + '\'' +
            ", active_code='" + active_code + '\'' +
            ", active_date='" + active_date + '\'' +
            ", groups='" + groups + '\'' +
            ", mcu_version='" + mcu_version + '\'' +
            ", firmware_version='" + firmware_version + '\'' +
            ", source='" + source + '\'' +
            ", mac='" + mac + '\'' +
            ", mcu_mod='" + mcu_mod + '\'' +
            ", access_key='" + access_key + '\'' +
            ", authority='" + authority + '\'' +
            ", authorize_code='" + authorize_code + '\'' +
            '}';
  }
}
