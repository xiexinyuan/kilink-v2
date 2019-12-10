package com.konka.iot.tuya.model.message;

import lombok.Data;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-24 18:31
 * @Description TODO
 */
@Data
public class BizData {
    private Long time;
    private String devId;
    private String dpId;
    private String uid;
    private String uuid;
    private String token;

    @Override
    public String toString( ) {
        return "BizData{" +
                "time=" + time +
                ", devId='" + devId + '\'' +
                ", dpId='" + dpId + '\'' +
                ", uid='" + uid + '\'' +
                ", uuid='" + uuid + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
