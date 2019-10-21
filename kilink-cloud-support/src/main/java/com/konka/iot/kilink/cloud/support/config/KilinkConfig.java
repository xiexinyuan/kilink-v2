package com.konka.iot.kilink.cloud.support.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-20 17:52
 * @Description kilink配置类
 */

@Data
@Lazy
@Component
@ConfigurationProperties(prefix="kilink")
public class KilinkConfig {
    private String cloud_url;
    private String admin_accesstoken_rediskey;
    private String kilink_access_key_id;
    private String kilink_access_key_secret;
    private String kilink_product_datapoint;
}
