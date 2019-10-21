package com.konka.iot.tuya.core.service.device;

import com.konka.iot.tuya.model.device.DeviceTokenReqModel;
import com.tuya.api.model.Command;
import com.tuya.api.model.domain.device.*;

import java.util.List;
import java.util.Map;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-11 10:31
 * @Description 设备对接
 */
public interface TuyaDeviceService {

    /** 获取用户下的设备列表
     * @author xiexinyuan
     * @date 2019/9/11 10:32
     * @param userId 用户ID
     * @return List<DeviceVo>
     * @throws Exception
     */
    List<DeviceVo> getDevices(String userId) throws Exception;


    /** 生成配网令牌
     * @author xiexinyuan
     * @date 2019/9/11 10:32
     * @param deviceTokenReqModel 设备token请求实体
     * @return DeviceToken
     * @throws Exception
     */
    DeviceToken getDeviceToken(DeviceTokenReqModel deviceTokenReqModel) throws Exception;

    /** 获取设备配网列表
     * @author xiexinyuan
     * @date 2019/9/11 10:32
     * @param token 设备配网token
     * @return DeviceResultOfToken
     * @throws Exception
     */
    DeviceResultOfToken getConfigNetworkDevices(String token) throws Exception;

    /** 开放网关允许子设备入网
     * @author xiexinyuan
     * @date 2019/9/11 10:32
     * @param deviceId 设备ID
     * @param duration 网关发现时间 默认100s,最大3600s,0为停止发现 可以不填
     * @return Boolean
     * @throws Exception
     */
    Boolean enabledSub(String deviceId, Integer... duration) throws Exception;

    /** 获取入网子设备列表
     * @author xiexinyuan
     * @date 2019/9/11 10:32
     * @param deviceId 设备ID
     * @param discoveryTime 网关发现子设备时间 精确到S
     * @return List<DeviceVo>
     * @throws Exception
     */
    List<DeviceVo> listSub(String deviceId, long discoveryTime) throws Exception;

    /** 获取设备详情
     * @author xiexinyuan
     * @date 2019/9/11 10:32
     * @param deviceId 设备ID
     * @return DeviceVo
     * @throws Exception
     */
    DeviceVo getDeviceInfo(String deviceId) throws Exception;

    /** 获取应用下设备列表
     * @author xiexinyuan
     * @date 2019/9/11 10:32
     * @return BatchDevices
     * @throws Exception
     */
    BatchDevices getSchemaDevices() throws Exception;

    /** 获取网关下的子设备列表
     * @author xiexinyuan
     * @date 2019/9/11 10:32
     * @param deviceId 网关设备ID
     * @return List<DeviceVo>
     * @throws Exception
     */
    List<DeviceVo> subDevices(String deviceId) throws Exception;


    /** 按品类获取指令集
     * @author xiexinyuan
     * @date 2019/9/11 10:32
     * @param category 品类名称
     * @return CategoryFunctions
     * @throws Exception
     */
    CategoryFunctions functionsByCategory(String category) throws Exception;


    /** 按设备获取指令集
     * @author xiexinyuan
     * @date 2019/9/11 10:32
     * @param deviceId 设备ID
     * @return CategoryFunctions
     * @throws Exception
     */
    CategoryFunctions functionsByDevice(String deviceId) throws Exception;

    /** 设备控制
     * @author xiexinyuan
     * @date 2019/9/12 14:49
     * @param deviceId 设备ID
     * @param commands 控制指令集
     * @return Boolean
     * @throws Exception
    */
    Boolean deviceCommand(String deviceId, List<Command> commands) throws Exception;

    /** 修改设备名称
     * @author xiexinyuan
     * @date 2019/9/12 14:49
     * @param deviceId 设备ID
     * @param name 设备名称
     * @return Boolean
     * @throws Exception
     */
    Boolean editName(String deviceId, Map<String, Object> name) throws Exception;

    /** 修改子设备名称
     * @author xiexinyuan
     * @date 2019/9/12 14:49
     * @param deviceId 设备ID
     * @param functionCode 子设备ID
     * @param name 子设备名称
     * @return Boolean
     * @throws Exception
     */
    Boolean editSubName(String deviceId, String functionCode, Map<String, Object> name) throws Exception;


    /** 恢复设备出厂设备
     * @author xiexinyuan
     * @date 2019/9/12 14:49
     * @param deviceId 设备ID
     * @return Boolean
     * @throws Exception
     */
    Boolean reset(String deviceId) throws Exception;

    /** 移除设备
     * @author xiexinyuan
     * @date 2019/9/12 14:49
     * @param deviceId 设备ID
     * @return Boolean
     * @throws Exception
     */
    Boolean remove(String deviceId) throws Exception;

    /**
     * 获取设备最新状态
     * @param deviceId
     * @return
     * @throws Exception
     */
    List<Status> getDeviceStatus(String deviceId) throws Exception;
}
