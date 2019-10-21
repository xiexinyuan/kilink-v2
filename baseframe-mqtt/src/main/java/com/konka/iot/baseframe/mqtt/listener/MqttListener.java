package com.konka.iot.baseframe.mqtt.listener;

import com.konka.iot.baseframe.mqtt.client.MqttClientService;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-24 14:57
 * @Description mqtt回调类
 */
public abstract class MqttListener implements MqttCallback {

    public final Logger log = LoggerFactory.getLogger(getClass());

    // 获取连接服务
    public abstract MqttClientService getMqttClientService();

    @Override
    public void connectionLost(Throwable cause) {
        boolean connected = false;
        // 连接丢失后，一般在这里面进行重连
        while(true) {
            try {
                log.info("连接断开，30S之后尝试重连");
                Thread.sleep(30000);
                connected = getMqttClientService().reConnect();
                log.info("重新连接 mqtt 服务端成功");
                break;
            } catch (Exception e) {
                log.info("重新连接 mqtt 服务端失败：{}", e.getMessage());
                e.printStackTrace();
                continue;
            }
        }
        try {
            connectionLostListener(connected);
        } catch (Exception e) {
            log.error("connectionLostListener error ： {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {

        try {
            messageArrivedListener(topic, message);
        } catch (Exception e) {
            log.error("messageArrived error :{}",e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            deliveryCompleteListener(token);
        } catch (Exception e) {
            log.error("deliveryComplete error :{}",e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 监听与服务器关闭连接
     * @return
     */
    public abstract void connectionLostListener(boolean connected)throws Exception;
    /**
     * 监听消息
     * @return
     */
    public abstract void messageArrivedListener(String topic, MqttMessage message) throws Exception;
    /**
     * 监听交互完成
     * @return
     */
    public abstract void deliveryCompleteListener(IMqttDeliveryToken token)throws Exception;
}
