package com.konka.iot.kilink.cloud.support.config.dataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Map;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-19 20:14
 * @Description 设置数据源
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    protected Object determineCurrentLookupKey( ) {
        logger.info("当前数据源：{}", DynamicDataSourceContextHolder.get());
        return DynamicDataSourceContextHolder.get();
    }
    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);
        // 必须添加该句，让方法根据重新赋值的targetDataSource依次根据key关键字
        // 查找数据源,返回DataSource,否则新添加数据源无法识别到
        super.afterPropertiesSet();
    }
}
