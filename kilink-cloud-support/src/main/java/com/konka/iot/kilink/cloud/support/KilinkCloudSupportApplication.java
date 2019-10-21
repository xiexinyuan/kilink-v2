package com.konka.iot.kilink.cloud.support;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@EnableDubbo
@EnableCaching
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class KilinkCloudSupportApplication {

    public static void main(String[] args) {
        SpringApplication.run(KilinkCloudSupportApplication.class, args);
    }

}
