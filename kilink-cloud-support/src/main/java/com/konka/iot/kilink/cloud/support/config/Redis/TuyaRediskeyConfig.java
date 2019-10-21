package com.konka.iot.kilink.cloud.support.config.Redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-11 16:00
 * @Description TODO
 */
@Data
@Lazy
@Component
@ConfigurationProperties(prefix="redis-prefix.tuya")
public class TuyaRediskeyConfig {
    private String product_mapping_prefix;
    private String device_mapping_prefix;
    private String datapoint_mapping_prefix;
}
