package com.konka.iot.tuya.model.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-11 09:32
 * @Description 涂鸦注册用户实体
 */
@Data
@Builder
@ApiModel(value="user",description="同步用户请求实体")
public class User implements Serializable {

    @ApiModelProperty(value="用户名",name="userName",example="lisi",required = true)
    private String userName;
    @ApiModelProperty(value="密码",name="passWord",example="123456",required = true)
    private String passWord;
    @ApiModelProperty(value="用户名类型 1:mobile,2:email,3:username,默认:3",name="passWord",example="1",required = true)
    private Integer userNameType;
    @ApiModelProperty(value="昵称",name="passWord",example="jack")
    private String nickName;
}
