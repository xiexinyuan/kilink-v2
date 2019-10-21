package com.konka.iot.baseframe.mqtt.utils;

import com.konka.iot.baseframe.common.utils.ByteUtil;
import com.konka.iot.baseframe.mqtt.model.VgCommand;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import sun.misc.BASE64Encoder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author zwm
 * @date 2018-12-7
 */
public class CommandUtil {

    /**
     * 将命令list转换为 byte数组
     * @param command 命令
     * @return mqtt 接受数组
     */
    public static byte[] getByteDatapoint(List<VgCommand> command) {
        int dpLen = 0;
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        // topic
        buffer.putShort((short) 6);
        // len
        buffer.putShort((short) 1);
        // flag
        buffer.put((byte) 96);
        for (VgCommand m : command) {
            Integer b1 = m.getType();
            if (b1 < 0 || b1 > 9) {
                continue;
            }
            buffer.put((byte) m.getIndex().intValue());
            switch (b1) {
                case 1:
                    // bool
                    dpLen += 1;
                    buffer.putShort((short) 1);
                    String v = m.getValue().trim();
                    if ("true".equalsIgnoreCase(v) || "false".equalsIgnoreCase(v)) {
                        m.setValue("true".equalsIgnoreCase(v) ? "1" : "0");
                    }
                    buffer.put((byte) Integer.parseInt(m.getValue()));
                    break;
                case 2:
                    // byte
                    dpLen += 1;
                    buffer.putShort((short) 1);
                    buffer.put((byte) Integer.parseInt(m.getValue()));
                    break;
                case 3:
                    // s16 16位短整型(有符号)
                    dpLen += 2;
                    buffer.putShort((short) 4098);
                    buffer.putShort((short) Integer.parseInt(m.getValue()));
                    break;
                case 4:
                    // s32 32位整型(有符号)
                    dpLen += 4;
                    buffer.putShort((short) 12292);
                    buffer.putInt(Integer.parseInt(m.getValue()));
                    break;
                case 5:
                    // float 浮点
                    dpLen += 4;
                    buffer.putShort((short) 28676);
                    buffer.putFloat(Float.parseFloat(m.getValue()));
                    break;
                case 6:
                    // string 字符串。如果是中文会报错
                    int strLen = m.getValue().length();

                    dpLen += strLen;
                    buffer.putShort((short) (9 << 12 | (strLen & 0xfff)));
                    buffer.put(m.getValue().getBytes());
                    break;
                case 7:
                    // 字节数组
                    strLen = m.getValue().length() / 2;
                    dpLen += strLen;
                    buffer.putShort((short) (10 << 12 | (strLen & 0xfff)));
                    byte[] bytesTmp = new byte[strLen];
                    ByteUtil.hexToBytes(bytesTmp, 0, m.getValue());
                    buffer.put(bytesTmp);
                    break;
                case 8:
                    // u16 16位短整型(无符号)
                    dpLen += 2;
                    buffer.putShort((short) 8194);
                    buffer.putShort((short) Integer.parseInt(m.getValue()));
                    break;
                case 9:
                    // u32 32位整型(无符号)
                    dpLen += 4;
                    buffer.putShort((short) 16388);
                    buffer.putInt(Integer.parseInt(m.getValue()));
                    break;
                default:
                    //没有对应的数据类型，舍弃该值
                    break;
            }
        }
        dpLen += 3 * command.size();

        MqttMessage message = new MqttMessage();
        message.setQos(1);

        buffer.array()[2] = (byte) ((dpLen + 1) >>> 8);
        buffer.array()[3] = (byte) ((dpLen + 1) & 0xff);
        byte[] bf = new byte[dpLen + 5];
        buffer.position(0);
        buffer.get(bf);
        return bf;
    }

    /**
     * 解析成命令
     * @param payload byte 参数
     * @return 命令格式
     */
    public static List<VgCommand> parseDatapoint(byte[] payload) {
        ByteBuffer b = ByteBuffer.wrap(payload);
        // topic。一般为7
        short topic = b.getShort();
        // length。内容长度
        short length = b.getShort();
        short messageId = b.getShort();
        // sync。同步标志。一般为96
        byte sync = b.get();
        if (96 == sync) {
            // 参数值从第6位开始解析。总长度位length
            List<VgCommand> commands = new ArrayList<>();
            int pos = 6;
            while (pos < length) {
                // index
                int index = b.get();
                // type
                short type = b.getShort();
                VgCommand c = new VgCommand();
                c.setIndex(index);
                pos += 3;
                switch (type) {
                    case 1:
                        // bool or byte
                        c.setType(2);
                        c.setValue(b.get() + "");
                        pos += 1;
                        break;
                    case 4098:
                        // s16 16位短整型(有符号) 16 2
                        c.setType(3);
                        c.setValue(b.getShort() + "");
                        pos += 2;
                        break;
                    case 12292:
                        c.setType(4);
                        c.setValue(b.getInt() + "");
                        // s32 32位整型(有符号) 48 4
                        pos += 4;
                        break;
                    case 28676:
                        c.setType(5);
                        c.setValue(b.getFloat() + "");
                        // float 浮点 112 4
                        pos += 4;
                        break;
                    case 8194:

                        c.setType(8);
                        c.setValue(b.getShort() + "");
                        // u16 16位短整型(无符号) 32 2
                        pos += 4;
                        break;
                    case 16388:
                        // u32 32位整型(无符号) 64, 4
                        c.setType(9);
                        c.setValue(b.getInt() + "");
                        pos += 4;
                        break;
                    //0, 7, 0, 8, 0, 0, 96, 8, -96, 2, -2, -1
                    //0, 7, 0, 10, 0, 0, 96, 8, -96, 4, -1, -1, -1, -1
                    default:
                        byte high = (byte) (0x00FF & (type >> 8));
                        if (high == -112) {
                            //该值为string类型
                            //长度为无符号整数
                            int stringLength = (0x00FF & type) & 0xFF;
                            byte[] stringBytes = new byte[stringLength];
                            b.get(stringBytes);
                            c.setType(6);
                            c.setValue(new String(stringBytes));
                            pos += stringLength;
                        } else if (high == -96) {
                            //byte类型
                            //长度为无符号整数。
                            int stringLength = (0x00FF & type) & 0xFF;
                            byte[] stringBytes = new byte[stringLength];
                            b.get(stringBytes);
                            c.setType(7);
                            //由于value是String类型。在这里将byte[]使用base64加密成String存储
                            BASE64Encoder encoder = new BASE64Encoder();
                            c.setValue(encoder.encode(stringBytes));
                            pos += stringLength;
                        } else {
                            //没有对应的类型。说明payload有问题，直接返回
                            return commands;
                        }
                }
                commands.add(c);
            }
            return commands;
        }
        return Collections.emptyList();
    }
}
