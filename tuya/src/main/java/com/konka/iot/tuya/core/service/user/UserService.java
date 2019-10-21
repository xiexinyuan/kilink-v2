package com.konka.iot.tuya.core.service.user;

import com.konka.iot.tuya.model.user.User;
import com.tuya.api.model.domain.user.UserList;

/**
 * @Author xiexinyuan
 * @Date 2019-09-10 17:03
 * @Description 用户对接
 **/
public interface UserService {

    /** 注册用户
     * @author xiexinyuan
     * @date 2019/9/11 10:32
     * @param user 用户实体
     * @return String
     * @throws Exception
    */
    String registerUser(User user) throws Exception;


    /** 获取用户列表
     * @author xiexinyuan
     * @date 2019/9/11 10:32
     * @param pageNo 当前页
     * @param pageSize 页大小
     * @return ResponseModel<List<User>>
     * @throws Exception
     */
    UserList getUsers(int pageNo, int pageSize) throws Exception;
}
