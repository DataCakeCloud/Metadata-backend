package com.lakecat.web.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class HIveGateWayConfiguration {

    @Value("${gateway.hive.url}")
    private String url;

    @Value("${gateway.hive.username}")
    private String username;

    @Value("${gateway.hive.passwd}")
    private String passwd;


    @Bean
    public HikariDataSource hikariDataSource(){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(passwd);
        config.setMaximumPoolSize(2); // 最大连接数
        config.setMinimumIdle(1); // 最小空闲连接数
        config.setIdleTimeout(40000); // 连接空闲超时时间（毫秒）
        HikariDataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }
}
