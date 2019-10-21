package com.konka.iot.tuya;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import com.konka.iot.baseframe.common.utils.SpringContextUtil;
import com.konka.iot.tuya.core.service.gateway.GatewayService;
import com.konka.iot.tuya.listener.ApplicationServiceCloseListener;
import com.konka.iot.tuya.mqtt.DeviceMqttClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@EnableDubbo
@EnableCaching
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TuyaApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TuyaApplication.class);

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(TuyaApplication.class, args);
        // 添加监听器
        SpringContextUtil.setApplicationContext(applicationContext);
        ((ConfigurableApplicationContext) applicationContext).addApplicationListener(SpringContextUtil.getBean(ApplicationServiceCloseListener.class));
    }

    @Autowired
    private DeviceMqttClientService deviceMqttClientService;

    @Autowired
    private GatewayService gatewayService;

    /**
     * 监听服务启动状态
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) {
        // 初始化 deviceMqttClientService 客户端
        deviceMqttClientService.init();
        // 初始化虚拟网关
        gatewayService.init();
    }
}