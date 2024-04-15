package com.lakecat.web.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.lakecat.web.entity.CurrentUser;
import com.lakecat.web.entity.InfTraceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

import static com.lakecat.web.common.CommonMethods.isAdmin;
@Slf4j
@Configuration
public class MybatisPlusConfig {

    public static final String[] IGNORE_DB_SHARD = {"user_group", "user_group_relation", "task",
            "actor", "access_group"};

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 动态表名插件
        DatacakeDynamicTableNameInterceptor dynamicTableNameInnerInterceptor = new DatacakeDynamicTableNameInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler(new DataCakeTableNameHandler() {
            String[] ignoreDbShard = IGNORE_DB_SHARD;
            @Override
            public String dynamicTableName(String database, String sql, String tableName) {
                List<String> list = Arrays.asList(ignoreDbShard);
                if (list.contains(tableName.trim()) ) {
                    return "ds_task."+tableName;
                }
                return "gov."+tableName;
//                CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
//                String tenantName="";
//                if (userInfo==null){
//                    tenantName="ninebot";
//                }else {
//                    tenantName = userInfo.getTenantName();
//                }
//                if (StringUtils.isEmpty(tenantName)) {
//                    return tableName;
//                }
//                if (isAdmin(tenantName)) {
//                    return tableName;
//                } else {
//                    if ("user_group".equals(tableName)||"user_group_relation".equals(tableName)){
//                        return  "ds_task_" + tenantName + "." + tableName;
//                    }
//                    return "gov_" + tenantName + "." + tableName;
//                }
            }
        });

        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
        return interceptor;
    }

}

