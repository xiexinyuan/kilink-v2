package com.konka.iot.tuya.enums;

/**
 * @Author xiexinyuan
 * @Date 2019-09-09 14:59
 * @Description 涂鸦错误码枚举类
 **/

public enum TuyaErrorCodeEnum {

    SYSTEM_ERROR(500,	"system error,please contact the admin", "系统错误，请联系管理员"),
    DATANOT_EXIST(1000,	"data not exist", "数据不存在"),
    SECRET_INVALID(1001,	"secret invalid", "密钥非法"),
    ACCESS_TOKEN_IS_NULL(1002,	"access_token is null", "访问令牌为空"),
    GRANT_TYPE_INVALID(1003,	"grant type invalid", "grant type 无效"),
    SIGN_INVALID(1004,	"sign invalid", "签名无效"),
    APPKEY_INVALID(1005,	"appkey invalid", "appkey 无效"),
    NOT_SUPPORT_CONTENT_TYPE(1006,	"not support content type", "不支持的content type"),
    NOT_SUPPORT_APPKEY(1007,	"not support appkey", "不支持的appkey，请使用云端key"),
    TOKEN_IS_EXPIRED(1010,	"token is expired", "token过期"),
    TOKEN_INVALID(1011,	"token invalid", "token无效"),
    TOKEN_STATUS_IS_INVALID(1012,	"token status is invalid", "token状态无效"),
    REQUEST_TIME_IS_INVALID(1013,	"request time is invalid", "请求时间无效"),
    PARAMS_IS_EMPTY(1100,	"params is empty", "参数为空"),
    PARAMS_RANGE_INVALID(1101,	"params range invalid", "参数范围无效"),
    PARAMS_IS_NULL(1102,	"params is null", "参数为null"),
    COMMANDS_ISSUE_ERROR(1103,	"commands issue error", "指令下发失败"),
    TYPE_IS_INCORRECT(1104,	"type is incorrect", "类型不正确"),
    MISSING_THE_HEADER(1105,	"missing the header", "缺少header"),
    PERMISSION_DENY(1106,	"permission deny", "权限非法"),
    CODE_INVALID(1107,	"code invalid", "code无效"),
    DEVICE_IS_OFFLINE(2001,	"device is offline", "设备离线"),
    NOT_DEVICES(2002,	"this user dose not have any devices", "用户账号下设备为空"),
    FUNCTION_NOT_SUPPORT(2003,	"function not support", "指令不支持"),
    NOT_SUPPORT_THE_LOCK_TYPE(2004,	"not support the lock type", "不支持的锁类型"),
    PRODUCT_NOT_EXIST(2005,	"product not exist", "产品不存在"),
    USER_NOT_EXIST(2006,	"user not exist", "用户不存在"),
    DEVICE_TOKEN_EXPIRED(2007,	"device token expired", "设备token过期"),
    COMMAND_OR_VALUE_NOT_SUPPORT(2008,	"command or value not support", "指令或值不支持"),
    NOT_SUPPORT_THIS_DEVICE(2009,	"not support this device", "不支持此类设备"),
    DEVICE_NOT_EXIST(2010,	"device not exist", "设备不存在"),
    APPLICATION_NOT_SUPPORT(2012,	"application not support", "应用不存在"),
    ADD_TIMER_FAILED(2013,	"add timer failed", "添加定时任务失败"),
    NOT_HAVE_ANY_TIMERS(2014,	"this device dose not have any timers", "设备没有任何定时任务"),
    THIS_CATEGORY_IS_NOT_SUPPORTED(2015,	"this category is not supported", "该分类不支持");

    private int code;
    private String msg;
    private String description;

    public int getCode( ) {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg( ) {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDescription( ) {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    TuyaErrorCodeEnum(int code, String msg, String description){
        this.code = code;
        this.msg = msg;
        this.description = description;
    }
}
