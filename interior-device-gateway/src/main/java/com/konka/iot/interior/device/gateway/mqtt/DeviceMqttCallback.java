package com.konka.iot.interior.device.gateway.mqtt;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;
import com.konka.iot.baseframe.common.config.ErrorCodeEnum;
import com.konka.iot.baseframe.common.utils.ByteUtil;
import com.konka.iot.baseframe.common.utils.JsonUtil;
import com.konka.iot.baseframe.common.utils.RedisUtil;
import com.konka.iot.baseframe.mqtt.client.MqttClientService;
import com.konka.iot.baseframe.mqtt.config.XlinkMqttConstant.Topic;
import com.konka.iot.baseframe.mqtt.listener.MqttListener;
import com.konka.iot.baseframe.mqtt.model.VgCommand;
import com.konka.iot.baseframe.mqtt.utils.CommandUtil;
import com.konka.iot.baseframe.mqtt.utils.XlinkMqttMsgUtil;
import com.konka.iot.interior.device.gateway.config.GatewayConfig;
import com.konka.iot.interior.device.gateway.core.service.device.InteriorDeviceService;
import com.konka.iot.interior.device.gateway.core.service.gateway.GatewayService;
import com.konka.iot.interior.device.gateway.core.service.gateway.impl.GatewayServiceImpl;
import com.konka.iot.interior.device.gateway.model.DeviceAddModel;
import com.konka.iot.interior.device.gateway.model.DeviceModel;
import com.konka.iot.interior.device.gateway.model.ServicesInfo;
import com.konka.iot.kilink.cloud.support.api.model.device.DataponitMapping;
import com.konka.iot.kilink.cloud.support.api.model.product.ProductMapping;
import com.konka.iot.kilink.cloud.support.api.service.device.DeviceService;
import com.konka.iot.kilink.cloud.support.api.service.product.ProductService;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Base64.Decoder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-24 15:35
 * @Description mqtt回调处理
 */

@Component
public class DeviceMqttCallback extends MqttListener {

    // 阻塞队列
    private BlockingQueue<Map<String, Object>> paramsQueue = new ArrayBlockingQueue(1);

    // 存放激活结果
    public BlockingQueue<Map<String, Object>> getParamsQueue( ) {
        return paramsQueue;
    }

    public void setParamsQueue(BlockingQueue<Map<String, Object>> queue) {
        this.paramsQueue = queue;
    }

    @Autowired
    private GatewayConfig gatewayConfig;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private DeviceMqttClientService deviceMqttClientService;

    @Reference
    private ProductService productService;

    @Reference
    private DeviceService deviceService;

    @Autowired
    private InteriorDeviceService interiorDeviceService;

    @Override
    public MqttClientService getMqttClientService( ) {
        return deviceMqttClientService;
    }

    @Override
    public void connectionLostListener(boolean connected) {
        // 初始化虚拟网关
        if (connected) {
            gatewayService.init();
        }
    }

    @Override
    public void messageArrivedListener(String topic, MqttMessage message) throws Exception{
        log.info("DeviceMqttCallback - 接收消息主题 :{} ", topic);
        log.info("DeviceMqttCallback - 接收消息内容 : {}", Arrays.toString(message.getPayload()));
        if (Topic.DEVICE_ACTIVATION_RESULT.getCode().equals(topic)) {
            //设备激活结果  回调
            activeResult(message.getPayload());
        } else if (topic.startsWith(Topic.DEVICE_ONLINE_RESULT.getCode(""))) {
            //设备上线应答 回调
            onlineResult(topic, message.getPayload());
        } else if (topic.startsWith(Topic.DEVICE_CONTROL.getCode(""))) {

            byte[]  messageIdByte = Arrays.copyOfRange(message.getPayload(), 4, 6);
            int messageId = ByteUtil.byteToInt(messageIdByte);
            log.info("=======================messageId========================");
            log.info("messageId is : {}", messageId);
            log.info("=======================messageId========================");
            //应用设置数据端点 topic : s7/123456
            String deviceId = topic.substring(3);
            List<VgCommand> vgCommands = CommandUtil.parseDatapoint(message.getPayload());
            // 添加控制涂鸦的方法 1、设备id查到涂鸦设备id,平台产品id->数据端点映射关系—>涂鸦端点
            commandDevice(deviceId, vgCommands);
            // $8是为了请求不超时
            MqttMessage msg = new MqttMessage();
            msg.setId(messageId);
            msg.setQos(0);
            msg.setPayload(XlinkMqttMsgUtil.getStatusRebackResponse(messageId));
            deviceMqttClientService.publishMessage(Topic.DEVICE_CONTROL_RESPONSE.getCode(deviceId), msg);
        }
    }

