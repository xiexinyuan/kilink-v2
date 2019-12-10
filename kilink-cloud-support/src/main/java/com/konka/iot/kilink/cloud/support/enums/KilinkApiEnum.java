package com.konka.iot.kilink.cloud.support.enums;

import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-20 17:27
 * @Description kilink地址枚举类
 */
public enum KilinkApiEnum {

    AUTH_TOKEN("/v2/accesskey_auth",RequestMethod.POST,"开发者换取调用凭证"),
    ADD_DEVICE("/v2/product/{product_id}/device",RequestMethod.POST,"添加设备"),
    LIST_GATE_DEVICES("/v2/devices",RequestMethod.POST,"分页查询网关下子设备"),
    PRODUCT_ADD_DATAPOINT("/v2/product/{product_id}/datapoint",RequestMethod.POST,"添加产品数据端点"),
    PRODUCT_DATAPOINT_LIST("/v2/product/{product_id}/datapoints",RequestMethod.POST,"获取产品数据端点列表"),
    SET_DEVICE_DATAPOINT("/v2/device/command/{device_id}/datapoint",RequestMethod.POST,"用于通过操作虚拟设备来回写操作设备"),
    DEVICE_QRCODE("/v2/product/{product_id}/device/{device_id}/qrcode",RequestMethod.POST,"生成设备二维码"),
    USER_BIND_DEVICE_QRCODE("/v2/user/{user_id}/qrcode_subscribe",RequestMethod.POST,"用户通过二维码绑定设备"),
    USER_UNBIND_DEVICE("/v2/user/{user_id}/unsubscribe",RequestMethod.POST,"取消订阅设备"),
    DEVICE_USER_LIST_URL("/v2/product/{product_id}/device/{device_id}/subscribes",RequestMethod.POST,"查询设备下的用户信息"),
    ADD_DEVICE_BATCH("/v2/product/{product_id}/device_import_batch_2",RequestMethod.POST,"批量添加设备");

    // 接口地址
    private String url;
    // 请求类型
    private RequestMethod requestMethod;
    // 接口描述
    private String description;

    public String getUrl( ) {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RequestMethod getRequestMethod( ) {
        return requestMethod;
    }

    public void setRequestMethod(RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getDescription( ) {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    KilinkApiEnum(String url){
        this.url = url;
    }

    KilinkApiEnum(String url, RequestMethod requestMethod, String description){
        this.url = url;
    }
}
