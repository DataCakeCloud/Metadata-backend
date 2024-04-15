package com.lakecat.web;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.ushareit.interceptor", "com.lakecat.web"}, exclude = {DruidDataSourceAutoConfigure.class})
@EnableCaching
@EnableAsync
@EnableScheduling
@MapperScan("com.lakecat.web.mapper")
@EnableTransactionManagement
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")//定时任务锁,默认时间30S
public class WebApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(WebApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
