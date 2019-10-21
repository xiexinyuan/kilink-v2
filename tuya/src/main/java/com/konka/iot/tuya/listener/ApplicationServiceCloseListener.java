package com.konka.iot.tuya.listener;

import com.konka.iot.baseframe.common.exception.DataCheckException;
import com.konka.iot.tuya.core.service.gateway.GatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-26 12:05
 * @Description 应用服务停止时的监听事件
 */
@Component
public class ApplicationServiceCloseListener implements ApplicationListener<ContextClosedEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GatewayService gatewayService;

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        logger.info("应用服务停止");
        try {
            logger.info("虚拟网关下线开始");
            gatewayService.offline();
            logger.info("虚拟网关下线结束");
        } catch (DataCheckException e){
            logger.error("虚拟网关下线异常：{}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("虚拟网关下线异常：{}", e.getMessage());
            e.printStackTrace();
        }
    }
}
