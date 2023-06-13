package com.message.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DruidConfiguration {
    @Bean
    public DataSource dataSource(DataSourceProperties properties){
        // 根据配置动态构建一个DataSource
        return properties.initializeDataSourceBuilder().build();
    }


}
