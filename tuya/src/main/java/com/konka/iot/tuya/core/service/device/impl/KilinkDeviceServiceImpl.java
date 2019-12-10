package com.konka.iot.tuya.core.service.device.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.konka.iot.baseframe.common.config.ErrorCodeEnum;
import com.konka.iot.baseframe.common.core.service.BaseService;
import com.konka.iot.baseframe.common.exception.DataCheckException;
import com.konka.iot.baseframe.common.utils.RedisUtil;
import com.konka.iot.baseframe.common.utils.StringUtil;
import com.konka.iot.kilink.cloud.support.api.model.device.DeviceMapping;
import com.konka.iot.kilink.cloud.support.api.model.product.ProductMapping;
import com.konka.iot.kilink.cloud.support.api.service.device.DeviceService;
import com.konka.iot.kilink.cloud.support.api.service.product.ProductService;
import com.konka.iot.tuya.config.GatewayConfig;
import com.konka.iot.tuya.core.service.device.KilinkDeviceService;
import com.konka.iot.tuya.model.device.DeviceActiveResultModel;
import com.konka.iot.tuya.model.device.DeviceAddModel;
import com.konka.iot.tuya.mqtt.DeviceMqttCallback;
import com.konka.iot.tuya.mqtt.DeviceMqttClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-20 10:45
 * @Description TODO
 */
@Service
public class KilinkDeviceServiceImpl extends BaseService implements KilinkDeviceService {

    @Reference
    private DeviceService deviceService;

    @Reference
    private ProductService productService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private GatewayConfig gatewayConfig;

    @Autowired
    private DeviceMqttClientService deviceMqttClientService;

    @Autowired
    private DeviceMqttCallback deviceMqttCallback;

    // 存放需要传递给mqtt回调的参数, 利用阻塞队列保证激活一个设备后再激活另一个设备
    private static volatile BlockingQueue<Map<String, Object>> paramsQueue = new ArrayBlockingQueue(1);

    private static volatile BlockingQueue<DeviceActiveResultModel> resultModelsQueue = new ArrayBlockingQueue(1);

    @Override
    public void add(DeviceAddModel deviceAddModel) throws Exception {
        //获取当前网关的设备ID
        String g_deviceId = (String) redisUtil.get(gatewayConfig.getGateway_device_id());

        if (g_deviceId == null || "".equals(g_deviceId)) {
            throw new DataCheckException(ErrorCodeEnum.GATEWAY_NOT_EXIST.getCode(), ErrorCodeEnum.GATEWAY_NOT_EXIST.getMessage());
        }
        if (deviceAddModel == null) {
            throw new DataCheckException(ErrorCodeEnum.DEVICE_NETWORK_INFO_NULL.getCode(), ErrorCodeEnum.DEVICE_NETWORK_INFO_NULL.getMessage());
        }
        // 保存第三方产品ID
        Set<String> t_pids = new HashSet<>(1);

        t_pids.add(deviceAddModel.getProductId());

        // 获取产品映射集合
        List<ProductMapping> productMappings = productService.getKilinkProductIdBatch(t_pids);

        // 过滤未配置产品映射关系的设备
        if (productMappings == null || productMappings.isEmpty()) {
            throw new DataCheckException(ErrorCodeEnum.PRODUCT_MAPPING_INFO_NULL.getCode(), ErrorCodeEnum.PRODUCT_MAPPING_INFO_NULL.getMessage());

        } else if (t_pids.size() > productMappings.size()) {
            checkProductConfigExist(productMappings, t_pids);
        }
        dealActiveDevice(g_deviceId, deviceAddModel, productMappings.get(0));
    }

    @Override
    public DeviceActiveResultModel getResultModel() throws Exception{
        logger.info("==================获取阻塞队列中的值==================");
        return resultModelsQueue.take();
    }
    @Override
    public void clearQueue(){
        paramsQueue.clear();
        resultModelsQueue.clear();
    }

