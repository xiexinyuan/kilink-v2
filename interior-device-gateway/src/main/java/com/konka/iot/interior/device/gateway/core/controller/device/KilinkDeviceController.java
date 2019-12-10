package com.konka.iot.interior.device.gateway.core.controller.device;

import com.alibaba.dubbo.config.annotation.Reference;
import com.konka.iot.baseframe.common.config.ErrorCodeEnum;
import com.konka.iot.baseframe.common.exception.DataCheckException;
import com.konka.iot.baseframe.common.model.ResponseModel;
import com.konka.iot.baseframe.common.utils.RedisUtil;
import com.konka.iot.interior.device.gateway.config.GatewayConfig;
import com.konka.iot.interior.device.gateway.core.service.device.InteriorDeviceService;
import com.konka.iot.interior.device.gateway.core.service.device.KilinkDeviceService;
import com.konka.iot.interior.device.gateway.model.DeviceAddModel;
import com.konka.iot.kilink.cloud.support.api.service.device.DeviceService;
import com.konka.iot.kilink.cloud.support.api.service.product.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-11 10:30
 * @Description 设备对接
 */
@Api(tags = {"kilink设备对接操作接口"})
@RestController
@RequestMapping("/v1/device/kilink")
public class KilinkDeviceController {

    private static final Logger logger = LoggerFactory.getLogger(KilinkDeviceController.class);

    @Autowired
    private KilinkDeviceService kilinkDeviceService;

    @Autowired
    private InteriorDeviceService interiorDeviceService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private GatewayConfig gatewayConfig;

    @Reference
    private DeviceService deviceService;
    @Reference
    private ProductService productService;

    @PostMapping(value = "/bind")
    @ApiOperation(value = "绑定设备", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel bind(@RequestBody @ApiParam(value = "设备信息", required = true) DeviceAddModel deviceAddModel) {
        logger.info("设备配网信息为：{}", deviceAddModel);

        ResponseModel resp = new ResponseModel<>();
        try {
            //获取当前网关的设备ID
            String g_deviceId = (String) redisUtil.get(gatewayConfig.getGateway_device_id());

            if (g_deviceId == null || "".equals(g_deviceId)) {
                throw new DataCheckException(ErrorCodeEnum.GATEWAY_NOT_EXIST.getCode(), ErrorCodeEnum.GATEWAY_NOT_EXIST.getMessage());
            }
            boolean bindDevice = interiorDeviceService.bindDevice(deviceAddModel.getAccessToken(),
                    deviceAddModel.getUserId(),
                    deviceAddModel.getDeviceId());

            // 绑定成功后获取设备列表将设备映射到kilink平台 如果映射失败则取消当前已绑定的设备
            if(!bindDevice){
                throw new DataCheckException("绑定设备失败");
            }
            boolean addDevice = kilinkDeviceService.addDevice(g_deviceId, deviceAddModel);

            if(!addDevice){
                throw new DataCheckException("绑定设备失败");
            }

        } catch (DataCheckException e) {
            logger.error("绑定设备异常: {}", e.getMessage());
            resp = new ResponseModel(e.getCode(), false, e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("绑定设备异常: {}", e.getMessage());
            resp = new ResponseModel(ErrorCodeEnum.ERROR.getCode(), false, ErrorCodeEnum.ERROR.getMessage());
            e.printStackTrace();
        }
        logger.info("绑定设备结果：{}", resp);
        return resp;
    }

//    @PostMapping(value = "/add")
//    @ApiOperation(value = "添加设备到kilink平台", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseModel add(@RequestBody @ApiParam(value = "设备信息", required = true) DeviceAddModel deviceAddModel) {
//        logger.info("设备配网信息为：{}", deviceAddModel);
//        ResponseModel<Boolean> resp = new ResponseModel<>();
//        try {
//            interiorDeviceService.getDevices();
//            resp.setData(success);
//        } catch (DataCheckException e) {
//            logger.error("绑定设备异常: {}", e.getMessage());
//            resp = new ResponseModel(e.getCode(), false, e.getMessage(), false);
//            e.printStackTrace();
//        } catch (Exception e) {
//            logger.error("绑定设备异常: {}", e.getMessage());
//            resp = new ResponseModel(ErrorCodeEnum.ERROR.getCode(), false, ErrorCodeEnum.ERROR.getMessage(), false);
//            e.printStackTrace();
//        }
//        logger.info("绑定设备结果：{}", resp);
//        return resp;
//    }
}
