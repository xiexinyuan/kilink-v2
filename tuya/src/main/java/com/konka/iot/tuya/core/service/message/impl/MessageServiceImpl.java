package com.konka.iot.tuya.core.service.message.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.konka.iot.baseframe.common.core.service.BaseService;
import com.konka.iot.baseframe.common.exception.DataCheckException;
import com.konka.iot.baseframe.common.utils.AESUtil;
import com.konka.iot.baseframe.common.utils.JsonUtil;
import com.konka.iot.baseframe.mqtt.model.VgCommand;
import com.konka.iot.kilink.cloud.support.api.model.device.DataponitMapping;
import com.konka.iot.kilink.cloud.support.api.model.device.DeviceMapping;
import com.konka.iot.kilink.cloud.support.api.service.device.DeviceService;
import com.konka.iot.tuya.config.TuyaConfig;
import com.konka.iot.tuya.core.service.message.MessageService;
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

    @Autowired
    private DeviceMqttClientService deviceMqttClientService;

    @Override
    public void dealMessage(ReceiveDataModel receiveDataModel){

        switch (receiveDataModel.getProtocol()){
            case 4:
                // 数据上报事件
                delDeviceStatus(receiveDataModel.getData());
                break;
            case 20:
                // 其他事件
                break;
            default:
                logger.error("协议号未定义");
                break;
        }
    }

    private void delDeviceStatus(String data){
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
            String result = AESUtil.decrypt(new String(dataByte, "utf-8"), key);
            DeviceStatusReportModel status = JsonUtil.string2Obj(result, DeviceStatusReportModel.class);
            // 获取设备映射关系
            String deviceId = status.getDeviceId();   // 设备ID
            List<String> ids = new ArrayList<>(1);
            ids.add(deviceId);
            List<DeviceMapping> deviceMappings = deviceService.findDeviceMapping(ids);
            // 获取数据端点映射关系
            String productId = status.getProductKey();   // 产品ID
            List<DataponitMapping> dataponitMappings = deviceService.findThridDatapointMapping(productId);

            // 组装设备状态值
            List<DeviceStatus > deviceStatus = status.getStatus();
            List<VgCommand> vgCommands = new ArrayList<>(deviceStatus.size());
            for (DeviceStatus deviceStatu: deviceStatus) {
                VgCommand vgCommand = new VgCommand();
                for (DataponitMapping dataponitMapping: dataponitMappings) {
                    if(deviceStatu.getCode().equals(dataponitMapping.getTDatapointCode())){
                        vgCommand.setIndex(dataponitMapping.getKDatapointIndex());
                        vgCommand.setType(dataponitMapping.getKDatapointType());
                        vgCommand.setValue((String) deviceStatu.getValue());
                        break;
                    }
                }
                vgCommands.add(vgCommand);
            }
            // 发送数据给云端
            for (DeviceMapping deviceMapping: deviceMappings) {
                if(deviceMapping.getTDeviceId().equals(deviceId)){
                    // 上报设备当前状态
                    deviceMqttClientService.reportDeviceStatus(deviceMapping.getKDeviceId(), vgCommands);
                    break;
                }
            }
        } catch (DataCheckException e) {
            logger.error("设备上报实时数据异常：{}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("设备上报实时数据异常：{}", e.getMessage());
            e.printStackTrace();
        }
    }
}
