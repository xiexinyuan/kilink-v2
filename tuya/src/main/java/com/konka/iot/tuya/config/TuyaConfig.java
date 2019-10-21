package com.konka.iot.tuya.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @Author xiexinyuan
 * @Date 2019-09-09 17:13
 * @Description 涂鸦参数配置类
 **/
@Data
@Lazy
@Component
@ConfigurationProperties(prefix = "tuya")
public class TuyaConfig {

    private String  accessId;
    private String  accessKey;
    private String  schema;
    private String  country;
    private String  countryCode;
    private String  cn_url;
    private String  us_url;
    private String  eu_url;
}
