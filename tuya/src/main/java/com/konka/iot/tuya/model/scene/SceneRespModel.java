package com.konka.iot.tuya.model.scene;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-12 15:15
 * @Description TODO
 */
@Data
@ApiModel(value="sceneRespModel",description="家庭场景响应实体")
public class SceneRespModel {
    @ApiModelProperty(value="场景ID",name="scene_id")
    private String scene_id;

    @ApiModelProperty(value="场景名称",name="scene_name")
    private String scene_name;

    @ApiModelProperty(value="背景图片",name="background")
    private String background;

    @ApiModelProperty(value="动作列表",name="actions")
    private List<SceneAction> actions;
}

@Data
@ApiModel(value="sceneAction",description="场景动作实体")
class SceneAction{
    @ApiModelProperty(value="执行动作设备ID",name="entity_id")
    private String entity_id;
    @ApiModelProperty(value="执行动作参数 json字符串",name="executor_property")
    private String executor_property;
    @ApiModelProperty(value="执行动作类别",name="action_executor")
    private String action_executor;
}
