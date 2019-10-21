package com.konka.iot.tuya;

import com.alibaba.dubbo.config.annotation.Reference;
import com.konka.iot.tuya.config.TuyaConfig;
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
}
