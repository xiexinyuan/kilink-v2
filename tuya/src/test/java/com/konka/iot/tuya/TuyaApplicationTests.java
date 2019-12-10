package com.konka.iot.tuya;

import com.alibaba.dubbo.config.annotation.Reference;
import com.konka.iot.tuya.config.TuyaConfig;
import com.konka.iot.tuya.core.service.message.MessageService;
import com.konka.iot.tuya.model.message.ReceiveDataModel;
import com.tuya.api.TuyaClient;
import com.tuya.api.model.enums.UserTypeEnum;
import com.tuya.api.util.MD5Util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.konka.iot.kilink.cloud.support.api.service.product.ProductService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TuyaApplicationTests {

    @Autowired
    private TuyaClient tuyaClient;
    @Autowired
    private TuyaConfig tuyaConfig;
    @Autowired
    private MessageService messageService;

    @Test
    public void contextLoads() {
        //注册用户
        String uid = tuyaClient.registerUser(tuyaConfig.getSchema(), tuyaConfig.getCountryCode(), "15083560287", MD5Util.getMD5("123456"), "testRegister", UserTypeEnum.MOBLIE);
        System.out.println("成功同步用户：" + uid);
        //获取用户列表
        tuyaClient.getUsers(tuyaConfig.getSchema(), 1, 100);
        //生成配网令牌
        // tuyaClient.getDeviceToken();
    }

    @Reference
    private ProductService productService;

    @Test
    public void testProductMapping() {

        try {
            String pid = productService.getProductMapping(null, "第三方产品ID");
            System.out.println(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDelMessage(){
        ReceiveDataModel model = new ReceiveDataModel();
        model.setProtocol(4);
        String data = "NdCFxg8nj/1LIUv350ELTmA5PInnVyecK1r7gBWDAU1qt41DZDJTMWu/019UNxYx6z9hSROZiuMpCRwWuRYlwbHqUk3S6YhHSAFNhAt+gVCYMrWfxc0SJAAOuwD9lWb4m7DNleO0ASsDkudZAnXPRf5LzwlhKC2njoKCHjNxwbI2EjpfGjRoeZri4tYc+PLYo9BPQ7Iy4J6IXbeZSrxg3HW++H5vxzQgzdL+0hwzjpJyftEOgGC84lgF6JiGik73";
        model.setData(data);
        messageService.dealMessage(model);
    }
}
