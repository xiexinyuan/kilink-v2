package com.konka.iot.tuya.core.service.gateway.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.konka.iot.baseframe.common.core.service.BaseService;
import com.konka.iot.baseframe.common.exception.DataCheckException;
import com.konka.iot.baseframe.common.utils.RedisUtil;
import com.konka.iot.baseframe.common.utils.StringUtil;
import com.konka.iot.kilink.cloud.support.api.model.device.DeviceStatusModel;
import com.konka.iot.kilink.cloud.support.api.service.device.DeviceService;
import com.konka.iot.tuya.config.GatewayConfig;
import com.konka.iot.tuya.core.service.gateway.GatewayService;
import com.konka.iot.tuya.mqtt.DeviceMqttClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-25 12:17
 * @Description TODO
 */

@Service
public class GatewayServiceImpl extends BaseService implements GatewayService {
    @Autowired
    private GatewayConfig gatewayConfig;

    @Reference
    private DeviceService deviceService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private DeviceMqttClientService deviceMqttClientService;

    // 全局保存网关设备的Mac地址用于激活后区分网关设备
    public static String gatewaiMac;

    @Override
    public void init( ){
        logger.info("初始化虚拟网关开始");
        try {
            String g_deviceId = (String) redisUtil.get(gatewayConfig.getGateway_device_id());
            if(g_deviceId != null){// 网关设备已经注册激活  但是下线了 重新上线
                // 网关设备上线
                logger.info("网关设备{}已经注册激活, 重新上线网关设备及其子设备", g_deviceId);
                deviceMqttClientService.onlineDeviceSync(g_deviceId);
                // 查询网关下的子设备并上线
                onlineSubDevice(g_deviceId);
            }else{
                // 网关设备激活
                gatewaiMac = StringUtil.randomMac();
                deviceMqttClientService.activeDeviceSync(gatewayConfig.getProductId(), gatewaiMac);
            }
        } catch (DataCheckException e) {
            logger.error("初始化虚拟网关异常：{}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("初始化虚拟网关异常：{}", e.getMessage());
            e.printStackTrace();
        }

        logger.info("初始化虚拟网关结束");

    }

    @Override
    public void offline( ) throws Exception {
        String g_deviceId = (String) redisUtil.get(gatewayConfig.getGateway_device_id());
        if(g_deviceId != null){
            deviceMqttClientService.offlineDevice(g_deviceId);
        }else {
            throw new DataCheckException("获取网关设备ID为空");
        }
    }

    // 异步上线网关子设备
    @Async("taskExecutor")
    public void onlineSubDevice(String gatewayid){
        logger.info("分页获取网关子设备列表 每次取50条记录");
        //分页获取网关子设备列表 每次取50条记录
        AtomicInteger pageNo = new AtomicInteger(0);
        int pageSize = 50;
        while (true){
            try {
                List<DeviceStatusModel> devices =  deviceService.listGatewayDevice(gatewayid, pageNo, pageSize);
                logger.info("第{}次查询,共查出{}个设备", pageNo.intValue() + 1, devices.size());
                // 当获取的设备列表为空时停止查询
                if(devices == null || devices.isEmpty()){
                    logger.info("第{}次查询获取网关{}的子设备为空, 终止上线操作" ,pageNo.intValue() + 1, gatewayid);
                    // 退出循环
                    break;
                }else {
                    // 遍历网关子设备列表 并上线
                    for (DeviceStatusModel device: devices) {
                        logger.info("网关{}下的子设备{}开始上线" ,gatewayid, device.getId());
                        deviceMqttClientService.onlineDeviceSync(device.getId());
                    }
                }
                pageNo.incrementAndGet();
                // 暂停100 ms
                Thread.sleep(100);
            } catch (DataCheckException e){
                logger.error("获取网关{}下的子设备并上线失败：{}" ,gatewayid, e.getMessage());
                e.printStackTrace();
                // 退出循环
                break;
            } catch (Exception e) {
                logger.error("获取网关{}下的子设备并上线失败：{}" ,gatewayid, e.getMessage());
                e.printStackTrace();
                // 退出循环
                break;
            }
        }

    }
}
