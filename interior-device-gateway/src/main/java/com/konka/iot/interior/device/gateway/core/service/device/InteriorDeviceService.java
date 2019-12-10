package com.konka.iot.interior.device.gateway.core.service.device;

import com.konka.iot.interior.device.gateway.model.DeviceModel;
import com.konka.iot.interior.device.gateway.model.InteriorDeviceCommand;

import java.util.List;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-11-21 11:17
 * @Description TODO
 */
public interface InteriorDeviceService {
    /**
     * 绑定设备(电视机)
     * @param token
     * @param userId
     * @param deviceId
     * @throws Exception
     */
    boolean bindDevice(String token, String userId, String deviceId) throws Exception;


    /**
     * 获取设备列表
     * @param userId
     * @param devIds(可选参数,最大长度50)
     * @return
     * @throws Exception
     */
    List<DeviceModel> getDevices(String token, String userId, String ...devIds) throws Exception;


    /**
     * 获取设备快照
     * @param userId
     * @param devIds
     * @return
     * @throws Exception
     */
    List<DeviceModel> getDevicesSnapshot(String token, String userId, String [] devIds) throws Exception;


    /**
     * 设备控制
     * @param userId
     * @param command
     * @return
     * @throws Exception
     */
    boolean commandDevice(String token, String userId, InteriorDeviceCommand command) throws Exception;
    
}
