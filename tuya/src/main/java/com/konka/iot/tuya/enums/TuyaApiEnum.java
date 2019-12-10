package com.konka.iot.tuya.enums;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-11 17:15
 * @Description 涂鸦api接口地址枚举
 */
public enum  TuyaApiEnum {


    ENABLE_SUB_DISCOVERY("/v1.0/devices/{device_id}/enabled-sub-discovery","开放网关允许子设备入网"),
    LIST_SUB("/v1.0/devices/{device_id}/list-sub","获取入网子设备列表"),
    SUB_DEVICES("/v1.0/devices/{device_id}/sub-devices","获取网关下的子设备列表"),
    LIST_DEVICE("/v1.0/devices","获取设备列表"),
    EDIT_DEVICE("/v1.0/devices/{device_id}","修改设备名称"),
    DEL_DEVICE("/v1.0/devices/{device_id}","移除设备"),
    EDIT_SUB_DEVICE("/v1.0/devices/{device_id}/functions/{function_code}","修改子设备名称"),
    REST_DEVICE("/v1.0/devices/{device_id}/reset-factory","恢复设备出厂设置"),
    GET_SCENES("/v1.0/homes/{home_id}/scenes","查询家庭下的场景列表"),
    DEVICE_ALL_STATISTIC("/v1.0/devices/{device_id}/all-statistic-type","获取设备支持的统计类型"),
    DEVICE_STATISTIC_ACCUMULATE("/v1.0/devices/{device_id}/all-statistic-accumulate","获取历史累计值");

    //接口地址
    private String url;
    //接口描述
    private String description;

    public String getUrl( ) {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription( ) {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    TuyaApiEnum(String url){
        this.url = url;
    }

    TuyaApiEnum(String url, String description){
        this.url = url;
    }
}
