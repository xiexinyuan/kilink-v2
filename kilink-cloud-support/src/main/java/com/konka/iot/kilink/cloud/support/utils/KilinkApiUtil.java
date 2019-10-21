package com.konka.iot.kilink.cloud.support.utils;

import com.konka.iot.baseframe.common.model.HttpResult;
import com.konka.iot.baseframe.common.utils.HttpUtil;
import com.konka.iot.baseframe.common.utils.JsonUtil;
import com.konka.iot.baseframe.common.utils.RedisUtil;
import com.konka.iot.kilink.cloud.support.config.KilinkConfig;
import com.konka.iot.kilink.cloud.support.enums.KilinkApiEnum;
import org.apache.http.HttpHeaders;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-20 17:44
 * @Description kilink请求工具类
 */
@Lazy
@Component
public class KilinkApiUtil {

    private static final Logger loger = LoggerFactory.getLogger(KilinkApiUtil.class);

    @Autowired
    private KilinkConfig kilinkConfig;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private HttpUtil httpUtil;

    /**
     * 获取开发者调用凭证
     * @return token
     */
    public String getAccessToken(){

        // 从redis中获取token
        String token = (String) redisUtil.get(kilinkConfig.getAdmin_accesstoken_rediskey());

        if(token == null){
            String url = kilinkConfig.getCloud_url() + KilinkApiEnum.AUTH_TOKEN.getUrl();
            Map<String, Object> reqMap = new HashMap<>(2);
            reqMap.put("id", kilinkConfig.getKilink_access_key_id());
            reqMap.put("secret", kilinkConfig.getKilink_access_key_secret());
            Map<String, Object> headerMap = new HashMap<>(1);
            headerMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            try {
                HttpResult result = httpUtil.doPost(url, reqMap, headerMap);
                if(result.getCode() == 200){
                    Map<String, String> respMap = JsonUtil.string2Obj(result.getBody(), new TypeReference<Map<String, String>>() {});
                    token = respMap.get("access_token");
                    // 更新到redis
                    redisUtil.set(kilinkConfig.getAdmin_accesstoken_rediskey(), token);
                }else {
                    loger.error("获取kilink调用token失败：{}", result.getBody());
                }
            } catch (Exception e) {
                e.printStackTrace();
                loger.error("获取kilink调用token失败：{}", e.getMessage());
            }
        }

        return token;
    }

    /**
     * 创建请求头
     * @return
     */
    public Map<String, Object> createHeaderMap(String ...token){
        String accessToken = getAccessToken();
        Map<String, Object> headerMap = new HashMap<>(2);
        headerMap.put("Access-Token", (token == null|| token.length == 0) ? accessToken : token[0]);
        headerMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return headerMap;
    }
}
