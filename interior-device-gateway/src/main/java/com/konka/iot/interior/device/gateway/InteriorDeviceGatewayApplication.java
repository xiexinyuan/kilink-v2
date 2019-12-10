package com.konka.iot.interior.device.gateway;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import com.konka.iot.interior.device.gateway.core.service.gateway.GatewayService;
import com.konka.iot.interior.device.gateway.mqtt.DeviceMqttClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@EnableDubbo
@EnableCaching
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class InteriorDeviceGatewayApplication implements CommandLineRunner {


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
