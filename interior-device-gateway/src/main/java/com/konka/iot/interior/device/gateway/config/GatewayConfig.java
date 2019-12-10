package com.konka.iot.interior.device.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-25 12:14
 * @Description 网关信息配置类
 */
@Data
@Lazy
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayConfig {
    private String productId;
    private String deviceName;
    private String gateway_device_id;
    private String product_mapping_prefix;
    private String device_mapping_prefix;
    private String datapoint_mapping_prefix;
}
