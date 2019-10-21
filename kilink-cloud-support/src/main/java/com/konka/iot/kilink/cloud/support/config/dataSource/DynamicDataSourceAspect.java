package com.konka.iot.kilink.cloud.support.config.dataSource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-19 20:20
 * @Description 配置数据源切面
 */

@Aspect
@Order(-1)
@Component
public class DynamicDataSourceAspect {

    private static final Logger logger = LoggerFactory.getLogger(DynamicDataSourceAspect.class);

    /**
     * 执行方法前更换数据源
     *
     * @param joinPoint        切点
     * @param targetDataSource 动态数据源
     */
    @Before("@annotation(targetDataSource)")
    public void doBefore(JoinPoint joinPoint, TargetDataSource targetDataSource) {
        DataSourceKey dataSourceKey = targetDataSource.name();
        if (dataSourceKey == DataSourceKey.DB_KILINK) {
            logger.info(String.format("设置数据源为  %s", DataSourceKey.DB_KILINK));
            DynamicDataSourceContextHolder.set(DataSourceKey.DB_KILINK);
        } else if(dataSourceKey == DataSourceKey.DB_KSOA_KILINK){
            logger.info(String.format("设置数据源为  %s", DataSourceKey.DB_KSOA_KILINK));
            DynamicDataSourceContextHolder.set(DataSourceKey.DB_KSOA_KILINK);
        }else {
            logger.info(String.format("使用默认数据源  %s", DataSourceKey.DB_KILINK));
            DynamicDataSourceContextHolder.set(DataSourceKey.DB_KILINK);
        }
    }

    /**
     * 执行方法后清除数据源设置
     *
     * @param joinPoint        切点
     * @param targetDataSource 动态数据源
     */
    @After("@annotation(targetDataSource)")
    public void doAfter(JoinPoint joinPoint, TargetDataSource targetDataSource) {
        logger.info(String.format("当前数据源  %s  执行清理方法", targetDataSource.name()));
        DynamicDataSourceContextHolder.clear();
    }
}
