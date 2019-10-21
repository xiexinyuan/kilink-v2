package com.konka.iot.tuya.core.service.message;

import com.konka.iot.tuya.model.message.ReceiveDataModel;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-10-12 17:08
 * @Description 消息处理服务
 */
public interface MessageService {

    void dealMessage(ReceiveDataModel receiveDataModel);
}
