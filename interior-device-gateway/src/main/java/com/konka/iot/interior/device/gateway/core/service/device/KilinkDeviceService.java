package com.konka.iot.interior.device.gateway.core.service.device;

import com.konka.iot.interior.device.gateway.model.DeviceActiveResultModel;
import com.konka.iot.interior.device.gateway.model.DeviceAddModel;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-11-19 18:07
 * @Description TODO
 */
public interface KilinkDeviceService {
    /**
     * 添加设备(电视机)
     * @param g_deviceId
     * @param deviceAddModel
     * @return
     * @throws Exception
     */
    boolean addDevice(String g_deviceId, DeviceAddModel deviceAddModel) throws Exception;

    /**
     * 保存设备激活记录
     * @param deviceId
     * @param code
     * @param message
     * @param isActive
     */
    void saveActiveRecord(String deviceId, int code, String message, boolean isActive);

    /**
     * 获取设备激活记录信息
     * @return
     * @throws Exception
     */
    DeviceActiveResultModel getResultModel( ) throws Exception;


    /**
     * 处理队列
     */
    void clearQueue();
}
