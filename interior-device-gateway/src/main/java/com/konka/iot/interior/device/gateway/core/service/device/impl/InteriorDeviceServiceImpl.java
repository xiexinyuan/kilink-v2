package com.konka.iot.interior.device.gateway.core.service.device.impl;

import com.konka.iot.baseframe.common.core.service.BaseService;
import com.konka.iot.baseframe.common.exception.DataCheckException;
import com.konka.iot.baseframe.common.model.HttpResult;
import com.konka.iot.baseframe.common.utils.HttpUtil;
import com.konka.iot.baseframe.common.utils.JsonUtil;
import com.konka.iot.interior.device.gateway.config.InteriorConfig;
import com.konka.iot.interior.device.gateway.core.service.device.InteriorDeviceService;
import com.konka.iot.interior.device.gateway.model.ApiRequestHeader;
import com.konka.iot.interior.device.gateway.model.ApiRequestModel;
import com.konka.iot.interior.device.gateway.model.DeviceModel;
import com.konka.iot.interior.device.gateway.model.InteriorDeviceCommand;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-11-21 16:42
 * @Description TODO
 */
@Service
public class InteriorDeviceServiceImpl extends BaseService implements InteriorDeviceService {
    @Autowired
    private InteriorConfig interiorConfig;
    @Autowired
    private HttpUtil httpUtil;
    @Override
    public boolean bindDevice(String token, String userId, String deviceId) throws Exception {
        Map<String, String> payload = new HashMap<>(1);
        payload.put("devId", deviceId);
        String reqParams = createRequst("BindDevice", userId, payload);

        Map<String, Object> headerParams = new HashMap<>(1);
        headerParams.put("Authorization", token );
        HttpResult result = httpUtil.doPost(interiorConfig.getTestUrl(), reqParams, headerParams);
        if(result.getCode() == 200){
            return true;
        }else {
            errorMsg(result.getBody(), "绑定设备失败");
        }
        return false;
    }

    @Override
    public List<DeviceModel> getDevices(String token, String userId, String... devIds) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        if(devIds != null && devIds.length > 0){
            if(devIds.length > 50){
                throw new DataCheckException(500, "最多操作50个设备");
            }
            payload.put("devIds", devIds);
        }
        String reqParams = createRequst("DeviceDiscovery", userId, payload);

        Map<String, Object> headerParams = new HashMap<>(1);
        headerParams.put("Authorization", token );
        HttpResult result = httpUtil.doPost(interiorConfig.getTestUrl(), reqParams, headerParams);
        if(result.getCode() == 200){
            return JsonUtil.string2Obj(result.getBody(), new TypeReference<List<DeviceModel>>() {});
        }else {
            errorMsg(result.getBody(), "获取设备列表失败");
        }
        return null;
    }

    @Override
    public List<DeviceModel> getDevicesSnapshot(String token, String userId,String[] devIds) throws Exception {
        if(devIds != null && devIds.length > 50){
            throw new DataCheckException(500, "最多操作50个设备");
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("devIds", devIds);
        String reqParams = createRequst("DeviceSnapshot", userId, payload);
        Map<String, Object> headerParams = new HashMap<>(1);
        headerParams.put("Authorization", token );
        HttpResult result = httpUtil.doPost(interiorConfig.getTestUrl(), reqParams, headerParams);
        if(result.getCode() == 200){
            return JsonUtil.string2Obj(result.getBody(), new TypeReference<List<DeviceModel>>() {});
        }else {
            errorMsg(result.getBody(), "获取设备快照失败");
        }
        return null;
    }

    @Override
    public boolean commandDevice(String token, String userId,InteriorDeviceCommand command) throws Exception {
        if(command == null){
            throw new DataCheckException(500, "设备控制指令为空");
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("command", command);
        String reqParams = createRequst("DeviceCommand", userId, payload);
        Map<String, Object> headerParams = new HashMap<>(1);
        headerParams.put("Authorization", token );
        HttpResult result = httpUtil.doPost(interiorConfig.getTestUrl(), reqParams, headerParams);
        if(result.getCode() == 200){
            return true;
        }else {
            errorMsg(result.getBody(), "获取设备快照失败");
        }
        return false;
    }

    private <T> String createRequst(String nameSpace, String userId, T payload){
        ApiRequestHeader header = new ApiRequestHeader();
        header.setNamespace(nameSpace);
        header.setThirdPartyId(interiorConfig.getAppId());
        header.setGranteeId(userId);
        header.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        header.setTimestamp(System.currentTimeMillis());
        ApiRequestModel<T> model = new ApiRequestModel();
        model.setHeader(header);
        model.setPayload(payload);
        return JsonUtil.obj2String(model);
    }

    private void errorMsg(String resultBody, String message) throws DataCheckException {
        resultBody = resultBody.replace("\'","");
        ApiRequestModel<Map<String, Object>> model = JsonUtil.string2Obj(resultBody, new TypeReference<ApiRequestModel<Map<String, Object>>>(){});
        Map<String, Object> errorMap = model.getPayload();
        logger.error(message + "：{}", errorMap);
        int code = Integer.parseInt(errorMap.get("code") + "");
        throw new DataCheckException(code, message + ": "+ errorMap.get("description"));
    }
}