    @Override
    public void deliveryCompleteListener(IMqttDeliveryToken token) {
    }

    /**
     * 激活结果回调
     *
     * @param data
     */
    private void activeResult(byte[] data) throws Exception{

        byte[] mac_length_byte = new byte[]{data[4], data[5]};
        int mac_length_int = ByteUtil.byteToInt(mac_length_byte);
        int ret_code = data[5 + mac_length_int + 1];

        // 获取回调的mac
        String mac = ByteUtil.bytesToHex(Arrays.copyOfRange(data, 6, mac_length_int + 6));
        // 是否是网关设备
        boolean isGateway = mac.equals(GatewayServiceImpl.gatewaiMac);

        Map<String, Object> params = null;
        DeviceAddModel deviceAddModel = null;

        // 处理网关子设备时获取外部参数

        if(!isGateway){
            // 获取外部传递过来的参数
            params = getParamsQueue().take();
            deviceAddModel = (DeviceAddModel) params.get("deviceAddModel");
        }
        switch (ret_code) {
            case 0:
                // 获取回调的deviceid
                String deviceId = ByteUtil.byteToInt(Arrays.copyOfRange(data, mac_length_int + 7, mac_length_int + 11)) + "";
                // 将激活的设备上线
                try {
                    deviceMqttClientService.onlineDeviceSync(deviceId);
                } catch (Exception e) {
                    log.error("设备上线异常： {}", e.getMessage());
                    saveActiveRecord(deviceId, ErrorCodeEnum.DEVICE_ONLINE_ERROR.getCode(), ErrorCodeEnum.DEVICE_ONLINE_ERROR.getMessage(), false);
                    e.printStackTrace();
                    return;
                }
                // 将激活的设备打开日志开关
                try {
                    deviceMqttClientService.deviceLogSwitch(deviceId);
                } catch (Exception e) {
                    log.error("激活成功的设备打开日志开关异常： {}", e.getMessage());
                    e.printStackTrace();
                }
                // 如果是网关设备,将网关设备的id保存至redis
                if (isGateway) {
                    redisUtil.set(gatewayConfig.getGateway_device_id(), deviceId);
                } else {
                    // 网关子设备激活后处理
                    boolean success = activeGatewaySubDevice(deviceId, params);
                    if(success){
                        saveActiveRecord(deviceId, ErrorCodeEnum.DEVICE_ACTIVE_SUCCESS.getCode() ,ErrorCodeEnum.DEVICE_ACTIVE_SUCCESS.getMessage(), true);
                    }
                }
                log.info("设备{}激活成功", deviceId);
                break;
            case 1:
                if(!isGateway){
                    saveActiveRecord(deviceAddModel.getDeviceId(), ErrorCodeEnum.VIRTUAL_DEVICE_NOT_EXIST.getCode(),ErrorCodeEnum.VIRTUAL_DEVICE_NOT_EXIST.getMessage(), false);
                    log.error("涂鸦设备{}激活失败, 虚拟设备不存在", deviceAddModel.getDeviceId());
                }else {
                    log.error("网关设备激活失败, 虚拟设备不存在");
                }

                break;
            case 2:

                if(!isGateway){
                    saveActiveRecord(deviceAddModel.getDeviceId(), ErrorCodeEnum.ACTIVATION_CODE_ERROR.getCode(),ErrorCodeEnum.ACTIVATION_CODE_ERROR.getMessage(),false);
                    log.error("涂鸦设备{}激活失败, 激活码错误", deviceAddModel.getDeviceId());
                }else {
                    log.error("网关设备激活失败, 激活码错误");
                }

                break;
            case 3:
                if(!isGateway){
                    saveActiveRecord(deviceAddModel.getDeviceId(), ErrorCodeEnum.UNAUTHORIZED.getCode(), ErrorCodeEnum.UNAUTHORIZED.getMessage(),false);
                    log.error("涂鸦设备{}激活失败, 未授权", deviceAddModel.getDeviceId());
                }else {
                    log.error("网关设备激活失败, 未授权");
                }

                break;
            case 4:
                if(!isGateway){
                    saveActiveRecord(deviceAddModel.getDeviceId(), ErrorCodeEnum.SN_NOT_CORRESPOND.getCode(), ErrorCodeEnum.SN_NOT_CORRESPOND.getMessage(),false);
                    log.error("涂鸦设备{}激活失败, SN 字段跟已有设备不对应", deviceAddModel.getDeviceId());
                }else {
                    log.error("网关设备激活失败, SN 字段跟已有设备不对应");
                }

                break;
            case 5:
                if(!isGateway){
                    saveActiveRecord(deviceAddModel.getDeviceId(), ErrorCodeEnum.INSUFFICIENT_AUTHORIZED_QUOTA.getCode(),ErrorCodeEnum.INSUFFICIENT_AUTHORIZED_QUOTA.getMessage(),false);
                    log.error("涂鸦设备{}激活失败, 产品授权配额不足，使用 Mac 池预导入，激活创建设备时授权配额不足", deviceAddModel.getDeviceId());
                }else {
                    log.error("网关设备激活失败, 产品授权配额不足，使用 Mac 池预导入，激活创建设备时授权配额不足");
                }

                break;
            case 6:
                if(!isGateway){
                    saveActiveRecord(deviceAddModel.getDeviceId(), ErrorCodeEnum.MAC_EXISTING.getCode(),ErrorCodeEnum.MAC_EXISTING.getMessage(),false);
                    log.error("涂鸦设备{}激活失败, Mac 在 Mac 池已被激活，不能重复使用", deviceAddModel.getDeviceId());
                }else {
                    log.error("网关设备激活失败, Mac 在 Mac 池已被激活，不能重复使用");
                }

                break;
            case 7:
                if(!isGateway){
                    saveActiveRecord(deviceAddModel.getDeviceId(), ErrorCodeEnum.GATEWAY_ID_NOT_EXIST.getCode(), ErrorCodeEnum.GATEWAY_ID_NOT_EXIST.getMessage(),false);
                    log.error("涂鸦设备{}激活失败, 网关设备 ID 不存在", deviceAddModel.getDeviceId());
                }
                break;
            case 8:
                if(!isGateway){
                    saveActiveRecord(deviceAddModel.getDeviceId(), ErrorCodeEnum.NOT_GATEWAY_DEVICE.getCode(), ErrorCodeEnum.NOT_GATEWAY_DEVICE.getMessage(),false);
                    log.error("涂鸦设备{}激活失败, 指定的网关设备 ID 并不是网关设备", deviceAddModel.getDeviceId());
                }

                break;
            case 9:
                if(!isGateway){
                    saveActiveRecord(deviceAddModel.getDeviceId(), ErrorCodeEnum.REFUSED_ACTIVATE.getCode(), ErrorCodeEnum.REFUSED_ACTIVATE.getMessage(),false);
                    log.error("涂鸦设备{}激活失败, 拒绝激活", deviceAddModel.getDeviceId());
                }else {
                    log.error("网关设备激活失败, 拒绝激活");
                }

                break;
            default:
                if(!isGateway){
                    saveActiveRecord(deviceAddModel.getDeviceId(), ErrorCodeEnum.UNDEFINED_CODE.getCode(), ErrorCodeEnum.UNDEFINED_CODE.getMessage(),false);
                }
                log.error("未知的 Ret code");
                break;

        }


    }

