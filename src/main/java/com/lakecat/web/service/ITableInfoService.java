package com.lakecat.web.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lakecat.web.entity.*;
import com.lakecat.web.entity.table.TableOutputInfo;
import com.lakecat.web.entity.table.TablePrivilegeInfo;
import com.lakecat.web.entity.table.TableProfileInfo;
import com.lakecat.web.entity.table.TableStorageInfo;
import com.lakecat.web.entity.table.TableSummaryDescInfo;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.response.Response;
import com.lakecat.web.vo.blood.AddColumnVo;
import com.lakecat.web.vo.blood.AlterColumnVo;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.model.Database;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-13
 */
public interface ITableInfoService extends IService <TableInfo> {

    boolean alterTableBetweenByLakecat(Long id, String owner, String region,
                                       String targetTenantName,
                                       String originTenantName,
                                       String dbName,
                                       String tableName) throws BusinessException;

    void addCloumnLevel(AddColumnVo addColumnVo);

    boolean existTable( String region, String databaseName, String tableName);

    Response createTable(TableInfo entity);

    Response createDatabase(DatabaseInfo entity);

    Response createDatabaseByGateWay(DatabaseInfo entity);

    Response updateDatabaseByGateWay(DatabaseInfo entity);

    Response deleteDatabaseByGateWay(DatabaseInfo entity);

    void synchronization( String tenantName, String region, Boolean flag,boolean update);

    List <JSONObject> init();

    TableInfo getTableDetail(Long id);

    TableInfo getTableDetail(TableInfo tableInfo);

    TableInfo column(Long id);

    TableInfo column(TableInfo tableInfo);

    JSONObject search(TableInfo tableInfo) throws BusinessException;

    List <Object> getData(String table, String region, Integer size, String tenantName, String userName);

    @Transactional
    boolean changeTableDetail(TableInfo entity) throws BusinessException;


    List <String> getDBList(String PROJECT_ID, String region) throws BusinessException;

    List<Database> searchDbList(String PROJECT_ID, String region) throws BusinessException;

    void isRightSQL(String sql, String region, String tenantName, String userName) throws BusinessException;

    boolean alterColumns(TableInfo entity) throws BusinessException;

    boolean alterColumns(AlterColumnVo alterColumnVo) throws BusinessException;

    boolean addColumns(TableInfo entity) throws BusinessException;

    boolean alterOwnerByLakecat(TableInfo tableInfo, String owner, String region, String tenantName) throws BusinessException;

    boolean createTableByLakecat(Long id,String owner,  String region, String tenantName) throws BusinessException;

    JSONObject route(JSONObject args);

    @Transactional
    String preciseSync(String userId, String region, String databaseName, String tableName,String tenantName) throws BusinessException;

    List <TableUsageProfileGroupByUser> tableProfileInfo(TableInfoReq tableInfoReq, Integer recentlyDays);

    TableStorageInfo tableStorageInfo(TableInfoReq tableInfoReq, Long id) throws BusinessException;

    TableOutputInfo tableOutputInfo(TableInfoReq tableInfoReq) throws BusinessException;

    List <TablePrivilegeInfo> tablePrivilegeInfo(TableInfoReq tableInfoReq) throws BusinessException;

    PageInfo<TablePrivilegeInfo> tablePrivilegeInfoPages(TableInfoReq tableInfoReq) throws BusinessException;

    TableSummaryDescInfo tableSummaryDescInfo(TableInfoReq tableInfoReq, Long id) throws BusinessException;

    boolean setCollect(JSONObject entity) throws BusinessException;

    JSONObject searchOne(TableInfo tableInfo) throws BusinessException;

    TableInfo getTable(TableInfo tableInfo) throws BusinessException;

    TableInfo getTableOtherInfo(TableInfo tableInfo) throws BusinessException;

    TableInfo updateTable(TableInfo tableInfo) throws BusinessException;

    List <CollectInfo> collectList(TableInfo tableInfo) throws BusinessException;

    PageInfo<TableInfo> collectPages(CollectInfo collectInfo) ;

    TableInfo getTableInfoIfNotPresent(Long id, TableInfo tableInfo);

    void syncLast();


    Database getDBByName(String tenantName, String region, String dbName);
}
