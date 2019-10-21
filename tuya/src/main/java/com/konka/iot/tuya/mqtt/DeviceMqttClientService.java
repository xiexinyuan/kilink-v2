package com.konka.iot.tuya.mqtt;

import com.konka.iot.baseframe.common.utils.ByteUtil;
import com.konka.iot.baseframe.mqtt.client.xlink.XlinkMqttClientService;
import com.konka.iot.baseframe.mqtt.config.MqttConfig;
import com.konka.iot.baseframe.mqtt.listener.MqttListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-24 15:34
 * @Description TODO
 */

@Component
public class DeviceMqttClientService extends XlinkMqttClientService {


    @Autowired
    private MqttConfig mqttConfig;

    @Autowired
    private DeviceMqttCallback deviceMqttCallback;

    @Override
    public MqttListener setMqttListener() {
        return deviceMqttCallback;
    }

    @Override
    public MqttConfig setMqttCofig( ) {
        // 构建秘钥
        mqttConfig.setPassWord(ByteUtil.bytesToHex(ByteUtil.digestMD5((mqttConfig.getUserName()
                + mqttConfig.getPassWord()).getBytes())));
        return mqttConfig;
    }
}
