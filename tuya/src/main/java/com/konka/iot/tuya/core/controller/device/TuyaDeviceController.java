package com.konka.iot.tuya.core.controller.device;

import com.konka.iot.baseframe.common.model.ResponseModel;
import com.konka.iot.tuya.core.service.device.TuyaDeviceService;
import com.konka.iot.tuya.model.device.DeviceTokenReqModel;
import com.tuya.api.model.Command;
import com.tuya.api.model.domain.device.*;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-11 10:30
 * @Description 设备对接
 */
@Api(tags = {"涂鸦设备对接操作接口"})
@RestController
@RequestMapping("/v1/device/tuya")
public class TuyaDeviceController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TuyaDeviceService tuyaDeviceService;

    @GetMapping(value = "/{userId}/list")
    @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    @ApiOperation(value = "获取用户下的设备列表", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<List<DeviceVo>> getUsers(@PathVariable("userId") String userId) {

        ResponseModel<List<DeviceVo>> resp = null;
        try {
            List<DeviceVo> result = tuyaDeviceService.getDevices(userId);
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取用户{}下的设备列表失败: {}", userId, e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }

    @PostMapping(value = "/token")
    @ApiImplicitParam(name = "deviceTokenReqModel", value = "配网请求实体", dataType = "deviceTokenReqModel", required = true)
    @ApiOperation(value = "生成配网令牌", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<DeviceToken> deviceToken(@RequestBody DeviceTokenReqModel deviceTokenReqModel) {

        ResponseModel<DeviceToken> resp = null;
        try {
            DeviceToken result = tuyaDeviceService.getDeviceToken(deviceTokenReqModel);
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("生成配网令牌失败: {}", e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }

    @GetMapping(value = "/network/list")
    @ApiImplicitParam(name = "token", value = "设备配网令牌", required = true)
    @ApiOperation(value = "获取设备配网列表", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<DeviceResultOfToken> getConfigNetworkDevices(@RequestParam("token") String token) {

        ResponseModel<DeviceResultOfToken> resp = null;
        try {
            DeviceResultOfToken result = tuyaDeviceService.getConfigNetworkDevices(token);
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取设备配网列表失败: {}", e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }


    @PutMapping(value = "/{deviceId}/enabled-sub/{duration}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "网关设备ID", required = true),
            @ApiImplicitParam(name = "duration", value = "网关发现时间 默认为100S 最大300S 0 为停止发现", dataType = "Integer")
    })
    @ApiOperation(value = "开放网关允许子设备入网", httpMethod = "PUT", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<Boolean> enabledSub(@PathVariable("deviceId") String deviceId,
                                             @PathVariable(value = "duration", required = false) Integer duration) {

        ResponseModel<Boolean> resp = null;
        try {
            Boolean result = tuyaDeviceService.enabledSub(deviceId, duration);
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("开放网关{}允许子设备入网失败: {}", deviceId, e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }

    @PutMapping(value = "/{deviceId}/list-sub/{discoveryTime}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "网关设备ID", required = true),
            @ApiImplicitParam(name = "discoveryTime", value = "网关发现子设备时间", dataType = "long", required = true)
    })
    @ApiOperation(value = "获取入网子设备列表", httpMethod = "PUT", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<List<DeviceVo>> listSub(@PathVariable("deviceId") String deviceId,
                                                 @PathVariable("discoveryTime") long discoveryTime) {

        ResponseModel<List<DeviceVo>> resp = null;
        try {
            List<DeviceVo> result = tuyaDeviceService.listSub(deviceId, discoveryTime);
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取网关{}下入网子设备列表失败: {}", deviceId, e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }


    @GetMapping(value = "/{deviceId}")
    @ApiImplicitParam(name = "deviceId", value = "设备ID", required = true)
    @ApiOperation(value = "获取设备详情", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<DeviceVo> getDeviceInfo(@PathVariable("deviceId") String deviceId) {

        ResponseModel<DeviceVo> resp = null;
        try {
            DeviceVo result = tuyaDeviceService.getDeviceInfo(deviceId);
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取设备{}详情失败: {}", deviceId, e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }

    @GetMapping(value = "/schema")
    @ApiOperation(value = "获取应用下设备列表", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<BatchDevices> getSchemaDevices( ) {

        ResponseModel<BatchDevices> resp = null;
        try {
            BatchDevices result = tuyaDeviceService.getSchemaDevices();
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取当前应用下设备列表失败: {}", e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }

    @GetMapping(value = "/{deviceId}/sub-devices")
    @ApiImplicitParam(name = "deviceId", value = "网关设备ID", required = true)
    @ApiOperation(value = "根据网关设备ID获取子设备列表", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<List<DeviceVo>> subDevices(@PathVariable("deviceId") String deviceId) {

        ResponseModel<List<DeviceVo>> resp = null;
        try {
            List<DeviceVo> result = tuyaDeviceService.subDevices(deviceId);
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取网关{}下的子设备列表失败: {}", deviceId, e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }

    @GetMapping(value = "/functions/{category}")
    @ApiImplicitParam(name = "category", value = "品类名称", required = true)
    @ApiOperation(value = "获取设备指令集(按品类)", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<CategoryFunctions> functionsByCategory(@PathVariable("category") String category) {

        ResponseModel<CategoryFunctions> resp = null;
        try {
            CategoryFunctions result = tuyaDeviceService.functionsByCategory(category);
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取设备品类{}指令集失败: {}", category, e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }

    @GetMapping(value = "/{deviceId}/functions/")
    @ApiImplicitParam(name = "deviceId", value = "设备ID", required = true)
    @ApiOperation(value = "获取设备指令集(按设备)", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<CategoryFunctions> functionsByDevice(@PathVariable("deviceId") String deviceId) {

        ResponseModel<CategoryFunctions> resp = null;
        try {
            CategoryFunctions result = tuyaDeviceService.functionsByDevice(deviceId);
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取设备{}指令集失败: {}", deviceId, e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }


    @PostMapping(value = "/{deviceId}/commands")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", required = true),
            @ApiImplicitParam(name = "commands", value = "控制指令集", dataTypeClass = List.class, allowMultiple = true, required = true)
    })
    @ApiOperation(value = "设备控制", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<Boolean> deviceCommand(@PathVariable("deviceId") String deviceId, @RequestBody List<Command> commands) {

        ResponseModel<Boolean> resp = null;
        try {
            Boolean result = tuyaDeviceService.deviceCommand(deviceId, commands);
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("设备{}控制失败: {}", deviceId, e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }


    @PutMapping(value = "/{deviceId}/edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", required = true),
            @ApiImplicitParam(name = "name", value = "设备名称", required = true, examples = @Example({
                    @ExampleProperty(value = "{'name':'light'}", mediaType = "application/json")}))
    })
    @ApiOperation(value = "修改设备名称", httpMethod = "PUT", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<Boolean> editName(@PathVariable("deviceId") String deviceId, @RequestBody Map<String, Object> name) {

        ResponseModel<Boolean> resp = null;
        try {
            Boolean result = tuyaDeviceService.editName(deviceId, name);
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("修改设备{}名称失败: {}", deviceId, e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }

    @PutMapping(value = "/{deviceId}/edit/sub")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", required = true),
            @ApiImplicitParam(name = "functionCode", value = "子设备code", required = true, example = "switch_1"),
            @ApiImplicitParam(name = "name", value = "设备名称", required = true, examples = @Example({@ExampleProperty(value = "{'name':'light'}", mediaType = "application/json")}))
    })
    @ApiOperation(value = "修改子设备名称", httpMethod = "PUT", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<Boolean> editSubName(@PathVariable("deviceId") String deviceId,
                                              @PathVariable("functionCode") String functionCode,
                                              @RequestBody Map<String, Object> name) {

        ResponseModel<Boolean> resp = null;
        try {
            Boolean result = tuyaDeviceService.editSubName(deviceId, functionCode, name);
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("修改子设备{}名称失败: {}", deviceId, e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }

    @PutMapping(value = "/{deviceId}/rest")
    @ApiImplicitParam(name = "deviceId", value = "设备ID", required = true)
    @ApiOperation(value = "恢复出厂设置", httpMethod = "PUT", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<Boolean> reset(@PathVariable("deviceId") String deviceId) {

        ResponseModel<Boolean> resp = null;
        try {
            Boolean result = tuyaDeviceService.reset(deviceId);
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("设备{}恢复出厂设置失败: {}", deviceId, e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }


    @DeleteMapping(value = "/{deviceId}/remove")
    @ApiImplicitParam(name = "deviceId", value = "设备ID", required = true)
    @ApiOperation(value = "移除设备", httpMethod = "DELETE", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<Boolean> remove(@PathVariable("deviceId") String deviceId) {

        ResponseModel<Boolean> resp = null;
        try {
            Boolean result = tuyaDeviceService.remove(deviceId);
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("移除设备{}失败: {}", deviceId, e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }

    @GetMapping(value = "/{deviceId}/status")
    @ApiImplicitParam(name = "deviceId", value = "设备ID", required = true)
    @ApiOperation(value = "获取设备当前状态", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<List<Status>> getStatus(@PathVariable("deviceId") String deviceId) {

        ResponseModel<List<Status>> resp = null;
        try {
            List<Status> result = tuyaDeviceService.getDeviceStatus(deviceId);
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取设备当前状态{}失败: {}", deviceId, e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }
}
