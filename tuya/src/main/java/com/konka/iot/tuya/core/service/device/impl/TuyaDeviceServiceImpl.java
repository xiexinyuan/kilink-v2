package com.konka.iot.tuya.core.service.device.impl;

import com.konka.iot.baseframe.common.core.service.BaseService;
import com.konka.iot.baseframe.common.utils.JsonUtil;
import com.konka.iot.tuya.config.TuyaConfig;
import com.konka.iot.tuya.core.service.device.TuyaDeviceService;
import com.konka.iot.tuya.enums.TuyaApiEnum;
import com.konka.iot.tuya.model.device.DeviceTokenReqModel;
import com.konka.iot.tuya.model.device.StatisticAccumulate;
import com.konka.iot.tuya.model.device.StatisticType;
import com.tuya.api.TuyaClient;
import com.tuya.api.model.Command;
import com.tuya.api.model.domain.device.*;
import com.tuya.api.model.enums.HttpMethod;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-11 15:47
 * @Description 设备对接
 */
@Service
public class TuyaDeviceServiceImpl extends BaseService implements TuyaDeviceService {

    @Autowired
    private TuyaClient tuyaClient;
    @Autowired
    private TuyaConfig tuyaConfig;

    @Override
    public List<DeviceVo> getDevices(String userId) throws Exception {

        return tuyaClient.getUserDevices(userId);
    }

    @Override
    public DeviceToken getDeviceToken(DeviceTokenReqModel deviceTokenReqModel) throws Exception {
        return tuyaClient.getDeviceToken(deviceTokenReqModel.getUid(),
                deviceTokenReqModel.getTimeZoneId(),
                deviceTokenReqModel.getLon(),
                deviceTokenReqModel.getLat(),
                deviceTokenReqModel.getLang());
    }

    @Override
    public DeviceResultOfToken getConfigNetworkDevices(String token) throws Exception {
        return tuyaClient.getDeviceListOfToken(token);
    }

    @Override
    public Boolean enabledSub(String deviceId, Integer... duration) throws Exception {
        String url = tuyaConfig.getCn_url() + TuyaApiEnum.ENABLE_SUB_DISCOVERY.getUrl().replace("{device_id}", deviceId);
        if(duration != null){
            url = url.concat("?duration="+ duration);
        }
        String result = tuyaClient.commonHttpRequest(url, HttpMethod.PUT, null,null);
        return Boolean.parseBoolean(result);
    }

    @Override
    public List<DeviceVo> listSub(String deviceId, Long discoveryTime) throws Exception {
        String url = tuyaConfig.getCn_url() + TuyaApiEnum.LIST_SUB.getUrl().replace("{device_id}", deviceId);
        if(discoveryTime != null){
            url = url.concat("?discovery_time="+ discoveryTime);
        }
        String result = tuyaClient.commonHttpRequest(url, HttpMethod.GET, null,null);
        return JsonUtil.string2Obj(result, new TypeReference<List<DeviceVo>>() {});
    }

    @Override
    public DeviceVo getDeviceInfo(String deviceId) throws Exception {

        return tuyaClient.getDeviceInfo(deviceId);
    }
    

    @Override
    public BatchDevices getSchemaDevices() throws Exception {
        String url = tuyaConfig.getCn_url() + TuyaApiEnum.LIST_DEVICE.getUrl().concat("?schema="+ tuyaConfig.getSchema());
        String result = tuyaClient.commonHttpRequest(url, HttpMethod.GET, null,null);
        return JsonUtil.string2Obj(result, BatchDevices.class);
    }

    @Override
    public List<DeviceVo> subDevices(String deviceId) throws Exception {
        String url = TuyaApiEnum.SUB_DEVICES.getUrl().replace("{device_id}", deviceId);
        String result = tuyaClient.commonHttpRequest(url, HttpMethod.GET, null,null);
        return JsonUtil.string2Obj(result, new TypeReference<List<DeviceVo>>() {});
    }


    @Override
    public CategoryFunctions functionsByCategory(String category) throws Exception {
        return tuyaClient.getFunctionByCategory(category);
    }

    @Override
    public CategoryFunctions functionsByDevice(String deviceId) throws Exception {
        return tuyaClient.getFunctionsByDevId(deviceId);
    }

    @Override
    public Boolean deviceCommand(String deviceId, List<Command> commands) throws Exception {
        return tuyaClient.postDeviceCommand(deviceId, commands);
    }

    @Override
    public Boolean editName(String deviceId, Map<String, Object> name) throws Exception {
        String url = tuyaConfig.getCn_url() + TuyaApiEnum.EDIT_DEVICE.getUrl().replace("{device_id}", deviceId);
        String result = tuyaClient.commonHttpRequest(url, HttpMethod.PUT, null,name);
        return Boolean.parseBoolean(result);
    }

    @Override
    public Boolean editSubName(String deviceId, String functionCode, Map<String, Object> name) throws Exception {
        String url = tuyaConfig.getCn_url() + TuyaApiEnum.EDIT_SUB_DEVICE.getUrl().replace("{device_id}", deviceId).replace("{function_code}", functionCode);
        String result = tuyaClient.commonHttpRequest(url, HttpMethod.PUT, null,name);
        return Boolean.parseBoolean(result);
    }

    @Override
    public Boolean reset(String deviceId) throws Exception {
        String url = tuyaConfig.getCn_url() + TuyaApiEnum.REST_DEVICE.getUrl().replace("{device_id}", deviceId);
        String result = tuyaClient.commonHttpRequest(url, HttpMethod.PUT, null,null);
        return Boolean.parseBoolean(result);
    }

    @Override
    public Boolean remove(String deviceId) throws Exception {
        String url = tuyaConfig.getCn_url() + TuyaApiEnum.DEL_DEVICE.getUrl().replace("{device_id}", deviceId);
        String result = tuyaClient.commonHttpRequest(url, HttpMethod.DELETE, null,null);
        return Boolean.parseBoolean(result);
    }

    @Override
    public List<Status> getDeviceStatus(String deviceId) throws Exception {
        return tuyaClient.getDeviceStatus(deviceId);
    }

    @Override
    public List<StatisticType> getAllStatisticType(String deviceId) throws Exception {
        String url = tuyaConfig.getCn_url() + TuyaApiEnum.DEVICE_ALL_STATISTIC.getUrl().replace("{device_id}", deviceId);
        String result = tuyaClient.commonHttpRequest(url, HttpMethod.GET, null,null);
        return JsonUtil.string2Obj(result, new TypeReference<List<StatisticType>>() {});
    }

    @Override
    public StatisticAccumulate getStatisticAccumulate(String deviceId, String code) throws Exception {
        String url = tuyaConfig.getCn_url() + TuyaApiEnum.DEVICE_STATISTIC_ACCUMULATE.getUrl().replace("{device_id}", deviceId);
        if(code != null && !"".equals(code)){
            url = url.concat("?code="+ code);
        }
        String result = tuyaClient.commonHttpRequest(url, HttpMethod.GET, null,null);
        return JsonUtil.string2Obj(result, StatisticAccumulate.class);
    }
}
