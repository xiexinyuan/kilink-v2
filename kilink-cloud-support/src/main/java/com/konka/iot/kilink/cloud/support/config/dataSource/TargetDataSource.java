package com.konka.iot.kilink.cloud.support.config.dataSource;

import java.lang.annotation.*;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-19 19:57
 * @Description 多数据源注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TargetDataSource {
    DataSourceKey  name() default DataSourceKey.DB_KILINK;
}
