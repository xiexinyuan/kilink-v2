package com.konka.iot.tuya.model.message;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-12 17:13
 * @Description 接受涂鸦webhook回调消息实体
 */
@Data
public class ReceiveDataModel implements Serializable {
    // 协议号
    private Integer protocol;
    // 通讯协议版本号
    private String pv;
    // 时间戳
    private Long t;
    // 数据体
    private String data;

    @Override
    public String toString( ) {
        return "ReceiveDataModel{" +
                "protocol=" + protocol +
                ", pv='" + pv + '\'' +
                ", t=" + t +
                ", data=" + data +
                '}';
    }
}
