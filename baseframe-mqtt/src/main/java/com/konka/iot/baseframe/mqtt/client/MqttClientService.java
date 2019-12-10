package com.konka.iot.baseframe.mqtt.client;

import com.konka.iot.baseframe.common.exception.DataCheckException;
import com.konka.iot.baseframe.mqtt.config.MqttConfig;
import com.konka.iot.baseframe.mqtt.listener.MqttListener;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;


/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-24 12:12
 * @Description mqtt客户端抽象类
 */
public abstract class MqttClientService {

    private static final Logger log = LoggerFactory.getLogger(MqttClientService.class);

    private static MqttClient client = null;
    private static MqttConnectOptions option = null;

    /**
     * 设置mqtt回调
     *
     * @return
     */
    public abstract MqttListener setMqttListener();

    /**
     * 设置mqtt配置信息
     *
     * @return
     */
    public abstract MqttConfig setMqttCofig();


    public synchronized boolean init() {
        try {
            MqttConfig mqttCofig = setMqttCofig();
            if (mqttCofig == null) {
                throw new DataCheckException("mqttCofig is null");
            }
            if (null == client) {
                // MemoryPersistence设置clientid的保存形式，默认为以内存保存
                client = new MqttClient(mqttCofig.getUrl(), mqttCofig.getClientId(),null);
                //设置回调
                client.setCallback(setMqttListener());
            }
            //获取连接配置
            getOption(mqttCofig);
            client.connect(option);
            log.info("connect to Mqtt Server success");
            return client.isConnected();
        } catch (DataCheckException e) {
            log.error("connect to Mqtt Server error: {}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            log.error("connect to Mqtt Server error: {}", e.getMessage());
            e.printStackTrace();

        }
        return false;
    }


    private void getOption(MqttConfig mqttCofig) {
        //MQTT连接设置
        option = new MqttConnectOptions();
        //设置是否清空session,false表示服务器会保留客户端的连接记录，true表示每次连接到服务器都以新的身份连接
        option.setCleanSession(mqttCofig.getCleanSession());
        //设置连接的用户名
        option.setUserName(mqttCofig.getUserName());
        //设置连接的密码
        option.setPassword(mqttCofig.getPassWord().toCharArray());
        //设置版本
        option.setMqttVersion(mqttCofig.getVersion());
        //设置超时时间 单位为秒
        option.setConnectionTimeout(mqttCofig.getConnectionTimeout());
        //设置会话心跳时间 单位为秒 服务器会每隔(1.5*keepTime)秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
        option.setKeepAliveInterval(mqttCofig.getKeepAliveInterval());
        //设置是否自动重连
        option.setAutomaticReconnect(mqttCofig.getAutomaticReconnect());
        //setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
        //option.setWill(topic, "close".getBytes(), 2, true);
    }

    //断线重连
    public boolean reConnect() throws Exception {
        if (null != client && option != null) {
            client.connect(option);
            return client.isConnected();
        }else {
            throw new DataCheckException("mqtt client or option is null");
        }
    }

    // 关闭连接
    public void closeConnect() {
        try {
            //关闭存储方式
            //关闭连接
            if (null != client) {
                if (client.isConnected()) {
                    client.disconnect();
                    client.close();
                } else {
                    throw new DataCheckException("mqtt client is not connect");
                }
            } else {
                throw new DataCheckException("mqtt client is null");
            }
        } catch (MqttException e) {
            log.error("close connect error : {}", e.getMessage());
            e.printStackTrace();
        } catch (DataCheckException e) {
            log.error("close connect error : {}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            log.error("close connect error : {}", e.getMessage());
            e.printStackTrace();
        }
    }

    // 发布消息
    public void publishMessage(String pubTopic, String message, int qos) throws Exception {
        if (null != client && client.isConnected()) {
            log.info("发布消息: {}", client.isConnected());
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setQos(qos);
            mqttMessage.setPayload(message.getBytes());
            MqttTopic topic = client.getTopic(pubTopic);
            if (null != topic) {
                try {
                    MqttDeliveryToken publish = topic.publish(mqttMessage);
                    if (!publish.isComplete()) {
                        log.info("消息发布成功");
                    }
                } catch (MqttException e) {
                    log.error("消息发布异常： {}", e.getMessage());
                    e.printStackTrace();
                }
            }

        } else {
            log.error("mqtt client is null");
            throw new DataCheckException("mqtt client is null");
        }

    }

    // 发布消息
    public void publishMessage(String pubTopic, MqttMessage mqttMessage) throws Exception {
        if (null != client && client.isConnected()) {
            MqttTopic topic = client.getTopic(pubTopic);
            if (null != topic) {
                try {
                    MqttDeliveryToken publish = topic.publish(mqttMessage);
                    if (!publish.isComplete()) {
                        log.info("消息发布成功");
                    }
                } catch (MqttException e) {
                    log.error("消息发布异常： {}", e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            log.error("mqtt client is null");
            throw new DataCheckException("mqtt client is null");
        }

    }

    // 发布消息
    public void publishMessage(String pubTopic, byte[] message, int qos) throws Exception {
        if (null != client && client.isConnected()) {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setQos(qos);
            mqttMessage.setPayload(message);
            MqttTopic topic = client.getTopic(pubTopic);
            if (null != topic) {
                try {
                    MqttDeliveryToken publish = topic.publish(mqttMessage);
                    if (!publish.isComplete()) {
                        log.info("消息发布成功");
                    }
                } catch (MqttException e) {
                    log.error("消息发布异常： {}", e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            log.error("mqtt client is null");
            throw new DataCheckException("mqtt client is null");
        }

    }

    // 订阅主题
    public void subTopic(String topic) throws Exception {
        if (null != client && client.isConnected()) {
            try {
                client.subscribe(topic, 1);
            } catch (MqttException e) {
                log.error("subTopic exception : {}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            log.error("mqtt client is error");
            throw new DataCheckException("mqtt client is error");
        }
    }

    // 批量订阅
    public void subTopic(String[] topic, int[] qos) {
        if (null != client && client.isConnected()) {
            try {
                client.subscribe(topic, qos);
            } catch (MqttException e) {
                log.error("subTopic exception : {}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            log.error("mqtt client is error");
        }
    }


    // 清空主题
    public void cleanTopic(String topic) {
        if (null != client && !client.isConnected()) {
            try {
                client.unsubscribe(topic);
            } catch (MqttException e) {
                log.info("cleanTopic exception : {}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            log.info("client is error");
        }
    }

    // 批量清空主题
    public void cleanTopic(String[] topic) {
        if (null != client && !client.isConnected()) {
            try {
                client.unsubscribe(topic);
            } catch (MqttException e) {
                log.info("cleanTopic exception : {}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            log.info("mqtt client is error");
        }
    }
}
