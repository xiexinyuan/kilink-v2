package com.konka.iot.tuya.core.service.user.impl;

import com.konka.iot.baseframe.common.core.service.BaseService;
import com.konka.iot.tuya.config.TuyaConfig;
import com.konka.iot.tuya.core.service.user.UserService;
import com.konka.iot.tuya.model.user.User;
import com.tuya.api.TuyaClient;
import com.tuya.api.model.domain.user.UserList;
import com.tuya.api.model.enums.UserTypeEnum;
import com.tuya.api.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-11 11:32
 * @Description TODO
 */
@Service
public class UserServiceImpl extends BaseService implements UserService {

    @Autowired
    private TuyaClient tuyaClient;
    @Autowired
    private TuyaConfig tuyaConfig;

    @Override
    public String registerUser(User user) throws Exception {

        UserTypeEnum userTypeEnum = null;
        switch (user.getUserNameType()) {
            case 1:
                userTypeEnum = UserTypeEnum.MOBLIE;
                break;
            case 2:
                userTypeEnum = UserTypeEnum.EMAIL;
                break;
            case 3:
                userTypeEnum = UserTypeEnum.USER_NAME;
                break;

            default:
                userTypeEnum = UserTypeEnum.USER_NAME;
                break;
        }
        return tuyaClient.registerUser(tuyaConfig.getSchema(), tuyaConfig.getCountryCode(), user.getUserName(), MD5Util.getMD5(user.getPassWord()), user.getNickName(), userTypeEnum);
    }

    @Override
    public UserList getUsers(int pageNo, int pageSize) throws Exception {
        return tuyaClient.getUsers(tuyaConfig.getSchema(), pageNo, pageSize);
    }
}
