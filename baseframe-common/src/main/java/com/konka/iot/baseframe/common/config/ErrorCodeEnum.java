package com.konka.iot.baseframe.common.config;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-15 11:55
 * @Description 全局错误码枚举
 */
public enum ErrorCodeEnum {

    // 系统相关错误码以10开头
    SUCCESS(100001, "operate successfully"),
    ERROR(100002, "operation failure"),
    SERVICE_SERVICE(100003, "service service"),


    // 设备相关错误码以20开头
    DEVICE_ACTIVE_SUCCESS(200001, "device activation successfully"),
    DEVICE_ACTIVE_ERROR(200002, "device activation failure"),
    VIRTUAL_DEVICE_NOT_EXIST(200003, "virtual devices do not exist"),
    ACTIVATION_CODE_ERROR(200004, "activation code error"),
    UNAUTHORIZED(200005, "unauthorized"),
    SN_NOT_CORRESPOND(200006, "field SN does not correspond to an existing device"),
    INSUFFICIENT_AUTHORIZED_QUOTA(200007, "insufficient product authorization quota, pre-import with Mac pool, insufficient authorization quota when activating the create device"),
    MAC_EXISTING(200008, "Mac is Existing and cannot be reused"),
    REFUSED_ACTIVATE(200009, "refused to activate"),
    UNDEFINED_CODE(200010, "undefined ret code"),
    DEVICE_BIND_ERROR(200011, "device mapping relationship binding failed"),
    DEVICE_BIND_USER_ERROR(200012, "device binding user failed"),

    // 设备上线
    DEVICE_ONLINE_SUCCESS(200013, "device on-line successfully"),
    DEVICE_ONLINE_ERROR(200014, "device on-line failure"),
    DEVICE_KEY_ERROR(200015, "device key error"),
    CERTIFICATE_UNAUTHORIZED(200016, "certificate unauthorized"),
    DEVICE_NOT_EXIST(200017, "the device does not exist"),
    DEVICE_NETWORK_INFO_NULL(200018, "device distribution network information is empty"),


    // 产品相关错误码以30开头
    PRODUCT_MAPPING_INFO_NULL(30001, "product configuration information is empty"),

    // 网关相关错误码以40开头
    GATEWAY_NOT_EXIST(400001, "the gateway device does not exist"),
    NOT_GATEWAY_DEVICE(400002, "the specified gateway device ID is not a gateway device"),
    GATEWAY_ID_NOT_EXIST(400003, "the gateway device ID does not exist");


    ErrorCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    public int getCode( ) {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage( ) {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
