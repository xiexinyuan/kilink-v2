package com.konka.iot.baseframe.mqtt.config;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-24 15:50
 * @Description TODO
 */
public class XlinkMqttConstant {

    /**
     * topic相关枚举
     */
    public enum Topic {
        /**
         * 设备激活
         */
        DEVICE_ACTIVATION("$1"),
        /**
         * 设备激活结果  回调
         */
        DEVICE_ACTIVATION_RESULT("$2"),
        /**
         * 设备上线
         */
        DEVICE_ONLINE("$3/{device_id}"),
        /**
         * 设备上线应答 回调
         */
        DEVICE_ONLINE_RESULT("$4/{device_id}"),
        /**
         * 设备下线
         */
        DEVICE_OFFLINE("$5/{device_id}"),
        /**
         * 设备上报数据端点 (设备主动上报数据)
         */
        DEVICE_DATAPOINT_SYNC("$6/{device_id}"),
        /**
         * 应用设置数据端点 (设备控制)
         */
        DEVICE_CONTROL("$7/{device_id}"),
        /**
         * 应用设置数据端点 (设备控制) 响应
         */
        DEVICE_CONTROL_RESPONSE("$8/{device_id}"),
        /**
         * 设备接收通知
         */
        DEVICE_ACCEPTANCE_NOTICE("$c/{device_id}"),
        /**
         * 获取设备端点数据
         */
        DEVICE_GET_DATAPOINT("$w/{device_id}"),

        /**
         * 设备日志开关
         */
        DEViCE_LOG_SWITCH("$j/{device_id}");


        /**
         * The Code.
         */
        public String code;

        Topic(String code) {
            this.code = code;
        }

        /**
         * Gets code.
         *
         * @return the code
         */
        public String getCode() {
            return code;
        }

        /**
         * 把{device_id} 替换成 真正的device_id
         *
         * @return the code
         */
        public String getCode(String deviceId) {
            return code.replace("{device_id}", deviceId);
        }
    }
}
