package com.konka.iot.tuya.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @Author xiexinyuan
 * @Date 2019-09-10 11:25
 * @Description http配置类
 **/
@Data
@Lazy
@Component
@ConfigurationProperties(prefix="http")
public class HttpConfig {

    private Integer maxTotal;

    private Integer defaultMaxPerRoute;

    private Integer connectTimeout;

    private Integer connectionRequestTimeout;

    private Integer socketTimeout;

    private boolean staleConnectionCheckEnabled;
}
