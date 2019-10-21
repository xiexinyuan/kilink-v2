package com.konka.iot.tuya.core.controller.test;

import com.konka.iot.baseframe.common.model.ResponseModel;
import com.konka.iot.tuya.core.service.gateway.GatewayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-17 09:14
 * @Description TODO
 */

@Api(tags = {"测试接口"})
@RestController
@RequestMapping("/v1/device/test")
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private GatewayService gatewayService;

    @PostMapping(value = "/rest/online")
    @ApiOperation(value = "设备重新上线", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel online() {
        ResponseModel resp = new ResponseModel<>();
        try {
            gatewayService.init();
        } catch (Exception e) {
            logger.error("设备重新上线异常：{}", e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
            e.printStackTrace();
        }
        return resp;
    }
}