    private boolean activeGatewaySubDevice(String deviceId, Map<String, Object> params){

        // 绑定设备-虚拟设备
        boolean deviceSuccess  = false;
        try {
            deviceSuccess = bindDevice(deviceId, params);
        } catch (Exception e) {
            log.error("设备映射关系绑定异常：{}", e.getMessage());
            e.printStackTrace();
        }

        if(!deviceSuccess){
            saveActiveRecord(deviceId, ErrorCodeEnum.DEVICE_BIND_ERROR.getCode(), ErrorCodeEnum.DEVICE_BIND_ERROR.getMessage(), false);
            return false;
        }

        // 绑定设备-用户关系
        boolean userSuccess  = false;
        try {
            userSuccess = bindUser(deviceId, params);
        } catch (Exception e) {
            log.error("设备-用户映射关系绑定异常：{}", e.getMessage());
            e.printStackTrace();
        }

        if(!userSuccess){
            saveActiveRecord(deviceId, ErrorCodeEnum.DEVICE_BIND_USER_ERROR.getCode(), ErrorCodeEnum.DEVICE_BIND_USER_ERROR.getMessage(), false);
            return false;
        }
        // 上报设备当前的状态
        try {
            reportDeviceStatus(deviceId, params);
        } catch (Exception e) {
            log.error("设备上报当前状态异常：{}", e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 设备上线回调
     *
     * @param data
     */
    private void onlineResult(String topic, byte[] data) {
        int ret_code = data[4];
        String deviceId = topic.split("/")[1];
        StringBuffer message = new StringBuffer();
        switch (ret_code) {
            case 0:
                log.info("设备{}上线成功", deviceId);
                try {
                    deviceMqttClientService.onlineSuccessSubTopic(deviceId);
                } catch (Exception e) {
                    log.error("上线成功后订阅topic异常：{}",e.getMessage());
                    e.printStackTrace();
                }
                break;
            case 1:
                log.error("设备{}上线失败, Device Key 不正确", deviceId);
                saveActiveRecord(deviceId, ErrorCodeEnum.DEVICE_KEY_ERROR.getCode(), ErrorCodeEnum.DEVICE_KEY_ERROR.getMessage(), false);

                break;
            case 2:
                log.error("设备{}上线失败, 证书未授权", deviceId);
                saveActiveRecord(deviceId, ErrorCodeEnum.CERTIFICATE_UNAUTHORIZED.getCode(), ErrorCodeEnum.CERTIFICATE_UNAUTHORIZED.getMessage(), false);
                break;
            case 3:
                log.error("设备{}上线失败, 服务不可用", deviceId);
                saveActiveRecord(deviceId, ErrorCodeEnum.SERVICE_SERVICE.getCode(), ErrorCodeEnum.SERVICE_SERVICE.getMessage(), false);
                break;
            case 4:
                log.error("设备{}上线失败, 设备不存在", deviceId);
                saveActiveRecord(deviceId, ErrorCodeEnum.DEVICE_NOT_EXIST.getCode(), ErrorCodeEnum.DEVICE_NOT_EXIST.getMessage(), false);
                break;
            default:
                log.error("设备{}上线失败,未知的 ret code", deviceId);
                saveActiveRecord(deviceId, ErrorCodeEnum.UNDEFINED_CODE.getCode(), ErrorCodeEnum.UNDEFINED_CODE.getMessage(),false);
                break;
        }
    }

    /**
     * 绑定设备-设备映射
     * @param deviceId 设备ID
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean bindDevice(String deviceId, Map<String, Object> params) throws Exception{
        DeviceAddModel deviceAddModel = (DeviceAddModel) params.get("deviceAddModel");
        deviceService.addDeviceMapping(deviceId, deviceAddModel.getDeviceId());
        log.info("设备映射关系绑定成功");
        return true;
    }

    /**
     * 设备-用户映射关系
     * @param deviceId 设备ID
     */
    private boolean bindUser(String deviceId, Map<String, Object> params) throws Exception{
        ProductMapping productMapping = (ProductMapping) params.get("productMapping");
        DeviceAddModel deviceAddModel = (DeviceAddModel) params.get("deviceAddModel");
        // 设备和用户之间通过二维码绑定
        return deviceService.bindByQrcode(deviceAddModel.getUserId(), productMapping.getKProductId(), deviceId, deviceAddModel.getAccessToken());
    }

    /**
     * 上报设备当前的状态
     * @param deviceId
     * @return
     * @throws Exception
     */
    private void reportDeviceStatus(String deviceId, Map<String, Object> params) throws Exception{
        ProductMapping productMapping = (ProductMapping) params.get("productMapping");
        DeviceAddModel deviceAddModel = (DeviceAddModel) params.get("deviceAddModel");
        // 获取设备的当前状态
        List<DeviceModel> statuses = interiorDeviceService.getDevicesSnapshot(deviceAddModel.getAccessToken(), deviceAddModel.getUserId(), new String[]{deviceAddModel.getDeviceId()});
        log.info("获取到的设备{}状态为：{}", deviceId, JsonUtil.obj2String(statuses));
        List<DataponitMapping> dataponitMappings = deviceService.findThridDatapointMapping(productMapping.getTProductId());
        List<VgCommand> vgCommands = new ArrayList<>();
        for (DeviceModel status: statuses) {
            for (DataponitMapping dataponitMapping: dataponitMappings) {
                List<ServicesInfo> servicesInfos = status.getServices();
                for (ServicesInfo servicesInfo: servicesInfos) {
                    Map<String, Object> dataMap = servicesInfo.getData();
                    for (String key : dataMap.keySet()) {
                        if(key.equals(dataponitMapping.getTDatapointCode())){
                            VgCommand vgCommand = new VgCommand();
                            vgCommand.setIndex(dataponitMapping.getKDatapointIndex());
                            vgCommand.setType(dataponitMapping.getKDatapointType());
                            vgCommand.setValue(dataMap.get(key) + "");
                            vgCommands.add(vgCommand);
                            break;
                        }
                    }
                }

            }
        }
        // 上报设备当前状态
        deviceMqttClientService.reportDeviceStatus(deviceId, vgCommands);
    }


    /**
     * 设备控制
     * @param deviceId 设备ID
     * @param commands 控制指令
     */
    private void commandDevice(String deviceId, List<VgCommand> commands){
        log.info("下发控制消息为：{}", commands);
        try {
            //1、获取涂鸦设备ID
            String tDeviceId = deviceService.findTuyaDeviceId(deviceId);

            //2、获取涂鸦设备对应的数据端点
            // 2.1获取涂鸦产品ID
            String tPid = tuyaDeviceService.getDeviceInfo(tDeviceId).getProductId();
            // 2.2根据产品id获取数据端点映射关系
            List<DataponitMapping> dataponitMappings = deviceService.findThridDatapointMapping(tPid);
            //3、组装涂鸦控制命令
            List<Command> tuyaCommands = new ArrayList<>();
            for(VgCommand vgCommand: commands){
                for(DataponitMapping dataponitMapping: dataponitMappings){
                    if(vgCommand.getIndex() == dataponitMapping.getKDatapointIndex()){
                        Command command = new Command(dataponitMapping.getTDatapointCode(), formatterCommandValue(dataponitMapping.getKDatapointType(), vgCommand.getValue()));
                        tuyaCommands.add(command);
                        break;
                    }
                }
            }
            log.info("下发至涂鸦控制消息为：{}", JSONObject.toJSONString(tuyaCommands));
            //4、控制涂鸦设备
            boolean success = tuyaDeviceService.deviceCommand(tDeviceId, tuyaCommands);
            if (success){
                log.info("设备{}控制成功", deviceId);
            }else {
                log.info("设备{}控制失败", deviceId);
            }
        } catch (Exception e) {
            log.info("设备{}控制失败：{}", deviceId, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 转换成涂鸦设备数据格式
     * @param type
     * @param value
     * @return
     */
    private Object formatterCommandValue(int type, String value){
        Object object = new Object();
        try {
            switch (type){
                case 1:
                    object = Integer.parseInt(value) == 0 ? false : true;
                    break;
                case 2:
                    object = Byte.decode(value);
                    break;
                case 3:
                case 8:
                    object = Short.parseShort(value);
                    break;
                case 4:
                case 9:
                    object = Integer.parseInt(value);
                    break;
                case 5:
                    object = Float.parseFloat(value);
                    break;
                case 6:
                    try {
                        object = JSONObject.parseObject(value);
                    } catch (Exception e) {
                        object = value;
                    }
                    break;
                case 7:
                    Decoder decoder = Base64.getDecoder();
                    object = decoder.decode(value);
                    break;
                default:
                    break;
            }
            return object;
        } catch (Exception e) {
            log.error("数据格式转化错误");
            e.printStackTrace();
        }
        return null;
    }

    private void saveActiveRecord(String deviceId, int code, String message, boolean isActive){
/*        DeviceActiveResultModel resultModel = new DeviceActiveResultModel();
        resultModel.setId(deviceId);
        resultModel.setCode(code);
        resultModel.setMessage(message);
        resultModel.setIsActive(isActive);
        kilinkDeviceService.saveActiveRecord(deviceId, code, message, isActive);*/
    }
}
