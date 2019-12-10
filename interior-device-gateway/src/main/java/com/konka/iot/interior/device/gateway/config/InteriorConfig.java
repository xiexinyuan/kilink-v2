package com.konka.iot.interior.device.gateway.config;

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
@ConfigurationProperties(prefix = "interior")
public class InteriorConfig {

    private String  appId;
    private String  appKey;
    private String  testUrl;
    private String  proUrl;
}
