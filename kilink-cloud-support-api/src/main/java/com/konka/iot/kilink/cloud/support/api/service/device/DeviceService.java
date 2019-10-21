package com.konka.iot.kilink.cloud.support.api.service.device;

import com.konka.iot.kilink.cloud.support.api.model.device.DataponitMapping;
import com.konka.iot.kilink.cloud.support.api.model.device.DeviceMapping;
import com.konka.iot.kilink.cloud.support.api.model.device.DeviceModel;
import com.konka.iot.kilink.cloud.support.api.model.device.DeviceStatusModel;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-20 16:33
 * @Description TODO
 */
public interface DeviceService {

    /**
     * 添加虚拟设备
     * @param pId
     * @param deviceModel
     * @throws Exception
     */
    DeviceModel add(String pId, DeviceModel deviceModel) throws Exception;

    /**
     * 批量添加设备
     * @param pId
     * @param devices
     * @throws Exception
     */
    void batchDevice(String pId, List<DeviceModel> devices) throws Exception;

    /**
     * 查询网关下子设备(分页)
     * @param gatewayId 网关ID
     * @param pageNo 页码
     * @param pageSieze 页大小
     * @return DeviceStatusModel
     * @throws Exception
     */
    List<DeviceStatusModel> listGatewayDevice(String gatewayId, AtomicInteger pageNo, int pageSieze) throws Exception;

    /**
     * 绑定第三方设备和虚拟设备的映射
     * @param kDeviceId
     * @param tDeviceId
     * @throws Exception
     */
    void addDeviceMapping(String kDeviceId, String tDeviceId)throws Exception;

    /**
     * 获取虚拟设备对应的涂鸦设备ID
     * @param kDeviceId 虚拟设备ID
     * @return 涂鸦设备ID
     * @throws Exception
     */
    String findTuyaDeviceId(String kDeviceId) throws Exception;


    /**
     * 获取虚拟设备和的涂鸦设备的映射关系列表
     * @param tDeviceIds 虚拟设备ID集合
     * @return 映射关系列表
     * @throws Exception
     */
    List<DeviceMapping> findDeviceMapping(List<String> tDeviceIds) throws Exception;

    /**
     * 获取第三方产品数据端点映射
     * @param tPid 第三方产品id
     * @return
     * @throws Exception
     */
    List<DataponitMapping> findThridDatapointMapping(String tPid) throws Exception;

    /**
     * 通过二维码绑定设备
     * @param userId
     * @param productId
     * @param deviceId
     * @return
     * @throws Exception
     */
    boolean bindByQrcode(String userId, String productId, String deviceId, String accessToken) throws Exception;
}
