package com.konka.iot.tuya.core.controller.user;

import com.konka.iot.baseframe.common.model.ResponseModel;
import com.konka.iot.baseframe.common.utils.JsonUtil;
import com.konka.iot.tuya.core.service.user.UserService;
import com.konka.iot.tuya.model.user.User;
import com.tuya.api.model.domain.user.UserList;
import io.swagger.annotations.*;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Author xiexinyuan
 * @Date 2019-09-10 16:46
 * @Description 用户对接
 **/

@Api(tags = {"用户对接操作接口"})
@RestController
@RequestMapping("/v1/user")
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;


    @PostMapping(value = "/tuya/register")
    @ApiOperation(value = "同步涂鸦用户", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<Map<String, String>> registerUser(@RequestBody @ApiParam(value = "同步用户实体", required = true)User user) {

        ResponseModel<Map<String, String>> resp = null;
        try {
            Map<String, String> result = JsonUtil.string2Obj(userService.registerUser(user), new TypeReference<Map<String, String>>() {});
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("同步用户失败: {}", e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }

    @GetMapping(value = "/tuya/list/{page_no}/{page_size}")
    @ApiOperation(value = "获取用户列表", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<UserList> getUsers(@PathVariable("page_no") @ApiParam(value = "当前页", required = true)Integer pageNo, @PathVariable("page_size") @ApiParam(value = "页大小", required = true)Integer pageSize) {

        ResponseModel<UserList> resp = null;
        try {
            UserList result = userService.getUsers(pageNo, pageSize);
            resp = new ResponseModel<>(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("同步用户列表失败: {}", e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage());
        }
        return resp;
    }
}
