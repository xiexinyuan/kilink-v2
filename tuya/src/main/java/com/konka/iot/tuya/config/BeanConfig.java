package com.konka.iot.tuya.config;

import com.konka.iot.baseframe.common.utils.SpringContextUtil;
import com.konka.iot.baseframe.mqtt.config.MqttConfig;
import com.tuya.api.TuyaClient;
import com.tuya.api.model.enums.RegionEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-11 11:38
 * @Description TODO
 */
@Lazy
@Configuration
public class BeanConfig {

    @Autowired
    private TuyaConfig tuyaConfig;

    @Bean
    @Lazy
    @ConditionalOnClass(TuyaConfig.class)
    public TuyaClient tuyaClient(){
//        return new TuyaClient(tuyaConfig.getAccessId(), tuyaConfig.getAccessKey(), RegionEnum.CN, "授权码模式");
        return new TuyaClient(tuyaConfig.getAccessId(), tuyaConfig.getAccessKey(), RegionEnum.CN);
    }

    @Bean
    @Lazy
    @ConfigurationProperties(prefix = "mqtt")
    public MqttConfig mqttConfig() {
        return new MqttConfig();
    }

    @Bean
    @Lazy
    public SpringContextUtil springContextUtil(){
        return new SpringContextUtil();
    }

}
