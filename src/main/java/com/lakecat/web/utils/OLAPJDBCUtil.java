package com.lakecat.web.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.lakecat.web.config.GlobalConfig;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

@Slf4j
@Component
public class OLAPJDBCUtil {


    @Autowired
    GlobalConfig globalConfig;

    public static String getUserConf(String tenantName, String userId) {
        if (userId == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("--conf bdp-query-user=%s\n", userId)).append(String.format("--conf bdp-query-tenancy=%s\n", tenantName));
        return sb.toString();
    }

    public JSONArray getSqlResult(String sql, String region, String tenantName, String userId) {
        sql = getUserConf(tenantName, userId) + sql;
        TimeZone.setDefault(TimeZone.getTimeZone("+08:00"));
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet;
        Properties properties = new Properties();
        JSONArray dataList = new JSONArray();
        String url = globalConfig.getOlapServiceUrl(region);
        properties.setProperty("user", globalConfig.getOlapServiceUser(region));
        if (url.contains("443")) {
            properties.setProperty("SSL", "true");
            properties.setProperty("password", globalConfig.getOlapServicePassword(region));
        }
        try {
            connection = DriverManager.getConnection(url, properties);
            statement = connection.createStatement();
            boolean execute = statement.execute(sql);
            if (execute) {
                resultSet = statement.getResultSet();
                ResultSetMetaData metaData = resultSet.getMetaData();
                while (resultSet.next()) {
                    JSONObject rowData = new JSONObject();
                    Object value;
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        if (metaData.getColumnTypeName(i).contains("array") || metaData.getColumnTypeName(i).contains("map")) {
                            value = JSON.toJSONString(resultSet.getObject(i), SerializerFeature.IgnoreErrorGetter);
                        } else {
                            value = resultSet.getObject(i);
                        }
                        rowData.put(metaData.getColumnName(i), value);
                    }
                    dataList.add(rowData);
                }
            }
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return dataList;
    }

    private static volatile Connection instance;

    // 对外提供静态方法获取该对象
    public Connection getInstance(String region) {
        // 第一次判断，如果instance不为null，不进入抢锁阶段，直接返回实例
        if (instance == null) {
            synchronized (Connection.class) {
                // 抢到锁之后再次判断是否为null
                if (instance == null) {
                    TimeZone.setDefault(TimeZone.getTimeZone("+08:00"));
                    Properties properties = new Properties();
                    String url = globalConfig.getOlapServiceUrl(region);
                    properties.setProperty("user", globalConfig.getOlapServiceUser(region));
                    if (url.contains("443")) {
                        properties.setProperty("SSL", "true");
                        properties.setProperty("password", globalConfig.getOlapServicePassword(region));
                    }
                    try {
                        instance = DriverManager.getConnection(url, properties);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        }
        return instance;
    }


    public Response createTable(String sql, String region, String tenantName, String userId) {
        sql = getUserConf(tenantName, userId) + sql;
        TimeZone.setDefault(TimeZone.getTimeZone("+08:00"));
        Statement statement = null;
        try {
            Connection connection = getInstance(region);
            statement = connection.createStatement();
            boolean execute = statement.execute(sql);
            if (execute) {
                return Response.success();
            } else {
                return Response.fail("建表失败");
            }
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }


    private String getReadableErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message != null && message.startsWith("Query failed (") && message.contains("ErrorDetail:Error running query:")) {
            String[] split = message.split(":");
            if (split.length >= 4) {
                return String.join(":", Arrays.asList(split).subList(4, split.length));
            }
        }
        return message;
    }

}
