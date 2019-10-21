package com.konka.iot.baseframe.mqtt.client.xlink;

import com.konka.iot.baseframe.common.exception.DataCheckException;
import com.konka.iot.baseframe.common.utils.StringUtil;
import com.konka.iot.baseframe.mqtt.client.MqttClientService;
import com.konka.iot.baseframe.mqtt.config.XlinkMqttConstant.Topic;
import com.konka.iot.baseframe.mqtt.model.VgCommand;
import com.konka.iot.baseframe.mqtt.utils.CommandUtil;
import com.konka.iot.baseframe.mqtt.utils.XlinkMqttMsgUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-24 19:42
 * @Description xlink mqtt客户端
 */
public abstract class XlinkMqttClientService extends MqttClientService {

    private static final Logger log = LoggerFactory.getLogger(MqttClientService.class);

    /**
     * 同步激活
     *
     * @param productId 产品id
     * @param mac       mac地址
     * @param gatewayid 网关设备ID
     * @return Boolean
     * @throws DataCheckException
     */
    public void activeDeviceSync(String productId, String mac, String... gatewayid) throws Exception {
        log.info("激活设备 productId={}, mac={}", productId, mac);
        if (mac == null || "".equals(mac)) {
            throw new DataCheckException("mac is null");
        }
        byte[] msg = null;
        if (gatewayid == null || gatewayid.length == 0) {
            msg = XlinkMqttMsgUtil.buildMqttActivePayload(productId, mac);
        } else {
            msg = XlinkMqttMsgUtil.buildMqttActivePayloadGW(productId, mac, gatewayid);
            log.info("网关子设备发送激活消息： {}", Arrays.toString(msg));
        }
        publishMessage(Topic.DEVICE_ACTIVATION.getCode(), msg, 1);
        subTopic(Topic.DEVICE_ACTIVATION_RESULT.getCode());

    }
    /**
     * 同步上线设备
     *
     * @param deviceId
     * @return Boolean
     */
    public void onlineDeviceSync(String deviceId) throws Exception {
        String onlineResulttopic = Topic.DEVICE_ONLINE_RESULT.getCode(deviceId);
        log.info("设备上线订阅topic：{}", onlineResulttopic);
        byte[] tmp = {0, 3, 0, 0};
        publishMessage(Topic.DEVICE_ONLINE.getCode(deviceId), tmp, 1);
        subTopic(onlineResulttopic);
    }

    public void onlineSuccessSubTopic(String deviceId)throws Exception{
        String[] topics = {
                Topic.DEVICE_CONTROL.getCode(deviceId),// 控制指令
                Topic.DEVICE_ACCEPTANCE_NOTICE.getCode(deviceId),// 设备通知
                Topic.DEVICE_GET_DATAPOINT.getCode(deviceId)}; // 获取数据端点
        int[] qos = {1, 1, 1};
        log.info("设备上线成功后订阅以下topic：{}", topics);
        subTopic(topics, qos);
    }

    /**
     * 设备下线
     *
     * @param deviceId
     * @return
     * @throws DataCheckException
     */
    public void offlineDevice(String deviceId) throws Exception {
        byte[] tmp = {0, 5, 0, 0};
        cleanTopic(new String[]{Topic.DEVICE_ONLINE.getCode(deviceId),
                Topic.DEVICE_DATAPOINT_SYNC.getCode(deviceId),
                Topic.DEVICE_ONLINE_RESULT.getCode(deviceId),
                Topic.DEVICE_CONTROL.getCode(deviceId),
                Topic.DEVICE_ACCEPTANCE_NOTICE.getCode(deviceId),
                Topic.DEVICE_GET_DATAPOINT.getCode(deviceId)});

        publishMessage(Topic.DEVICE_OFFLINE.getCode(deviceId), tmp, 1);

    }

    /**
     * 上报设备状态
     *
     * @param deviceId
     * @param command
     * @return
     * @throws DataCheckException
     */
    public void reportDeviceStatus(String deviceId, List<VgCommand> command) throws Exception {

        if (command == null || StringUtil.isEmpty(deviceId) || command.isEmpty()) {
            throw new DataCheckException("command is isEmpty");
        }
        try {
            publishMessage(Topic.DEVICE_DATAPOINT_SYNC.getCode(deviceId), CommandUtil.getByteDatapoint(command), 1);
        } catch (Exception e) {
            log.error("设备" + deviceId + "上报状态失败", e);
        }
    }

    /**
     * 打开设备日志开关
     *
     * @param deviceId
     */
    public void deviceLogSwitch(String deviceId) throws Exception {
        byte[] tmp = new byte[5];
        int length_h = (1 & 0xFF00) >> 8;
        int length_l = 1 & 0xFF;
        tmp[0] = 0x00;
        tmp[1] = 0x13;
        tmp[2] = (byte) length_h;
        tmp[3] = (byte) length_l;
        tmp[4] = 0x08;
        publishMessage(Topic.DEViCE_LOG_SWITCH.getCode(deviceId), tmp, 1);
        log.info("打开设备日志开关 topic：{}", Topic.DEViCE_LOG_SWITCH.getCode(deviceId));
    }
}
