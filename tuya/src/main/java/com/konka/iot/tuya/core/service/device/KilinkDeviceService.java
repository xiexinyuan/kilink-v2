package com.konka.iot.tuya.core.service.device;

import com.konka.iot.tuya.model.device.DeviceActiveResultModel;
import com.konka.iot.tuya.model.device.DeviceAddModel;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-20 10:42
 * @Description kilink设备管理
 */
public interface KilinkDeviceService {
    /**
     * 添加虚拟设备
     * @param deviceAddModel
     * @throws Exception
     */
    void add(DeviceAddModel deviceAddModel) throws Exception;

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
