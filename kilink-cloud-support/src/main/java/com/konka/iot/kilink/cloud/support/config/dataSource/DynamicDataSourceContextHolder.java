package com.konka.iot.kilink.cloud.support.config.dataSource;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-19 20:11
 * @Description 解决多线程访问全局DataSourceKey变量的问题
 */
public class DynamicDataSourceContextHolder {

    private static final ThreadLocal<DataSourceKey> currentDatesource = new ThreadLocal<>();

    /**
     * 清除当前数据源
     */
    public static void clear( ) {
        currentDatesource.remove();
    }

    /**
     * 获取当前使用的数据源
     *
     * @return 当前使用数据源的ID
     */
    public static DataSourceKey get( ) {
        return currentDatesource.get();
    }

    /**
     * 设置当前使用的数据源
     *
     * @param value 需要设置的数据源ID
     */
    public static void set(DataSourceKey value) {
        currentDatesource.set(value);
    }
}

