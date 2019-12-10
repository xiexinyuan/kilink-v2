package com.konka.iot.kilink.cloud.open.core;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-11-29 18:18
 * @Description TODO
 */
@Api(tags = {"token相关"})
@RestController
@RequestMapping("/v1/token/")
public class TokenController {
    @GetMapping
    @ApiOperation(value = "获取access_token", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public String create(){
        return null;
    }

    @GetMapping("/refresh")
    @ApiOperation(value = "刷新access_token", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public String refresh(){
        return null;
    }
}