    @Override
    public void saveActiveRecord(String deviceId, int code, String message, boolean isActive){
        DeviceActiveResultModel resultModel = new DeviceActiveResultModel();
        resultModel.setId(deviceId);
        resultModel.setCode(code);
        resultModel.setMessage(message);
        resultModel.setIsActive(isActive);
        try {
            resultModelsQueue.put(resultModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void checkProductConfigExist(List<ProductMapping> productMappings, Set<String> t_pids) throws DataCheckException {
        // 保存配置表中的第三方的产品ID
        Set<String> t_pids_cof = new HashSet<>(productMappings.size());

        for (ProductMapping productMapping : productMappings) {

            t_pids_cof.add(productMapping.getTProductId());
        }
        // 求两个集合的差集  找出未配置产品映射的设备
        Set<String> resultSet = new HashSet<>();

        resultSet.addAll(t_pids);
        resultSet.removeAll(t_pids_cof);

        StringBuffer msg = new StringBuffer();
        for (String a : resultSet) {
            msg.append("【" + a + "】");
        }
        throw new DataCheckException(ErrorCodeEnum.ERROR.getCode(), "产品" + msg.toString() + "映射配置为空");
    }

    // 单个设备
    private void dealActiveDevice(String g_deviceId, DeviceAddModel deviceAddModel, ProductMapping productMapping) throws Exception {
        // 已经激活的设备, 只需要上线并且绑定与当前用户的关系即可 无需重新激活
        //1、获取设备映射关系
        List<String> deviceIds = new ArrayList<>(1);
        deviceIds.add(deviceAddModel.getDeviceId());
        List<DeviceMapping> deviceMappings = deviceService.findDeviceMapping(deviceIds);
        if (!deviceMappings.isEmpty()) {
            DeviceMapping deviceMapping = deviceMappings.get(0);
            logger.info("设备已经激活, 重新上线并绑定用户关系即可：{}", deviceAddModel);
            // 已经激活的设备直接重新上线并重置和用户的绑定关系
            if (deviceAddModel.getDeviceId().equals(deviceMapping.getTDeviceId())) {
                // 重新上线
                deviceMqttClientService.onlineDeviceSync(deviceMapping.getKDeviceId());
                if (deviceAddModel.getProductId().equals(productMapping.getTProductId())) {
                    // 重新绑定和用户之间的关系
                    try {
                        deviceService.bindByQrcode(deviceAddModel.getUserId(), productMapping.getKProductId(), deviceMapping.getKDeviceId(), deviceAddModel.getAccessToken());
                        saveActiveRecord(deviceMapping.getKDeviceId(), ErrorCodeEnum.SUCCESS.getCode(), "绑定成功", true);
                    } catch (DataCheckException e) {
                        logger.error(e.getMessage());
                        saveActiveRecord(deviceMapping.getKDeviceId(), ErrorCodeEnum.DEVICE_BIND_USER_ERROR.getCode(), e.getMessage(), false);
                        e.printStackTrace();
                    } catch (Exception e) {
                        logger.error("设备绑定异常： {}", e.getMessage());
                        saveActiveRecord(deviceMapping.getKDeviceId(), ErrorCodeEnum.DEVICE_BIND_USER_ERROR.getCode(), ErrorCodeEnum.DEVICE_BIND_USER_ERROR.getMessage(), false);
                        e.printStackTrace();
                    }
                }
            }
        } else {
            logger.info("设备{}未激活, 正常走激活绑定流程", deviceAddModel);
            //当第三方产品找到对应的kilink产品时， 1、激活该设备 绑定到网关设备下
            if (deviceAddModel.getProductId().equals(productMapping.getTProductId())) {
                activeDevice(productMapping, g_deviceId, deviceAddModel);
            }
        }
    }


    private void activeDevice(ProductMapping productMapping, String g_deviceId, DeviceAddModel deviceAddModel) throws Exception {
        // 将用户id 传递给mqtt回调处理类
        Map<String, Object> params = new HashMap<>(2);
        String mac = StringUtil.randomMac();
        params.put("mac", mac);
        params.put("deviceAddModel", deviceAddModel);
        params.put("productMapping", productMapping);
        // 将参数放入阻塞队列
        paramsQueue.put(params);
        // 初始化mqtt回调的阻塞队列
        deviceMqttCallback.setParamsQueue(paramsQueue);
        // 激活设备
        deviceMqttClientService.activeDeviceSync(productMapping.getKProductId(), mac, g_deviceId);
    }

}

