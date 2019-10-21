package com.konka.iot.tuya.config.interceptor;

import com.konka.iot.baseframe.common.model.ResponseModel;
import com.konka.iot.baseframe.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-29 14:26
 * @Description 涂鸦网关拦截器
 */
@Component
public class TuyaInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TuyaInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        String token = request.getHeader("access-token");
//        if (token == null || "".equals(token)) {
//            PrintWriter writer = null;
//            response.setCharacterEncoding("UTF-8");
//            response.setContentType("text/html; charset=utf-8");
//            try {
//                ResponseModel<String> responseModel = new ResponseModel<>(500, false, "access-token is null");
//                writer = response.getWriter();
//                writer.print(JsonUtil.obj2String(responseModel));
//                log.error("access-token is null");
//                return false;
//            } catch (IOException e) {
//                log.error("response error", e);
//            } finally {
//                if (writer != null)
//                    writer.close();
//            }
//        } else {
//            // 校验token
//        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
