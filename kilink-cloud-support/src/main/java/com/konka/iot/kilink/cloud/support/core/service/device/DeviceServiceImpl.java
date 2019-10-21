package com.konka.iot.kilink.cloud.support.core.service.device;

import com.alibaba.dubbo.config.annotation.Service;
import com.konka.iot.baseframe.common.core.service.BaseService;
import com.konka.iot.baseframe.common.utils.JsonUtil;
import com.konka.iot.baseframe.common.utils.RedisUtil;
import com.konka.iot.kilink.cloud.support.api.model.device.DataponitMapping;
import com.konka.iot.kilink.cloud.support.api.model.device.DeviceMapping;
import com.konka.iot.kilink.cloud.support.api.model.device.DeviceModel;
import com.konka.iot.kilink.cloud.support.api.model.device.DeviceStatusModel;
import com.konka.iot.kilink.cloud.support.api.service.device.DeviceService;
import com.konka.iot.kilink.cloud.support.config.Redis.TuyaRediskeyConfig;
import com.konka.iot.kilink.cloud.support.config.dataSource.DataSourceKey;
import com.konka.iot.kilink.cloud.support.config.dataSource.TargetDataSource;
import com.konka.iot.kilink.cloud.support.core.dao.device.DeviceDao;
import com.konka.iot.kilink.cloud.support.core.service.KilinkServiceUtil;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-20 16:44
 * @Description TODO
 */
@Service
public class DeviceServiceImpl extends BaseService implements DeviceService {

    @Autowired
    private KilinkServiceUtil kilinkServiceUtil;

    @Autowired
    private DeviceDao deviceDao;

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private TuyaRediskeyConfig tuyaRediskeyConfig;

    @Override
    public DeviceModel add(String pId, DeviceModel deviceModel) throws Exception {
        return kilinkServiceUtil.add(pId, deviceModel);
    }

    @Override
    public void batchDevice(String pId, List<DeviceModel> devices) throws Exception {
        kilinkServiceUtil.batchDevice(pId, devices);
    }

    @Override
    public List<DeviceStatusModel> listGatewayDevice(String gatewayId, AtomicInteger pageNo, int pageSieze) throws Exception {
        return kilinkServiceUtil.listGatewayDevice(gatewayId, pageNo, pageSieze);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TargetDataSource(name = DataSourceKey.DB_KILINK)
    public void addDeviceMapping(String kDeviceId, String tDeviceId) throws Exception {
        deviceDao.addDeviceMapping(kDeviceId, tDeviceId);
    }

    @Override
    @TargetDataSource(name = DataSourceKey.DB_KILINK)
    public List<DeviceMapping> findDeviceMapping(List<String> tDeviceIds) throws Exception {

        String prefix = tuyaRediskeyConfig.getDevice_mapping_prefix();
        List<DeviceMapping> deviceMappings = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        for (String deviceId: tDeviceIds){
            keys.add(prefix.concat(deviceId));
        }
        List<Object> deviceMappingsRedis = redisUtil.get(keys);
        // 移除null元素
        deviceMappingsRedis.removeAll(Collections.singleton(null));

        if(deviceMappingsRedis != null && !deviceMappingsRedis.isEmpty()){
            for (Object object: deviceMappingsRedis) {
                deviceMappings.add(JsonUtil.string2Obj(object.toString(), DeviceMapping.class));
            }
        }else{
            deviceMappings = deviceDao.findDeviceMapping(tDeviceIds);
            for (DeviceMapping deviceMapping : deviceMappings) {
                redisUtil.set(prefix.concat(deviceMapping.getTDeviceId()), JsonUtil.obj2String(deviceMapping));
            }
        }

        return deviceMappings;
    }

    @Override
    @TargetDataSource(name = DataSourceKey.DB_KILINK)
    public String findTuyaDeviceId(String kDeviceId) throws Exception {
        String tDeviceId = null;
        Object deviceMapping = redisUtil.get(tuyaRediskeyConfig.getDevice_mapping_prefix().concat(kDeviceId));
        if(deviceMapping != null){
            Map<String, String> deviceMappingMap = JsonUtil.string2Obj((String) deviceMapping, new TypeReference<Map<String, String>>() {});
            tDeviceId = deviceMappingMap.get(kDeviceId);
        }else {
            tDeviceId = deviceDao.findTuyaDeviceId(kDeviceId);
            // 将绑定关系放入redis
            Map<String, String> params = new HashMap<>();
            params.put(kDeviceId, tDeviceId);
            redisUtil.set(tuyaRediskeyConfig.getDevice_mapping_prefix().concat(kDeviceId), JsonUtil.obj2String(params));
        }
        return tDeviceId;
    }

    @Override
    @TargetDataSource(name = DataSourceKey.DB_KILINK)
    public List<DataponitMapping> findThridDatapointMapping(String tPid) throws Exception {

        List<DataponitMapping> dataponitMappings = null;
        String dataponitMappingRedis = (String) redisUtil.get(tuyaRediskeyConfig.getDatapoint_mapping_prefix().concat(tPid));

        if(dataponitMappingRedis != null){
            dataponitMappings = JsonUtil.string2Obj(dataponitMappingRedis, new TypeReference<List<DataponitMapping>>() {});
        }else {
            dataponitMappings = deviceDao.findThridDatapointMapping(tPid);
            redisUtil.set(tuyaRediskeyConfig.getDatapoint_mapping_prefix().concat(tPid), JsonUtil.obj2String(dataponitMappings));
        }
        return dataponitMappings;
    }

    @Override
    public boolean bindByQrcode(String userId, String productId, String deviceId, String accessToken) throws Exception {

        return kilinkServiceUtil.bindByQrcode(userId, productId, deviceId, accessToken);
    }
}
