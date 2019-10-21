package com.konka.iot.kilink.cloud.support.config.dataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-19 19:06
 * @Description 数据源配置
 */

@Configuration
@MapperScan(basePackages = "com.konka.iot.kilink.cloud.support.core.dao")
public class DataSourceConfig {


    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.kilink")
    public DataSourceProperties kilinkProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.ksoa-kilink")
    public DataSourceProperties ksoaProperties() {//这是是用hikariCP的时候用的
        return new DataSourceProperties();
    }


    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.kilink")
    public HikariDataSource kilinkDataSource() {
        return kilinkProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();

    }


    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.ksoa-kilink")
    public HikariDataSource ksoaDataSource() {

        return ksoaProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }


    /**
     * 动态数据源
     * @return 数据源实例
     */
    @Bean
    public DataSource dynamicDataSource() {
        DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource();
        // 设置默认的数据源
        dataSource.setDefaultTargetDataSource(ksoaDataSource());
        Map<Object, Object> dataSourceMap = new HashMap<>(2);
        dataSourceMap.put(DataSourceKey.DB_KILINK, kilinkDataSource());
        dataSourceMap.put(DataSourceKey.DB_KSOA_KILINK, ksoaDataSource());
        dataSource.setTargetDataSources(dataSourceMap);
        return dataSource;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dynamicDataSource());
        //此处设置为了解决找不到mapper文件的问题
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*/*.xml"));
        //开启自动驼峰命名转换 将数据库下划线转换为java驼峰命名 eg: user_name -> userName
        sqlSessionFactoryBean.getObject().getConfiguration().setMapUnderscoreToCamelCase(true);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate() throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory());
    }

    /**
     * 事务管理
     * @return 事务管理实例
     */
    @Bean
    public PlatformTransactionManager platformTransactionManager() {
        return new DataSourceTransactionManager(dynamicDataSource());
    }

}
