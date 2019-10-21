package com.konka.iot.tuya.core.service.scene.impl;

import com.konka.iot.baseframe.common.core.service.BaseService;
import com.konka.iot.baseframe.common.utils.JsonUtil;
import com.konka.iot.tuya.config.TuyaConfig;
import com.konka.iot.tuya.core.service.scene.SceneService;
import com.konka.iot.tuya.model.scene.SceneRespModel;
import com.konka.iot.tuya.enums.TuyaApiEnum;
import com.tuya.api.TuyaClient;
import com.tuya.api.model.enums.HttpMethod;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-12 15:47
 * @Description 场景管理
 */
@Service
public class SceneServiceImpl extends BaseService implements SceneService {

    @Autowired
    private TuyaClient tuyaClient;
    @Autowired
    private TuyaConfig tuyaConfig;

    @Override
    public List<SceneRespModel> getScenes(String homeId) throws Exception {
        String url = TuyaApiEnum.GET_SCENES.getUrl().replace("{home_id}", homeId);
        String result = tuyaClient.commonHttpRequest(url, HttpMethod.GET, null,null);
        return JsonUtil.string2Obj(result, new TypeReference<List<SceneRespModel>>() {});
    }
}

