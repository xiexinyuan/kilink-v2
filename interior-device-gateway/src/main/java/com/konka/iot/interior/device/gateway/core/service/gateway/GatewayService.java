package com.konka.iot.interior.device.gateway.core.service.gateway;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-25 12:17
 * @Description 网关服务
 */
public interface GatewayService {
    /**
     * 初始化虚拟网关
     * @throws Exception
     */
   void init( );

   /**
     * 虚拟网关下线
     * @throws Exception
     */
   void offline( ) throws Exception;
}
