package com.konka.iot.tuya.model.device;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-11 10:47
 * @Description 设备token请求实体
 */
@Data
@Builder
@ApiModel(value="deviceTokenReqModel",description="生成设备配网令牌请求实体")
public class DeviceTokenReqModel implements Serializable {
    @ApiModelProperty(value="用户唯一标识",name="uid",example="123",required = true)
    private String uid;
    @ApiModelProperty(value="用户所在时区ID,用于设置夏令时",name="timeZoneId",example="Asia/Shanghai",required = true)
    private String timeZoneId;
    @ApiModelProperty(value="家庭ID 不填则为默认家庭",name="owner_id",example="123")
    private String owner_id;
    @ApiModelProperty(value="经度",name="lon")
    private String lon;
    @ApiModelProperty(value="纬度",name="lat")
    private String lat;
    @ApiModelProperty(value="系统语言 zh eu等 默认zh",name="lang",example = "zh")
    private String lang;
}
