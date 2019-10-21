package com.konka.iot.baseframe.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-26 12:18
 * @Description spring上下文工具类
 */
public class SpringContextUtil{
    private static Logger log = LoggerFactory.getLogger(SpringContextUtil.class);

    // 上下文对象
    private static ApplicationContext applicationContext;

    /**
     * 实现ApplicationContextAware接口的回调方法，注入上下文对象
     *
     * @param applicationContext
     * @throws BeansException
     */
    public static void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }
    /**
     * 获取上下文对象
     *
     * @return applicationContext
     */
    public static ApplicationContext getApplicationContext( ) {
        return applicationContext;
    }

    /**
     * 判断上下文对象是否为空
     *
     * @return
     */
    public static boolean checkapplicationContext( ) {
        boolean flag = getApplicationContext() != null;
        if (!flag) {
            log.error("applicaitonContext未注入,实现ApplicationContextAware的类必须被spring管理");
        }
        return flag;
    }

    /**
     * 根据name获取bean
     *
     * @param name
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name) {
        if (checkapplicationContext()) {
            return (T) getApplicationContext().getBean(name);
        } else {
            return null;
        }
    }

    /**
     * 根据class 获取bean
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        if (checkapplicationContext()) {
            return getApplicationContext().getBean(clazz);
        } else {
            return null;
        }
    }

    /**
     * 根据name,以及Clazz返回指定的Bean
     *
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        if (checkapplicationContext()) {
            return getApplicationContext().getBean(name, clazz);
        } else {
            return null;
        }
    }
}
