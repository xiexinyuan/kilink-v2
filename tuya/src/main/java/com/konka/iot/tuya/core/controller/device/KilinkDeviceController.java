package com.konka.iot.tuya.core.controller.device;

import com.alibaba.dubbo.config.annotation.Reference;
import com.konka.iot.baseframe.common.config.ErrorCodeEnum;
import com.konka.iot.baseframe.common.exception.DataCheckException;
import com.konka.iot.baseframe.common.model.ResponseModel;
import com.konka.iot.kilink.cloud.support.api.service.product.ProductService;
import com.konka.iot.tuya.core.service.device.KilinkDeviceService;
import com.konka.iot.tuya.model.device.DeviceActiveResultModel;
import com.konka.iot.tuya.model.device.DeviceAddModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

    @Reference
    private ProductService productService;

    @PostMapping(value = "/add")
    @ApiOperation(value = "添加虚拟设备到网关(单个)", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel add(@RequestBody @ApiParam(value = "配网设备信息", required = true) DeviceAddModel deviceAddModel) {
        logger.info("设备配网信息为：{}", deviceAddModel);
        ResponseModel resp = new ResponseModel<>();
        try {
            kilinkDeviceService.add(deviceAddModel);
            DeviceActiveResultModel resultModel = kilinkDeviceService.getResultModel();
            if (resultModel.getIsActive()) {
                resp = new ResponseModel(ErrorCodeEnum.SUCCESS.getCode(), true, "添加设备成功", resultModel);
            } else {
                resp = new ResponseModel(ErrorCodeEnum.ERROR.getCode(), false, "添加设备失败", resultModel);
            }
        } catch (DataCheckException e) {
            logger.error("添加虚拟设备到网关异常: {}", e.getMessage());
            resp = new ResponseModel(e.getCode(), false, e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("添加虚拟设备到网关异常: {}", e.getMessage());
            resp = new ResponseModel(ErrorCodeEnum.ERROR.getCode(), false, ErrorCodeEnum.ERROR.getMessage());
            e.printStackTrace();
        }finally {
            kilinkDeviceService.clearQueue();
        }
        logger.info("添加虚拟设备到网关结果：{}", resp);
        return resp;
    }

    @GetMapping(value = "/product/{p_id}/tuya")
    @ApiOperation(value = "获取产品对应的涂鸦产品ID", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel productFilter(@PathVariable("p_id") @ApiParam(value = "kilink产品ID", required = true) String p_id){
        ResponseModel<Map<String, Object>> resp = null;
        try {
            String result = productService.getProductMapping(p_id,null);
            Map<String, Object> params = new HashMap<>(1);
            params.put("t_productId", result);
            resp = new ResponseModel<>(params);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取产品{}对应的涂鸦产品ID异常: {}", p_id, e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }

}
