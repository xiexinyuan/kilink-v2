package com.konka.iot.tuya.core.service.message.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.konka.iot.baseframe.common.core.service.BaseService;
import com.konka.iot.baseframe.common.exception.DataCheckException;
import com.konka.iot.baseframe.common.utils.AESUtil;
import com.konka.iot.baseframe.common.utils.JsonUtil;
import com.konka.iot.baseframe.mqtt.model.VgCommand;
import com.konka.iot.kilink.cloud.support.api.model.device.DataponitMapping;
import com.konka.iot.kilink.cloud.support.api.model.device.DeviceMapping;
import com.konka.iot.kilink.cloud.support.api.model.device.DeviceUsersModel;
import com.konka.iot.kilink.cloud.support.api.service.device.DeviceService;
import com.konka.iot.kilink.cloud.support.api.service.product.ProductService;
import com.konka.iot.tuya.config.TuyaConfig;
import com.konka.iot.tuya.core.service.message.MessageService;
import com.konka.iot.tuya.model.message.DeviceOtherMessageModel;
import com.konka.iot.tuya.model.message.DeviceStatus;
import com.konka.iot.tuya.model.message.DeviceStatusReportModel;
import com.konka.iot.tuya.model.message.ReceiveDataModel;
import com.konka.iot.tuya.mqtt.DeviceMqttClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.List;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-12 17:09
 * @Description TODO
 */
@Service
public class MessageServiceImpl extends BaseService implements MessageService {

    @Autowired
    private TuyaConfig tuyaConfig;

    @Reference
    private DeviceService deviceService;

    @Reference
    private ProductService productService;

    @Autowired
    private DeviceMqttClientService deviceMqttClientService;

    @Override
    public void dealMessage(ReceiveDataModel receiveDataModel){

        switch (receiveDataModel.getProtocol()){
            case 4:
                // 数据上报事件
                delDeviceStatusMessage(receiveDataModel.getData());
                break;
            case 20:
                // 其他事件
                delDeviceOtherMessage(receiveDataModel.getData());
                break;
            default:
                logger.error("协议号未定义");
                break;
        }
    }


    /**
     * 设备上报数据处理
     * @param data
     */
    private void delDeviceStatusMessage(String data){
        try {
            String result = getMessage(data);
            DeviceStatusReportModel status = JsonUtil.string2Obj(result, DeviceStatusReportModel.class);
            logger.info("设备上报数据为：{}", status);
            // 获取设备映射关系
            String deviceId = status.getDevId();   // 设备ID
            List<String> ids = new ArrayList<>(1);
            ids.add(deviceId);
            List<DeviceMapping> deviceMappings = deviceService.findDeviceMapping(ids);
            // 获取数据端点映射关系
            String productId = status.getProductKey();   // 产品ID
            List<DataponitMapping> dataponitMappings = deviceService.findThridDatapointMapping(productId);
            // 组装设备状态值
            List<DeviceStatus > deviceStatus = status.getStatus();
            List<VgCommand> vgCommands = new ArrayList<>();
            for (DeviceStatus deviceStatu: deviceStatus) {
                for (DataponitMapping dataponitMapping: dataponitMappings) {
                    if(deviceStatu.getCode().equals(dataponitMapping.getTDatapointCode())){
                        VgCommand vgCommand = new VgCommand();
                        vgCommand.setIndex(dataponitMapping.getKDatapointIndex());
                        vgCommand.setType(dataponitMapping.getKDatapointType());
                        vgCommand.setValue(String.valueOf(deviceStatu.getValue()));
                        vgCommands.add(vgCommand);
                        break;
                    }
                }
            }
            sendMessage(deviceMappings, vgCommands, deviceId);
        } catch (Exception e) {
            logger.error("设备上报实时数据处理异常： {}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 其他数据处理
     * @param data
     */
    private void delDeviceOtherMessage(String data){
        try {
            String result = getMessage(data);
            DeviceOtherMessageModel model = JsonUtil.string2Obj(result, DeviceOtherMessageModel.class);
            String t_deviceId = model.getDevId();   // 设备ID
            List<String> ids = new ArrayList<>(1);
            ids.add(t_deviceId);
            // 获取设备映射关系
            List<DeviceMapping> deviceMappings = deviceService.findDeviceMapping(ids);
            String k_deviceId = null;
            for (DeviceMapping deviceMapping: deviceMappings) {
                if(t_deviceId.equals(deviceMapping.getTDeviceId())){
                    k_deviceId = deviceMapping.getKDeviceId();
                    break;
                }
            }
            // 获取产品映射关系
            String t_productId = model.getProductKey();   // 产品ID
            String k_productId = productService.getProductMapping(null, t_productId);
            switch (model.getBizCode()){
                case "online":
                    logger.info("=========接收到设备上线消息=========");
                    deviceMqttClientService.onlineDeviceSync(k_deviceId);
                    break;
                case "offline":
                    logger.info("=========接收到设备下线消息=========");
                    deviceMqttClientService.offlineDevice(k_deviceId);
                    break;
                case "delete":
                    logger.info("=========接收到设备删除消息=========");
                    List<DeviceUsersModel> list = deviceService.getDeviceUsers(k_productId, k_deviceId);
                    for (DeviceUsersModel usersModel : list) {
                        deviceService.unbindDevice(usersModel.getUser_id(), k_deviceId);
                        Thread.sleep(100);
                    }
                    break;
            }

        } catch (Exception e) {
            logger.error("设备上报实时数据处理异常： {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private String getMessage(String data){
        try {
            if(data == null){
                throw new DataCheckException("设备上报实时数据为空");
            }
            // base64解码
            Decoder decoder = Base64.getDecoder();
            byte [] dataByte = decoder.decode(data);
            // 用accesskey的中间16位进行AES解密 获得设备状态数据
            String accessKey = tuyaConfig.getAccessKey();
            String key = accessKey.substring(8, 24);

            return AESUtil.aesDecrypt(dataByte, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendMessage(List<DeviceMapping> deviceMappings, List<VgCommand> vgCommands, String deviceId){
        // 发送数据给云端
        for (DeviceMapping deviceMapping: deviceMappings) {
            if(deviceMapping.getTDeviceId().equals(deviceId)){
                // 上报设备当前状态
                try {
                    deviceMqttClientService.reportDeviceStatus(deviceMapping.getKDeviceId(), vgCommands);
                } catch (Exception e) {
                    logger.error("发送设备当前状态异常： {}", e.getMessage());
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
