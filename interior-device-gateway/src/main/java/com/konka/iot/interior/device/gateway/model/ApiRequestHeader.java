package com.konka.iot.interior.device.gateway.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-11-21 16:28
 * @Description TODO
 */
@Data
public class ApiRequestHeader implements Serializable {
     private String namespace;
     private String thirdPartyId;
     private String granteeId;
     private String messageId;
     private Long timestamp;

    @Override
    public String toString( ) {
        return "ApiRequestHeader{" +
                "namespace='" + namespace + '\'' +
                ", thirdPartyId='" + thirdPartyId + '\'' +
                ", granteeId='" + granteeId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
