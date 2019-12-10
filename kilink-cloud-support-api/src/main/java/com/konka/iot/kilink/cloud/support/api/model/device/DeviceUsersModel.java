package com.konka.iot.kilink.cloud.support.api.model.device;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-29 16:53
 * @Description 设备用户信息模型
 */
@Data
public class DeviceUsersModel implements Serializable {
    private Integer role;
    private String user_id;
    private String from_id;
    private String create_date;

    @Override
    public String toString( ) {
        return "DeviceUsersModel{" +
                "role=" + role +
                ", user_id='" + user_id + '\'' +
                ", from_id='" + from_id + '\'' +
                ", create_date=" + create_date +
                '}';
    }
}
