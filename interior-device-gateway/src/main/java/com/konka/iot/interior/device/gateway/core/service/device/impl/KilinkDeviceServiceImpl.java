package com.konka.iot.interior.device.gateway.core.service.device.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.konka.iot.baseframe.common.config.ErrorCodeEnum;
import com.konka.iot.baseframe.common.core.service.BaseService;
import com.konka.iot.baseframe.common.exception.DataCheckException;
import com.konka.iot.baseframe.common.utils.RedisUtil;
import com.konka.iot.baseframe.common.utils.StringUtil;
import com.konka.iot.interior.device.gateway.config.GatewayConfig;
import com.konka.iot.interior.device.gateway.core.service.device.InteriorDeviceService;
import com.konka.iot.interior.device.gateway.core.service.device.KilinkDeviceService;
import com.konka.iot.interior.device.gateway.model.DeviceActiveResultModel;
import com.konka.iot.interior.device.gateway.model.DeviceAddModel;
import com.konka.iot.interior.device.gateway.model.DeviceModel;
import com.konka.iot.interior.device.gateway.mqtt.DeviceMqttCallback;
import com.konka.iot.interior.device.gateway.mqtt.DeviceMqttClientService;
import com.konka.iot.kilink.cloud.support.api.model.device.DeviceMapping;
import com.konka.iot.kilink.cloud.support.api.model.product.ProductMapping;
import com.konka.iot.kilink.cloud.support.api.service.device.DeviceService;
import com.konka.iot.kilink.cloud.support.api.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-11-19 18:07
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

    @Autowired
    private InteriorDeviceService interiorDeviceService;

    private static volatile BlockingQueue<Map<String, Object>> paramsQueue = new ArrayBlockingQueue(1);

    private static volatile BlockingQueue<DeviceActiveResultModel> resultModelsQueue = new ArrayBlockingQueue(1);

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
    public boolean addDevice(String g_deviceId, DeviceAddModel deviceAddModel) throws Exception {
        // 获取当前设备的信息
        List<DeviceModel> deviceModels = interiorDeviceService.getDevices(deviceAddModel.getAccessToken(),
                deviceAddModel.getUserId(), new String[]{deviceAddModel.getDeviceId()});
        if(deviceModels == null || deviceModels.isEmpty()){
            logger.error("获取设备列表为空");
            throw new DataCheckException("获取设备列表为空");
        }
        DeviceModel deviceModel = deviceModels.get(0);
        String t_pid = deviceModel.getDevInfo().getProdId();

        // 保存第三方产品ID
        Set<String> t_pids = new HashSet<>(1);
        t_pids.add(t_pid);
        // 获取产品映射集合
        List<ProductMapping> productMappings = productService.getKilinkProductIdBatch(t_pids);
        // 过滤未配置产品映射关系的设备
        if (productMappings == null || productMappings.isEmpty()) {
            throw new DataCheckException(ErrorCodeEnum.PRODUCT_MAPPING_INFO_NULL.getCode(),
                    ErrorCodeEnum.PRODUCT_MAPPING_INFO_NULL.getMessage());
        }

        dealActiveDevice(g_deviceId, t_pid, deviceAddModel, productMappings.get(0));

        return true;
    }

    // 单个设备
    private void dealActiveDevice(String g_deviceId, String t_pid, DeviceAddModel deviceAddModel, ProductMapping productMapping) throws Exception {
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
                if (t_pid.equals(productMapping.getTProductId())) {
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
            if (t_pid.equals(productMapping.getTProductId())) {
                activeDevice(productMapping, g_deviceId, deviceAddModel);
            }
        }
    }

    private void activeDevice(ProductMapping productMapping, String g_deviceId, DeviceAddModel deviceAddModel) throws Exception {
        // 将用户id 传递给mqtt回调处理类
        Map<String, Object> params = new HashMap<>(3);
        params.put("mac", deviceAddModel.getMac());
        params.put("deviceAddModel", deviceAddModel);
        params.put("productMapping", productMapping);
        // 将参数放入阻塞队列
        paramsQueue.put(params);
        // 初始化mqtt回调的阻塞队列
        deviceMqttCallback.setParamsQueue(paramsQueue);
        // 激活设备
        deviceMqttClientService.activeDeviceSync(productMapping.getKProductId(), deviceAddModel.getMac(), g_deviceId);
    }
}
