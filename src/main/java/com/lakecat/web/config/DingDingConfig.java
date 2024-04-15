package com.lakecat.web.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Created by slj on 2022/5/25.
 */
@Data
@Configuration
public class DingDingConfig {

    @Value("${dingDing.tokenUrl}")
    private String dingDingTokenUrl;
    @Value("${dingDing.url}")
    private String dingDingUrl;
    @Value("${dingDing.username}")
    private String username;
    @Value("${dingDing.password}")
    private String password;
    @Value("${dingDing.syskey}")
    private String syskey;
    @Value("${dingDing.sysSecret}")
    private String sysSecret;
    @Value("${dingDing.callback}")
    private String callback;
    @Value("${dingDing.employee}")
    private String employee;
    @Value("${dingDing.aflow}")
    private String aflow;

}
