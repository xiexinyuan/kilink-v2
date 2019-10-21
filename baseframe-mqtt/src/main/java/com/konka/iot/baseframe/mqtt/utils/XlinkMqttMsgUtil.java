package com.konka.iot.baseframe.mqtt.utils;

import com.konka.iot.baseframe.common.utils.ByteUtil;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.ByteBuffer;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-24 19:55
 * @Description xlink mqtt消息体工具类
 */
public class XlinkMqttMsgUtil {

    /**
     * 普通设备
     * @param mac mac地址
     * @param pid 产品ID
     * @return
     */
    public static byte[] buildMqttActivePayload(String pid, String mac) {

        int macLength = (int) Math.ceil(mac.length() / 2.0);
        ByteBuffer byteBuffer = ByteBuffer.allocate(15 + macLength + pid.length());
        byteBuffer.putShort((short) 1);
        byteBuffer.putShort((short) (byteBuffer.capacity() - 4));

        byteBuffer.putShort((short) pid.length());
        byteBuffer.put(pid.getBytes());

        byteBuffer.putShort((short) (macLength));
        byte[] bytesTmp = new byte[macLength];
        hexToBytes(bytesTmp, 0, mac);
        byteBuffer.put(bytesTmp);
        byte[] f = {(byte) 0xf0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byteBuffer.put(f);

        return byteBuffer.array();
    }
    /**
     *  网关子设备
     * @param mac mac地址
     * @param pid 产品ID
     * @param gatewayId 网关设备ID
     * @return
     */
    public static byte[] buildMqttActivePayloadGW(String pid, String mac, String [] gatewayId) {
        byte[] payload = new byte[80];
        int index = 0;
        // type
        payload[index++] = 0x00;
        payload[index++] = 0x01;

        index += 2;

        // pid length
        int pid_length = pid.length();
        int pid_h = (pid_length & 0xFF00) >> 8;
        int pid_l = pid_length & 0xFF;
        payload[index++] = (byte)pid_h;
        payload[index++] = (byte)pid_l;

        // pid value
        for (byte pidByte: pid.getBytes()) {
            payload[index++] = pidByte;
        }

       // mac length
        int mac_length = (int) Math.ceil(mac.length() / 2.0);
        byte[] bytesTmp = new byte[mac_length];
        hexToBytes(bytesTmp, 0, mac);
        int mac_h = (mac_length & 0xFF00) >> 8;
        int mac_l = mac_length & 0xFF;
        payload[index++] = (byte)mac_h;
        payload[index++] = (byte)mac_l;

        // mac value
        for (byte macByte: bytesTmp) {
            payload[index++] = macByte;
        }
        // active flag  --> gateway device id --> 0000 0100
        payload[index++] = 0x04;

/*        // Wifi Firmware
        payload[index++] = 0x00;

        // Wifi Version
        payload[index++] = 0x00;
        payload[index++] = 0x00;

        // MCU Firmware
        payload[index++] = 0x00;

        // MCU Version
        payload[index++] = 0x00;
        payload[index++] = 0x00;

        // SN Length
        payload[index++] = 0x00;
        payload[index++] = 0x00;

        // SN Value
        payload[index++] = 0x00;*/

        // gateway device id 设备ID是int类型的
        int device_id = Integer.parseInt(gatewayId[0]);
        payload[index++] = (byte)((device_id >> 24) & 0xff);
        payload[index++] = (byte)((device_id >> 16) & 0xff);
        payload[index++] = (byte)((device_id >> 8) & 0xff);
        payload[index++] = (byte)((device_id) & 0xff);

        // data length
        int data_length = index - 4;
        int length_h = (data_length & 0xFF00) >> 8;
        int length_l = data_length & 0xFF;

        payload[2] = (byte)length_h;
        payload[3] = (byte)length_l;

        return payload;
    }

    private static void hexToBytes(byte[] targets, int offset, String s) {
        if (s == null) {
            return;
        }
        int len = s.length();
        // 对于基数长度的字符串做了一个适配
        if (targets != null && targets.length >= offset + len / 2) {
            for (int i = 0; i < len; i += 2) {
                if (i + 1 < len) {
                    targets[offset + i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                            + Character.digit(s.charAt(i + 1), 16));
                } else {
                    targets[offset + i / 2] = (byte) (Character.digit(s.charAt(i), 16));
                }
            }
        }
    }

    public static byte[] getStatusRebackResponse(){
        return new byte[]{0, 8, 0, 3, 0, 0, 0};
    }
}
