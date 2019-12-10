package com.konka.iot.tuya.core.controller.test;

import com.konka.iot.baseframe.common.model.ResponseModel;
import com.konka.iot.tuya.core.service.gateway.GatewayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

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

    @GetMapping(value = "/download/apk")
    @ApiOperation(value = "下载kilink安卓安装包", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public void download(HttpServletResponse response) {
        try {
            String path = System.getProperty("user.dir") + File.separator + File.separator + "KiLink.apk";
            // path是指欲下载的文件的路径。
            File file = new File(path);
            // 取得文件名。
            String filename = file.getName();
            // 取得文件的后缀名。
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();

            // 以流的形式下载文件。
            InputStream fis = new BufferedInputStream(new FileInputStream(path));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
