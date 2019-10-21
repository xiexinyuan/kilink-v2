package com.konka.iot.kilink.cloud.support.core.dao.device;

import com.konka.iot.kilink.cloud.support.api.model.device.DataponitMapping;
import com.konka.iot.kilink.cloud.support.api.model.device.DeviceMapping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-20 11:05
 * @Description TODO
 */
public interface DeviceDao {

    void addDeviceMapping(@Param("kDeviceId") String kDeviceId, @Param("tDeviceId") String tDeviceId) throws Exception;

    String findTuyaDeviceId(@Param("kDeviceId") String kDeviceId) throws Exception;

    List<DeviceMapping> findDeviceMapping(@Param("tDeviceIds") List<String> tDeviceIds) throws Exception;

    List<DataponitMapping> findThridDatapointMapping(@Param("tpid") String tPid) throws Exception;

}
