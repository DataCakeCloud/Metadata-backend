package com.lakecat.web.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.lakecat.web.entity.DatabaseInfo;
import com.lakecat.web.entity.InfTraceContextHolder;
import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.service.ITableInfoService;
import io.lakecat.catalog.common.model.Database;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hive.jdbc.HiveConnection;
import org.apache.hive.jdbc.HiveStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.MessageFormat;

@Slf4j
@Service
public class GatewayJDBCUtil {

    public static final String createDbSql = "CREATE DATABASE IF NOT EXISTS {0} ";

    public static final String updateDbSql = "alter database {0} set {1} ";

    public static final String deleteDbSql = " DROP DATABASE IF EXISTS {0} ";

    @Value("${gateway.url}")
    private String gatewayUrl;

    public boolean createDb(DatabaseInfo databaseInfo) throws BusinessException {
        String sql = MessageFormat.format(createDbSql, databaseInfo.getDatabaseName());
        if (StringUtils.isNoneBlank(databaseInfo.getLocationUri())) {
            sql = sql + " LOCATION '" + databaseInfo.getLocationUri() + "'";
        }
        return executeSqlDb(sql, "", InfTraceContextHolder.get().getTenantName(), InfTraceContextHolder.get().getUserName(), InfTraceContextHolder.get().getUuid(), InfTraceContextHolder.get().getCurrentGroup());
    }


    public boolean updateDb(DatabaseInfo databaseInfo, Database dbByName) throws BusinessException {

        if (StringUtils.isNoneBlank(databaseInfo.getLocationUri()) && !dbByName.getLocationUri().equals(databaseInfo.getLocationUri())) {
            String updateLocationUri = " LOCATION '" + databaseInfo.getLocationUri().trim() + "'";
            String sql = MessageFormat.format(updateDbSql, databaseInfo.getDatabaseName(), updateLocationUri);
            executeSqlDb(sql, "", InfTraceContextHolder.get().getTenantName(), InfTraceContextHolder.get().getUserName(), InfTraceContextHolder.get().getUuid(), InfTraceContextHolder.get().getCurrentGroup());
        }

        if ((StringUtils.isNoneBlank(databaseInfo.getDescription()) && StringUtils.isNoneBlank(dbByName.getDescription())
                && !dbByName.getDescription().equals(databaseInfo.getDescription())) ||
                (StringUtils.isNoneBlank(databaseInfo.getDescription()) && StringUtils.isEmpty(dbByName.getDescription()))) {
            String updateComment = " COMMENT '" + databaseInfo.getDescription() + "'";
            String sql = MessageFormat.format(updateDbSql, databaseInfo.getDatabaseName(), updateComment);
            executeSqlDb(sql, "", InfTraceContextHolder.get().getTenantName(), InfTraceContextHolder.get().getUserName(), InfTraceContextHolder.get().getUuid(), InfTraceContextHolder.get().getCurrentGroup());
        }

        return true;
    }

    public boolean deleteDb(DatabaseInfo databaseInfo) throws BusinessException {
        String sql = MessageFormat.format(deleteDbSql, databaseInfo.getDatabaseName());
        return executeSqlDb(sql, "", InfTraceContextHolder.get().getTenantName(), InfTraceContextHolder.get().getUserName(), InfTraceContextHolder.get().getUuid(), InfTraceContextHolder.get().getCurrentGroup());
    }


    public  boolean executeSql(String sql, String region, String tenantName, String userId ,String userGroup,String dbName) throws BusinessException {
        log.info("create table param is:userId,userGroup :"+userId+"," +userGroup );
        log.info("create table param is:getCurrentGroup :"+InfTraceContextHolder.get().getCurrentGroup());
        String url= MessageFormat.format(gatewayUrl,userId,userGroup,InfTraceContextHolder.get().getCurrentGroup(),tenantName);
        url =url.replace("default",dbName);
        log.info("sqlurl-->"+url);
        log.info("sqlurl-->"+sql);
        HiveConnection connection = null;
        HiveStatement statement = null;
        try {
            connection = (HiveConnection) DriverManager.getConnection(url);
            statement = (HiveStatement) connection.createStatement();
            boolean hasResult = statement.execute(sql);
            statement.close();
            connection.close();
            return hasResult;
        }catch (Exception e){
            log.error("",e);
            throw new BusinessException("创建库和表接口异常"+e.getMessage(),500);
        }finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public  boolean executeSqlDb(String sql, String region, String tenantName, String userId ,String userGroup,String userGroupName) throws BusinessException {
        String url= MessageFormat.format(gatewayUrl,userId,userGroup,userGroupName,tenantName);
        log.info("sqlurl-->"+url);
        log.info("sqlurl-->"+sql);
        HiveConnection connection = null;
        HiveStatement statement = null;
        try {
            connection = (HiveConnection) DriverManager.getConnection(url);
            statement = (HiveStatement) connection.createStatement();
            boolean hasResult = statement.execute(sql);
            statement.close();
            connection.close();
            return hasResult;
        }catch (Exception e){
            log.error("",e);
            throw new BusinessException("查询库和表接口异常"+e.getMessage(),500);
        }finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public JSONArray executeSqlHasResult(String sql, String region, String tenantName, String userId , String userGroup, String dbName){
        String url= MessageFormat.format(gatewayUrl,userId,userGroup,InfTraceContextHolder.get().getCurrentGroup(),tenantName);
        url =url.replace("default",dbName);
        log.info("sqlurl-->"+url);
        log.info("sqlurl-->"+sql);
        HiveConnection connection = null;
        HiveStatement statement = null;
        JSONArray dataList = new JSONArray();
        ResultSet resultSet;
        try {
            connection = (HiveConnection) DriverManager.getConnection(url);
            statement = (HiveStatement) connection.createStatement();
            boolean hasResult = statement.execute(sql);
            if (hasResult) {
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
        }catch (Exception e){
            log.error("",e);
        }finally {
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

    public static void main(String[] args) throws Exception{
        String url = "jdbc:hive2://gateway-test.ushareit.org:10009/tpcds_10g;auth=noSasl;user={0}?kyuubi.engine.type=JDBC;kyuubi.session.cluster.tags=name:aws,region:us-east-1;kyuubi.engine.jdbc.connection.user={1};kyuubi.engine.jdbc.connection.provider=HiveConnectionProvider";
        HiveConnection conn = (HiveConnection) DriverManager.getConnection(url);
        System.out.println("conn: " + conn);
        HiveStatement stmt = (HiveStatement) conn.createStatement();

        /*boolean hasResult = stmt.execute(sql);
        System.out.println("hasResult: " + hasResult);
*/

        String sqlcreatetable = "create table test_hzg(id INT)ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE";
        boolean hasResult = stmt.execute(sqlcreatetable);
        System.out.println("hasResult: " + hasResult);

        String sqlcreatedb = "CREATE DATABASE IF NOT EXISTS test_hzgdb COMMENT 'This is a sample database for testing purposes' LOCATION '/user/hive/databases/my_database'" ;
        hasResult = stmt.execute(sqlcreatedb);
        System.out.println("hasResult: " + hasResult);
        stmt.close();
        conn.close();
    }
}
