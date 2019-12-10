package com.konka.iot.tuya.core.controller.message;

import com.konka.iot.baseframe.common.exception.DataCheckException;
import com.konka.iot.baseframe.common.model.ResponseModel;
import com.konka.iot.baseframe.common.utils.EncryptionUtil;
import com.konka.iot.tuya.config.TuyaConfig;
import com.konka.iot.tuya.core.service.message.MessageService;
import com.konka.iot.tuya.model.message.ReceiveDataModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-12 15:53
 * @Description TODO
 */

@Api(tags = {"涂鸦webhook回调处理类"})
@RestController
@RequestMapping("/v1/message/")
public class MessageController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TuyaConfig tuyaConfig;

    @Autowired
    private MessageService messageService;

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @PostMapping(value = "/report")
    @ApiOperation(value = "接受涂鸦回调的消息", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel<Boolean> report(HttpServletRequest request, @RequestBody ReceiveDataModel receiveDataModel) {

        logger.info("接收的数据为: {}", receiveDataModel);

        ResponseModel<Boolean> resp = new ResponseModel<>(true);
        try {
            String clientId = request.getHeader("client-id");
            String accessToken = request.getHeader("access-token");
            String signature = request.getHeader("signature");
            String t = request.getHeader("t");
            String data = clientId + accessToken + t;
            String sign = EncryptionUtil.HMACSHA256(data, tuyaConfig.getAccessKey());
            if (!signature.equals(sign)) {
                throw new DataCheckException("签名校验失败");
            }
            // 异步处理消息
            threadPoolTaskExecutor.execute(( ) -> messageService.dealMessage(receiveDataModel));

        } catch (DataCheckException e) {
            logger.error("数据处理异常：{}", e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage(), false);
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("数据处理异常：{}", e.getMessage());
            resp = new ResponseModel<>(500, false, e.getMessage(), false);
            e.printStackTrace();
        }
        return resp;
    }
}
