package com.konka.iot.tuya.core.controller.cache;

import com.konka.iot.baseframe.common.model.ResponseModel;
import com.konka.iot.baseframe.common.utils.RedisUtil;
import com.konka.iot.tuya.config.GatewayConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-11 14:28
 * @Description redis缓存管理
 */
@RestController
@Api(tags = {"缓存操作接口"})
@RequestMapping("/tuya/cache/")
public class CacheController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private GatewayConfig gatewayConfig;

    @PostMapping(value = "/clear/gateway")
    @ApiOperation(value = "清除网关相关缓存", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<Boolean> gateway(){
        ResponseModel<Boolean> result = new ResponseModel<>();
        try {
            redisUtil.del(gatewayConfig.getGateway_device_id());
            result.setData(true);
        } catch (Exception e) {
            logger.error("缓存清理异常：{}", e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @PostMapping(value = "/clear/device")
    @ApiOperation(value = "清除设备相关缓存", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<Boolean> device(){
        ResponseModel<Boolean> result = new ResponseModel<>();
        try {
            Set<String> keys = redisUtil.keys(gatewayConfig.getDevice_mapping_prefix().concat("*"));
            for (String key: keys) {
                redisUtil.del(key);
            }
            result.setData(true);
        } catch (Exception e) {
            logger.error("缓存清理异常：{}", e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @PostMapping(value = "/clear/product")
    @ApiOperation(value = "清除产品相关缓存", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<Boolean> product(){
        ResponseModel<Boolean> result = new ResponseModel<>();
        try {
            Set<String> keys = redisUtil.keys(gatewayConfig.getProduct_mapping_prefix().concat("*"));
            for (String key: keys) {
                redisUtil.del(key);
            }
            result.setData(true);
        } catch (Exception e) {
            logger.error("缓存清理异常：{}", e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @PostMapping(value = "/clear/datapoint")
    @ApiOperation(value = "清除数据端点相关缓存", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<Boolean> dataPoint(){
        ResponseModel<Boolean> result = new ResponseModel<>();
        try {
            Set<String> keys = redisUtil.keys(gatewayConfig.getDatapoint_mapping_prefix().concat("*"));
            for (String key: keys) {
                redisUtil.del(key);
            }
            result.setData(true);
        } catch (Exception e) {
            logger.error("缓存清理异常：{}", e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}
