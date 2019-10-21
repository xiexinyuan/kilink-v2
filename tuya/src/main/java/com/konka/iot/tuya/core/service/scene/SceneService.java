package com.konka.iot.tuya.core.service.scene;

import com.konka.iot.tuya.model.scene.SceneRespModel;

import java.util.List;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-12 15:11
 * @Description 智能场景控制
 */
public interface SceneService {
    /** 获取家庭场景列表
     * @author xiexinyuan
     * @date 2019/9/12 15:13
     * @param homeId
     * @return List<SceneRespModel>
     * @throws Exception
    */
    List<SceneRespModel> getScenes(String homeId) throws Exception;
}
