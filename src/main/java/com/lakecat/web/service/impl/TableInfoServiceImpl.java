package com.lakecat.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.lakecat.web.common.CommonParameters;
import com.lakecat.web.constant.*;
import com.lakecat.web.entity.*;
import com.lakecat.web.entity.table.TableOutputInfo;
import com.lakecat.web.entity.table.TablePrivilegeInfo;
import com.lakecat.web.entity.table.TableStorageInfo;
import com.lakecat.web.entity.table.TableSummaryDescInfo;
import com.lakecat.web.excel.PubMethod;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.mapper.*;

import com.lakecat.web.response.Response;
import com.lakecat.web.service.*;
import com.lakecat.web.thread.AlertSyncThread;
import com.lakecat.web.thread.SyncThread;
import com.lakecat.web.utils.*;
import com.lakecat.web.vo.blood.AddColumnVo;
import com.lakecat.web.vo.blood.AlterColumnVo;
import com.lakecat.web.vo.blood.ColumnVo;
import com.lakecat.web.vo.blood.UserGroupVo;
import io.lakecat.catalog.client.CatalogUserInformation;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.LakeCatConf;
import io.lakecat.catalog.common.Operation;
import io.lakecat.catalog.common.model.*;
import io.lakecat.catalog.common.model.discovery.TableCategories;
import io.lakecat.catalog.common.model.glossary.Category;
import io.lakecat.catalog.common.model.glossary.Glossary;
import io.lakecat.catalog.common.plugin.request.*;
import io.lakecat.catalog.common.plugin.request.input.*;
import io.lakecat.catalog.common.plugin.request.CreateDatabaseRequest;
import io.lakecat.catalog.common.plugin.request.GetTableRequest;
import io.lakecat.catalog.common.plugin.request.ListDatabasesRequest;
import io.lakecat.catalog.common.plugin.request.input.DatabaseInput;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.lakecat.web.service.impl.LakeCatClientServiceImpl.MODEL;
import static com.lakecat.web.service.impl.LakeCatClientServiceImpl.TAG;
import static com.lakecat.web.service.impl.TableInfoByHmsServiceImpl.getCreateTableSql;
import static java.util.stream.Collectors.toList;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-13
 */
@Slf4j
@Service
@Component
public class TableInfoServiceImpl extends ServiceImpl<TableInfoMapper, TableInfo> implements ITableInfoService {

    @Resource
    UserHistoryServiceImpl userHistoryService;
    ;

    @Autowired
    ITableInfoService iTableInfoService;

    @Autowired
    DataGradeServiceImpl dataGradeService;

    @Autowired
    OLAPJDBCUtil olapJdbcUtil;


    @Autowired
    TableInfoMapper tableInfoMapper;

    @Autowired
    PermissionRecordMapper permissionRecordMapper;

    @Autowired
    IBillService ibillService;

    @Autowired
    public DictMapper dictMapper;

    @Autowired
    TableInfoByHmsServiceImpl tableInfoByHmsServiceImpl;

    @Autowired
    ITableProfileService tableProfileService;

    @Autowired
    ILakeCatClientService iLakeCatClientService;

    @Autowired
    TableStorageInfoMapper tableStorageInfoMapper;

    @Autowired
    SyncModelService syncModelService;


    @Autowired
    SyncThread alertSendThread;


    @Autowired
    AlertSyncThread thread;


    @Autowired
    CollectMapper collectMapper;

    @Autowired
    LastActivityMapper lastActivityMapper;

    @Autowired
    IGovTagService govTagService;

    @Autowired
    IGovService igovService;

    @Autowired
    DSUtilForLakecat dsUtilForLakecat;

    @Autowired
    ITableInfoService tableInfoService;

    @Autowired
    CatalogNameEnum CatalogNameEnum;

    @Autowired
    TableTagInfoService tableTagInfoService;

    @Autowired
    private GatewayJDBCUtil gatewayJDBCUtil;

    @Autowired
    private IAdminRoleService adminRoleService;

    @Autowired
    TableTagInfoMapper tableTagInfoMapper;

    @Value("${default.tenantName}")
    private String defaultTenantName;


    private static ExpiringMap<String, TableInfo> tableInfoCacheMap = CacheUtils.getCacheMap(2000, 30, String.class, TableInfo.class);
    private static boolean openCache = false;

    @Override
    public Response createTable(TableInfo entity) {
        try {
            String region = entity.getRegion();
            String tenantName = entity.getTenantName();
            String owner = entity.getOwner();
            String dbName = entity.getDbName();
            String name = entity.getName();
            String catalogName = CatalogNameEnum.getCatalogNameByRegion(entity.getRegion());
            entity.setCatalogName(catalogName);
            io.lakecat.catalog.common.model.Database dbByName = iTableInfoService.getDBByName(tenantName, region, dbName);
            String locationUri = dbByName.getLocationUri();
            if (locationUri == null || !locationUri.startsWith("s3://")) {
                //通过region 获取云资源url
                String storage = CatalogNameEnum.getStorageByRegion(entity.getRegion());
                if (storage == null || storage.equals("0")) {
                    return Response.fail("挂载云资源异常");
                }
                entity.setLocation(storage + "/" + dbName + "/" + name);

            } else {
                String location = dbByName.getLocationUri() + "/" + name;
                entity.setLocation(location);
            }
//            olapJdbcUtil.getSqlResult(getCreateTableSql(entity), region, tenantName, owner);

            String uuid = InfTraceContextHolder.get().getUuid();
            if (StringUtils.isNotEmpty(entity.getUserGroupUUid())) {
                uuid = entity.getUserGroupUUid();
            }
            String sql = getCreateTableSql(entity);
            log.info("start create table ");
            gatewayJDBCUtil.executeSql(sql, region, tenantName,
                    InfTraceContextHolder.get().getUserName(), uuid, entity.getDbName());
            RoleInputs roleInputs = new RoleInputs();
            roleInputs.setRoleName(uuid);
            roleInputs.setObjectNames(new String[]{entity.getRegion() + "." + entity.getDbName() + "." + entity.getName()});
            roleInputs.setOperation(new String[]{"修改表", "删除表", "描述表", "查询数据", "插入数据"});
            adminRoleService.grantPrivilegeToRole(roleInputs, InfTraceContextHolder.get().getTenantName());

            //额外信息更新到表上
            String securityLevel = entity.getSecurityLevel();
            List<Category> listTag = entity.getListTag();
            String createBy = InfTraceContextHolder.get().getUserName();

            log.info("start update table ");
            TableInfo tableInfo = new TableInfo();
            tableInfo.setSecurityLevel(securityLevel).setCreateBy(createBy).setListTag(listTag)
                    .setRegion(entity.getRegion()).setDbName(dbName).setTableName(name)
                    .setDescription(entity.getDescription());
            iLakeCatClientService.updateTable(tableInfo);

            log.info("start Sync table to mysql ");

            //同步到mysql
            tableInfoService.preciseSync(owner, region, dbName, name, tenantName);
            List<TableInfo> tableIdByNames = tableInfoMapper.getTableIdByName(entity);
            //查询数据
            TableInfo tableIdByName = null;
            if (CollectionUtils.isNotEmpty(tableIdByNames)) {
//                if (StringUtils.isNoneBlank(entity.getSubject())){
//                    tableInfoMapper.updateSubjectById(entity.getSubject(),tableIdByNames.get(0).getId());
//                }
                tableIdByName = tableIdByNames.get(0);
                Long id = tableIdByName.getId();
                //alterOwnerByLakecat(id,owner,region,tenantName);
                List<TableTagInfo> tags = entity.getTagList();
                if (tags != null) {
                    for (TableTagInfo tag : tags) {
                        tag.setTableId(id);
                    }
                    tableTagInfoService.addTableTag(tags);
                }
            }
            return Response.success(tableIdByName);
/*            Response table = olapJdbcUtil.createTable(getCreateTableSql(entity), region, tenantName, owner);
            if (table.getCode() != 0) {
                return table;
            }
            tableInfoService.preciseSync(owner, region, dbName, name, tenantName);
            TableInfo tableIdByName = tableInfoMapper.getTableIdByName(entity);
            //查询数据
            if (tableIdByName != null) {
                Long id = tableIdByName.getId();
                List <TableTagInfo> tags = entity.getTagList();
                if (tags != null) {
                    for (TableTagInfo tag : tags) {
                        tag.setTableId(id);
                    }
                    tableTagInfoService.addTableTag(tags);
                }
            }*/
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("Create table warn: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Response createDatabase(DatabaseInfo entity) {
        try {
            DatabaseInput databaseInput = new DatabaseInput();
            Map<String, String> parameters = new HashMap<>();
            parameters.put("lms_name", CatalogNameEnum.getCatalogNameByRegion(entity.getRegion()));
            databaseInput.setCatalogName(CatalogNameEnum.getCatalogNameByRegion(entity.getRegion()));
            databaseInput.setDatabaseName(entity.getDatabaseName());
            //设置默认规则
            String storageByRegion = CatalogNameEnum.getStorageByRegion(entity.getRegion());
            if (storageByRegion == null || storageByRegion.equals("0")) {
                return Response.fail("挂载云资源异常");
            }
            databaseInput.setLocationUri(storageByRegion + "/" + entity.getDatabaseName());
            databaseInput.setDescription(entity.getDescription());
            databaseInput.setOwner(entity.getUserId());
            databaseInput.setParameters(parameters);
            CreateDatabaseRequest createDatabaseRequest = new CreateDatabaseRequest();
            createDatabaseRequest.setInput(databaseInput);
            createDatabaseRequest.setProjectId(entity.getProjectId());
            createDatabaseRequest.setCatalogName(CatalogNameEnum.getCatalogNameByRegion(entity.getRegion()));
            Database database = iLakeCatClientService.get().createDatabase(createDatabaseRequest);
            assertNotNull(database.getDatabaseId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.success("创建成功");
    }


    @Override
    public Response createDatabaseByGateWay(DatabaseInfo entity) {
        try {
            RoleInputs roleInputsDb = new RoleInputs();
            roleInputsDb.setObjectNames(new String[]{entity.getRegion()});
            roleInputsDb.setOperation(new String[]{"创建库"});
            roleInputsDb.setRoleName(InfTraceContextHolder.get().getUuid());
            adminRoleService.grantPrivilegeToRole(roleInputsDb, InfTraceContextHolder.get().getTenantName());
            gatewayJDBCUtil.createDb(entity);
            log.info("createdb success");
            RoleInputs roleInputs = new RoleInputs();
            roleInputs.setRoleName(entity.getUuid());
            roleInputs.setObjectNames(new String[]{entity.getRegion() + "." + entity.getDatabaseName()});
            roleInputs.setOperation(new String[]{"创建表", "删除库", "描述库", "修改库"});
            try {
                adminRoleService.grantPrivilegeToRole(roleInputs, InfTraceContextHolder.get().getTenantName());
                log.info("grantPrivilegeToRole success");
            } catch (Exception e) {
                log.info("grantPrivilegeToRolefail", e);
            }
            try {
                iLakeCatClientService.alterDatabase(entity.getRegion(), entity.getDatabaseName(), entity.getUserGroupName());
                log.info("alterDatabase success");
            } catch (Exception e) {
                log.info("alterDatabase fail", e);
            }
            try {
                iLakeCatClientService.toTablePrivilegeForRole(entity.getUuid(), entity.getRegion() + "." + entity.getDatabaseName() + ".*");
                log.info("toTablePrivilegeForRole success");
            } catch (Exception e) {
                log.info("toTablePrivilegeForRolefail", e);
            }

        } catch (BusinessException e) {
            return Response.fail(500, "创建库和表接口异常" + e.getMessage());
        }
        return Response.success();
    }

    @Override
    public Response updateDatabaseByGateWay(DatabaseInfo entity) {
        try {
            String tenantName = InfTraceContextHolder.get().getTenantName();
            Database dbByName = getDBByName(tenantName, entity.getRegion(), entity.getDatabaseName());
            iLakeCatClientService.alterDatabase(entity);

            //如果变的是用户组，就改下面的
            RoleInputs roleInputs = new RoleInputs();
            roleInputs.setRoleName(entity.getUuid());
            roleInputs.setObjectNames(new String[]{entity.getRegion() + "." + entity.getDatabaseName()});
            roleInputs.setOperation(new String[]{"创建表", "删除库", "描述库", "修改库"});
            //如果组变化了 就改一遍，没变化 就不调下面接口。
            try {
                adminRoleService.grantPrivilegeToRole(roleInputs, InfTraceContextHolder.get().getTenantName());
                log.info("grantPrivilegeToRole success");
            } catch (Exception e) {
                log.info("grantPrivilegeToRolefail", e);
            }
            try {
                iLakeCatClientService.toTablePrivilegeForRole(entity.getUuid(), entity.getRegion() + "." + entity.getDatabaseName() + ".*");
                log.info("toTablePrivilegeForRole success");
            } catch (Exception e) {
                log.info("toTablePrivilegeForRolefail", e);
            }

        } catch (Exception e) {
            return Response.fail(500, "更新库和表接口异常" + e.getMessage());
        }
        return Response.success();
    }

    @Override
    public Response deleteDatabaseByGateWay(DatabaseInfo entity) {
        try {
            //先更新数据库
            gatewayJDBCUtil.deleteDb(entity);
            log.info("deleteDb success");
        } catch (BusinessException e) {
            return Response.fail(500, "删除数据库接口异常" + e.getMessage());
        }
        return Response.success();
    }


    @Override
    public TableInfo getTableDetail(Long id) {
        TableInfo tableDetail = tableInfoMapper.getTableDetail(id);
        tableDetail.setTitle(tableDetail.title());
        setDataGrade(tableDetail);
        cacheTableInfo(id, tableDetail);
        return tableDetail;
    }

    public TableInfo getTableDetail(TableInfo tableInfo) {
        LakeCatParam lakeCatParam = LakeCatParam.builder().region(tableInfo.getRegion())
                .dbName(tableInfo.getDbName()).tableName(tableInfo.getTableName()).build();

        TableInfo tableDetail = iLakeCatClientService.getTable(lakeCatParam);
        tableDetail.setTitle(tableDetail.title());
        setDataGrade(tableDetail);
        cacheTableInfo(tableInfo.getKey(), tableDetail);
        return tableDetail;
    }

    @Override
    public TableInfo column(Long id) {
        TableInfo tableDetail = tableInfoMapper.column(id);
        setDataGrade(tableDetail);
        return tableDetail;
    }


    @Override
    public TableInfo column(TableInfo tableInfo) {
        LakeCatParam lakeCatParam = LakeCatParam.builder().region(tableInfo.getRegion())
                .dbName(tableInfo.getDbName()).tableName(tableInfo.getTableName()).build();
        TableInfo tableDetail = iLakeCatClientService.getTable(lakeCatParam);
        setDataGrade(tableDetail);
        return tableDetail;
    }

    /**
     * 设置数据等级
     *
     * @param tableDetail
     */
    private void setDataGrade(TableInfo tableDetail) {
        if (tableDetail != null) {
            List<DataGrade> colsGrade = dataGradeService.getByTableSole(tableDetail.getSole());
            HashMap<String, DataGrade> gradeHashMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(colsGrade)) {
                for (DataGrade dataGrade : colsGrade) {
                    gradeHashMap.put(dataGrade.getName(), dataGrade);
                }
            }
            JSONArray colsJson = JSON.parseArray(tableDetail.getColumns());
            String name;
            for (int i = 0; i < colsJson.size(); i++) {
                JSONObject jsonObject = colsJson.getJSONObject(i);
                name = jsonObject.getString("name");
                if (gradeHashMap.containsKey(name)) {
                    jsonObject.put(DataGradeServiceImpl.DATA_GRADE_KEY, gradeHashMap.get(name).getGrade());
                } else {
                    jsonObject.put(DataGradeServiceImpl.DATA_GRADE_KEY, "");
                }
            }
            tableDetail.setColumnsList(colsJson);
            tableDetail.setColumns(JSON.toJSONString(colsJson));
        }
    }


    /**
     * 查询 功能
     */
    @Override
    public JSONObject searchOne(TableInfo tableInfo) {

        JSONObject object = new JSONObject();
        LakeCatParam lakeCatParam = LakeCatParam.builder().region(tableInfo.getRegion())
                .dbName(tableInfo.getDbName()).tableName(tableInfo.getTableName()).build();

        TableInfo table = iLakeCatClientService.getTable(lakeCatParam);
        if (table == null) {
            return object;
        }
        String key = table.getKey();
        List<String> keys = new ArrayList<>();
        keys.add(key);
        //collect
        Map<String, List<CollectInfo>> collectMap = collectMapper.searchBykeys(keys).stream().collect(Collectors.groupingBy(CollectInfo::getSole));

        //userList count
        Map<String, List<LastActivityInfo>> lastMap = lastActivityMapper.searchBykeys(keys).stream().collect(Collectors.groupingBy(LastActivityInfo::getSole));

        //tags
        Map<String, List<TableTagInfo>> collect = tableTagInfoMapper.searchBykeys(keys).stream().collect(Collectors.groupingBy(TableTagInfo::getSole));

        List<CollectInfo> collectInfos = collectMap.get(key);
        List<LastActivityInfo> lastActivityInfos = lastMap.get(key);
        List<TableTagInfo> tableTagInfos = collect.get(key);
        if (collectInfos != null) {
            table.setCollect(collectInfos.stream().findFirst().get().getCollect());
            String userList = collectInfos.stream().findFirst().get().getUserList();
            String userName = tableInfo.getUserName();
            if (StringUtils.isEmpty(userName)) {
                userName = InfTraceContextHolder.get().getUserName();
            }
            if (StringUtils.isNotEmpty(userList) && userList.contains(userName)) {
                table.setFlag(1);
            }
        }
        if (lastActivityInfos != null) {
            String userList = lastActivityInfos.stream().findFirst().get().getUserList();
            table.setUserList(userList);
            table.setCount(lastActivityInfos.stream().findFirst().get().getCount());
        }
        if (tableTagInfos != null) {
            table.setTags(tableTagInfos.stream().findFirst().get().getTags());
        }
        return getSearchElement(table);
    }


    @Override
    public TableInfo getTable(TableInfo tableInfo) {
        LakeCatParam lakeCatParam = LakeCatParam.builder().region(tableInfo.getRegion())
                .dbName(tableInfo.getDbName()).tableName(tableInfo.getTableName()).build();

        TableInfo table = iLakeCatClientService.getTable(lakeCatParam);

        //collect
        String key = table.getSole();
        Map<String, List<CollectInfo>> collectMap = getCacheCollectBySole(key);

        List<CollectInfo> collectInfos = collectMap.get(key);
        if (collectInfos != null) {
            table.setCollect(collectInfos.stream().findFirst().get().getCollect());
            String userList = collectInfos.stream().findFirst().get().getUserList();
            String userName = tableInfo.getUserName();
            if (StringUtils.isEmpty(userName)) {
                userName = InfTraceContextHolder.get().getUserName();
            }
            if (StringUtils.isNotEmpty(userList) && userList.contains(userName)) {
                table.setFlag(1);
            }
        }
        //最晚分区值
//        String latestPartitionName = iLakeCatClientService.getLatestPartitionName(tableInfo, tableInfo.getSole());
//        table.setLatestPartitionName(latestPartitionName);
//
//        //分区数
//        Integer partitionCount = iLakeCatClientService.getPartitionCount(tableInfo, tableInfo.getSole());
//        table.setPartitionCount(partitionCount);

        //模型
//        Glossary glossary = iLakeCatClientService.getGlossary(MODEL);
//        TableCategories tableRelationship = iLakeCatClientService.getTableRelationship(tableInfo);
//        if (glossary != null) {
//            List<Category> allCategory = glossary.getCategories();
//            List<Category> tableCategory = tableRelationship.getCategories();
//
//            //返回所有的模型
//            List<ModelResponse> list = new ArrayList<>();
//            List<Category> collect = allCategory.stream().map(data -> {
//                ModelResponse modelResponse = new ModelResponse();
//                modelResponse.setName(data.getName()).setId(data.getId())
//                        .setIdValues(new ArrayList<>()).setNameValues(new ArrayList<>());
//                list.add(modelResponse);
//                return data;
//            }).collect(toList());
//
//            //只对表中存在关系的变更
//            if (list != null && !list.isEmpty()) {
//                tableCategory = tableCategory.stream().filter(data -> data.getGlossaryId().equals(glossary.getId()))
//                        .collect(toList());
//                Map<Object, List<Category>> maps = tableCategory.stream().collect(Collectors.groupingBy(Category::getId));
//
//                //默认模型 0 是经典  其他的是其他模型id
//                DictInfo dictInfo = new DictInfo();
//                dictInfo.setDictType(MODEL);
//                List<DictInfo> search = dictMapper.search(dictInfo);
//                Map<String, List<DictInfo>> effectiveMap = search.stream().collect(Collectors.groupingBy(DictInfo::getKey));
//                for (ModelResponse modelResponse : list) {
//                    List<Category> categories = maps.get(modelResponse.getId());
//                    List<DictInfo> dictInfos = effectiveMap.get(modelResponse.getId().toString());
//                    if (dictInfos != null && !dictInfos.isEmpty()) {
//                        DictInfo dbDictInfo = dictInfos.stream().findFirst().get();
//                        if (dbDictInfo.getValue().equals("0")) {
//                            modelResponse.setEffective(true);
//                        }
//                    }
//
//                    if (categories != null) {
//                        Category category = categories.stream().findFirst().get();
//                        List<String> names = new ArrayList<>();
//                        List<Integer> ids = new ArrayList<>();
//                        getModelChain(category, names, ids);
//                        modelResponse.setIdValues(ids).setNameValues(names);
//                        modelResponse.setIsExist(true);
//                    }
//                }
//            }
//
//            List<ModelResponse> responseList = list.stream().sorted((a, b) -> b.getIsExist().compareTo(a.getIsExist()))
//                    .collect(toList());
//            table.setListModel(responseList);
//        }
//
//        //标签
//        Glossary tagGlossary = iLakeCatClientService.getCacheGlossary(TAG);
//        if (tagGlossary != null) {
//            List<Category> tagTableCategory = tableRelationship.getCategories();
//            tagTableCategory = tagTableCategory.stream().filter(data -> data.getGlossaryId().equals(tagGlossary.getId()))
//                    .collect(toList());
//            table.setListTag(tagTableCategory);
//        }

        return table;
    }


    @Override
    public TableInfo getTableOtherInfo(TableInfo tableInfo) {
        //最晚分区值
        if (tableInfo.getClearCache()) {
            iLakeCatClientService.clearCachePartitionName(tableInfo, tableInfo.getSole());
            iLakeCatClientService.clearCachePartitionCount(tableInfo, tableInfo.getSole());
        }
        String latestPartitionName = iLakeCatClientService.getLatestPartitionName(tableInfo, tableInfo.getSole());
        tableInfo.setLatestPartitionName(latestPartitionName);

        //分区数
        Integer partitionCount = iLakeCatClientService.getPartitionCount(tableInfo, tableInfo.getSole());
        tableInfo.setPartitionCount(partitionCount);

        //模型
        Glossary glossary = iLakeCatClientService.getGlossary(MODEL);
        TableCategories tableRelationship = iLakeCatClientService.getTableRelationship(tableInfo);
        if (glossary != null) {
            List<Category> allCategory = glossary.getCategories();
            List<Category> tableCategory = tableRelationship.getCategories();

            Map<Object, List<Category>> allMaps = allCategory.stream().collect(Collectors.groupingBy(Category::getId));
            tableCategory = tableCategory.stream().filter(data -> data.getGlossaryId().equals(glossary.getId()))
                    .collect(toList());

            for (Category category : tableCategory) {
                List<Category> categories = allMaps.get(category.getId());
                if (categories == null) {
                    allCategory.add(category);
                }
            }

            //返回所有的模型
            List<ModelResponse> list = new ArrayList<>();
            List<Category> collect = allCategory.stream().map(data -> {
                ModelResponse modelResponse = new ModelResponse();
                modelResponse.setName(data.getName()).setId(data.getId()).setCreateTime(data.getCreateTime())
                        .setIdValues(new ArrayList<>()).setNameValues(new ArrayList<>());
                list.add(modelResponse);
                return data;
            }).collect(toList());

            //只对表中存在关系的变更
            if (list != null && !list.isEmpty()) {
                Map<Object, List<Category>> maps = tableCategory.stream().collect(Collectors.groupingBy(Category::getId));

                //默认模型 0 是经典  其他的是其他模型id
                DictInfo dictInfo = new DictInfo();
                dictInfo.setDictType(MODEL);
                List<DictInfo> search = dictMapper.search(dictInfo);
                Map<String, List<DictInfo>> effectiveMap = search.stream().collect(Collectors.groupingBy(DictInfo::getKey));
                for (ModelResponse modelResponse : list) {
                    List<Category> categories = maps.get(modelResponse.getId());
                    List<DictInfo> dictInfos = effectiveMap.get(modelResponse.getId().toString());
                    if (dictInfos != null && !dictInfos.isEmpty()) {
                        DictInfo dbDictInfo = dictInfos.stream().findFirst().get();
                        if (dbDictInfo.getValue().equals("0")) {
                            modelResponse.setEffective(true);
                        }
                    }

                    if (categories != null) {
                        Category category = categories.stream().findFirst().get();
                        List<String> names = new ArrayList<>();
                        List<Integer> ids = new ArrayList<>();
                        getModelChain(category, names, ids);
                        modelResponse.setIdValues(ids).setNameValues(names);
                        modelResponse.setIsExist(true);
                    }
                }
            }

//            List<ModelResponse> responseList = list.stream()
////                    .sorted(Comparator.comparing(ModelResponse::getCreateTime))
////                    .sorted((a, b) -> b.getIsExist().compareTo(a.getIsExist()))
//                    .collect(toList());
            tableInfo.setListModel(list);
        }

        //标签
        Glossary tagGlossary = iLakeCatClientService.getCacheGlossary(TAG);
        if (tagGlossary != null) {
            List<Category> tagTableCategory = tableRelationship.getCategories();
            tagTableCategory = tagTableCategory.stream().filter(data -> data.getGlossaryId().equals(tagGlossary.getId()))
                    .collect(toList());
            tableInfo.setListTag(tagTableCategory);
        }


        return tableInfo;
    }


//    @Cacheable(cacheNames = {"collect"}, key = "'getCacheCollectBySole-'+#sole")
    public Map<String, List<CollectInfo>> getCacheCollectBySole(String sole) {
        List<String> keys = new ArrayList<>();
        keys.add(sole);
        //collect
        return collectMapper.searchBykeys(keys).stream().collect(Collectors.groupingBy(CollectInfo::getSole));
    }



    public void getModelChain(Category category, List<String> names, List<Integer> ids) {
        if (category != null) {
            ids.add(category.getId());
            names.add(category.getName());
        }
        if (category.getChildren() == null || category.getChildren().isEmpty()) {
            return;
        }
        Category childernCategory = category.getChildren().stream().findFirst().get();
        getModelChain(childernCategory, names, ids);
    }

    @Override
    public TableInfo updateTable(TableInfo tableInfo) {

        tableInfo.setUpdateTime(LocalDateTime.now().toString());
        String sole = tableInfo.getSole();
        //标签也放进去了  确认创建怎么创建
        iLakeCatClientService.updateTable(tableInfo);
        return null;
    }


    /**
     * 查询 功能
     */
    @Override
    public List<CollectInfo> collectList(TableInfo tableInfo) {
        //是否收藏
        CollectInfo collectInfo = new CollectInfo();
        collectInfo.setUserId(tableInfo.getUserId());
        return collectMapper.searchInfoByUserName(collectInfo);
    }


    public PageInfo<TableInfo> collectPages(CollectInfo collectInfo) {
        List<CollectInfo> collectInfos = collectMapper.searchInfoByUserName(collectInfo);
        List<CollectInfo> collectInfosPage = PageUtil.startPage(collectInfos, collectInfo.getPageNum(), collectInfo.getPageSize());
        PageInfo<CollectInfo> collectInfoPageInfo = new PageInfo<>(collectInfosPage);

        Glossary glossary = iLakeCatClientService.getCacheGlossary(MODEL);
        List<TableInfo> collect = collectInfoPageInfo.getList().stream().map(data -> {
            TableInfo tableInfo = new TableInfo();
            String sole = data.getTableName();
            tableInfo.setCollectTime(data.getCreateTime());
            String[] split = sole.split("\\.");
            if (split.length < 3) {
                tableInfo.setName(data.getSole());
                return tableInfo;
            }
            LakeCatParam lakeCatParam = LakeCatParam.builder().region(split[0])
                    .dbName(split[1]).tableName(split[2]).build();
            TableInfo table = iLakeCatClientService.getTable(lakeCatParam);
            table.setCollectTime(data.getCreateTime());
            TableCategories tableRelationship = iLakeCatClientService.getTableRelationship(table);
            if (glossary != null && tableRelationship != null && tableRelationship.getCategories() != null &&
                    !tableRelationship.getCategories().isEmpty()) {
                List<Category> tableCategory = tableRelationship.getCategories();
                tableCategory = tableCategory.stream().filter(t -> t.getGlossaryId().equals(glossary.getId()))
                        .collect(toList());

                //默认模型 0 是经典  其他的是其他模型id
                DictInfo dictInfo = new DictInfo();
                dictInfo.setDictType(MODEL);
                List<DictInfo> search = dictMapper.search(dictInfo);
                Map<String, List<DictInfo>> effectiveMap = search.stream().collect(Collectors.groupingBy(DictInfo::getKey));

                List<ModelResponse> list = new ArrayList<>();
                for (Category category : tableCategory) {
                    ModelResponse modelResponse = new ModelResponse();
                    List<DictInfo> dictInfos = effectiveMap.get(category.getId().toString());
                    if (dictInfos != null && !dictInfos.isEmpty()) {
                        DictInfo dbDictInfo = dictInfos.stream().findFirst().get();
                        if (dbDictInfo.getValue().equals("0")) {
                            modelResponse.setEffective(true);
                        }
                    }
                    modelResponse.setName(category.getName()).setId(category.getId());
                    list.add(modelResponse);
                    List<String> names = new ArrayList<>();
                    List<Integer> ids = new ArrayList<>();
                    getModelChain(category, names, ids);
                    modelResponse.setIdValues(ids).setNameValues(names);
                    modelResponse.setIsExist(true);
                }
                List<ModelResponse> responseList = list.stream().sorted((a, b) -> b.getIsExist().compareTo(a.getIsExist()))
                        .collect(toList());
                table.setListModel(responseList);
            }
            table.setDbTable(table.getDbName()+"."+table.getTableName());
            table.setName(table.getSole());
            return table;
        }).collect(toList());

        PageInfo<TableInfo> resultPageInfo = new PageInfo<>(collect);
        resultPageInfo.setPageNum(collectInfo.getPageNum());
        resultPageInfo.setPageSize(collectInfo.getPageSize());
        resultPageInfo.setTotal(collectInfos.size());

        return resultPageInfo;
    }

    @Override
    public TableInfo getTableInfoIfNotPresent(Long id, TableInfo tableInfo) {
        return getIfNotPresent(id, tableInfo);
    }


    private static Cache<TableInfo, JSONObject> cache = CacheBuilder.newBuilder()
            .maximumSize(50000)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build();

    /**
     * 查询 功能
     */
    @Override
    public JSONObject search(TableInfo tableInfo) {
        return searchV2(tableInfo);
        //增加缓存
//        JSONObject ifPresent = cache.getIfPresent(tableInfo);
//        TableInfoUserInput userInput = new TableInfoUserInput();
//        if (StringUtils.isNotBlank(tableInfo.getKeyWord())) {
//            userInput.setUserId(tableInfo.getUserId());
//            userInput.setInput(tableInfo.getKeyWord());
//            //写入用户输入表
//            userHistoryService.save(userInput);
//        }
//        //userHistoryService.save()
//        //thread.inputUserHistory(tableInfo);
//        if (ifPresent != null) {
//            return ifPresent;
//        }
//        if (StringUtils.isBlank(tableInfo.getRegion())) {
//            tableInfo.setRegion("");
//        }
//        if (StringUtils.isBlank(tableInfo.getSubject())) {
//            tableInfo.setSubject("");
//        }
//
//        String keyWord = tableInfo.getKeyWord();
//        StringBuilder sb = new StringBuilder();
//        if (StringUtils.isNotBlank(keyWord)) {
//
//            keyWord = keyWord.trim().toLowerCase();
//            String replace;
//            if (keyWord.contains(".")) {
//                String[] split = keyWord.split("\\.");
//                if (split.length == 2) {
//                    tableInfo.setDbName(keyWord.split("\\.")[0].trim());
//                    replace = keyWord.split("\\.")[1].trim();
//                } else {
//                    tableInfo.setDbName(keyWord.split("\\.")[0].trim());
//                    replace = "";
//                }
//            } else {
//                replace = keyWord.trim().toLowerCase();
//            }
//            if (!keyWord.equals("")) {
//                sb.append("%");
//                sb.append(replace);
//                sb.append("%");
//            }
//        }
//
//        JSONObject tasks = new JSONObject();
//        tableInfo.setKeyWordForSearch(sb.toString());
//        tableInfo.setPage((tableInfo.getPage() - 1) * tableInfo.getLimit());
//        long l = System.currentTimeMillis();
//        int count = tableInfoMapper.count(tableInfo);
//        if (count == 0) {
//            tasks.put("results", new ArrayList <>());
//            tasks.put("total", count);
//            return tasks;
//        }
//        tableInfo.setUserName("%" + tableInfo.getUserId() + "%");
//        List<TableInfo> list = tableInfoMapper.searchNew(tableInfo);
//        if (StringUtils.isNotEmpty(tableInfo.getKeyWordForSearch())) {
//            List<TableInfo> dbNumber = new ArrayList<>();
//            List<TableInfo> tableInfos = tableInfoMapper.searchTable(tableInfo);
//            Map<String, List<TableInfo>> collect = tableInfos.stream().collect(Collectors.groupingBy(TableInfo::getDbName));
//            for (Map.Entry<String, List<TableInfo>> entry : collect.entrySet()) {
//                TableInfo dbs = new TableInfo();
//                dbs.setDbName(entry.getKey());
//                dbs.setDbCount(entry.getValue().size());
//                dbNumber.add(dbs);
//            }
//            tasks.put("dbCount", dbNumber);
//        }
//
//        Role role=iLakeCatClientService.getRole(InfTraceContextHolder.get().getUuid());
//        long l1 = System.currentTimeMillis();
//        System.out.println(l1 - l);
//        ArrayList <Object> results = new ArrayList <>();
//        if (list.size() > 0) {
//            for (TableInfo element : list) {
//                JSONObject j=getSearchElement((element));
//                if (role.getRolePrivileges()!=null&&role.getRolePrivileges().length>0){
//                    for (RolePrivilege rolePrivilege:role.getRolePrivileges()){
//                        if (ObjectType.TABLE.printName.equals(rolePrivilege.getGrantedOn())){
//                            String name=rolePrivilege.getName();
//                            if (name.indexOf("*")>-1){
//                                if (name.toLowerCase().indexOf(element.getDbName().toLowerCase())>-1){
//                                    j.put("tableAuth",true);
//                                    break;
//                                }
//                            }else {
//                                if (name.equalsIgnoreCase(element.getRegion()+"."+element.getDbName()+"."+element.getName())){
//                                    j.put("tableAuth",true);
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
//                results.add(j);
//            }
//        }
//        Collections.sort(results, new Comparator <Object>() {
//            @Override
//            public int compare(Object o1, Object o2) {
//                JSONObject jsonObject1 = (JSONObject) o1;
//                JSONObject jsonObject2 = (JSONObject) o2;
//                Integer c1 = jsonObject1.getInteger("count");
//                Integer c2 = jsonObject2.getInteger("count");
//                c1 = c1 == null ? 0 : c1;
//                c2 = c2 == null ? 0 : c2;
//                return c2.compareTo(c1);
//            }
//        });
//        tasks.put("results", results);
//        tasks.put("total", count);
//        //设置缓存标量
//        cache.put(tableInfo, tasks);
//        return tasks;
    }

    /**
     * 元数据直接改造
     */
    public JSONObject searchV2(TableInfo tableInfo) {
        //增加缓存
        JSONObject ifPresent = cache.getIfPresent(tableInfo);
        //thread.inputUserHistory(tableInfo);
        if (StringUtils.isNotBlank(tableInfo.getKeyWord())) {
            TableInfoUserInput userInput = new TableInfoUserInput();
            userInput.setUserId(tableInfo.getUserId());
            userInput.setInput(tableInfo.getKeyWord());
            //写入用户输入表
            userHistoryService.save(userInput);
        }
//        if (ifPresent != null) {
//            return ifPresent;
//        }
        if (StringUtils.isBlank(tableInfo.getRegion())) {
            tableInfo.setRegion("");
        }
        if (StringUtils.isBlank(tableInfo.getSubject())) {
            tableInfo.setSubject("");
        }
        if (StringUtils.isNotBlank(tableInfo.getKeyWord())) {
            tableInfo.setKeyWord(tableInfo.getKeyWord().trim());
        }

        JSONObject tasks = new JSONObject();
        //String region, String keywords, Integer size, String pageToken

        LakeCatParam lakeCatParam = LakeCatParam.builder().region(tableInfo.getRegion())
                .secondaryKeywords(tableInfo.getSecondaryKeywords())
                .categoryId(tableInfo.getCategoryId())
                .keywords(tableInfo.getKeyWord())
                .size(tableInfo.getLimit()).pageToken(tableInfo.getPageToken()).dbName(tableInfo.getDbName()).build();
        List<TableInfo> tableInfos = iLakeCatClientService.searchTable(lakeCatParam);
        long l = System.currentTimeMillis();
        if (tableInfos.isEmpty()) {
            tasks.put("results", new ArrayList<>());
            tasks.put("nextMarker", lakeCatParam.getNextMarker());
            tasks.put("previousMarker", lakeCatParam.getPreviousMarker());
            return tasks;
        }

        List<String> keys = tableInfos.stream().map(TableInfo::getKey).collect(toList());
        //collect
        Map<String, List<CollectInfo>> collectMap = collectMapper.searchBykeys(keys).stream().collect(Collectors.groupingBy(CollectInfo::getSole));

        //userList count
        Map<String, List<LastActivityInfo>> lastMap = lastActivityMapper.searchBykeys(keys).stream().collect(Collectors.groupingBy(LastActivityInfo::getSole));

        //tags
        Map<String, List<TableTagInfo>> collect = tableTagInfoMapper.searchBykeys(keys).stream().collect(Collectors.groupingBy(TableTagInfo::getSole));

        List<TableInfo> transList = tableInfos.stream().map(data -> {
            List<CollectInfo> collectInfos = collectMap.get(data.getKey());
            List<LastActivityInfo> lastActivityInfos = lastMap.get(data.getKey());
            List<TableTagInfo> tableTagInfos = collect.get(data.getKey());
            if (collectInfos != null) {
                String userList = collectInfos.stream().findFirst().get().getUserList();
                if (StringUtils.isNotEmpty(userList) && userList.contains(tableInfo.getUserId())) {
                    data.setFlag(1);
                }
                data.setCollect(collectInfos.stream().findFirst().get().getCollect());
            }
            if (lastActivityInfos != null) {
                String userList = lastActivityInfos.stream().findFirst().get().getUserList();
                data.setUserList(userList);
                data.setCount(lastActivityInfos.stream().findFirst().get().getCount());
            }
            if (tableTagInfos != null) {
                data.setTags(tableTagInfos.stream().findFirst().get().getTags());
            }
            return data;
        }).collect(toList());

        long l1 = System.currentTimeMillis();
        System.out.println(l1 - l);
        ArrayList<Object> results = new ArrayList<>();

        //给当前表加权限
        Role role = iLakeCatClientService.getRole(InfTraceContextHolder.get().getUuid());
        if (transList.size() > 0) {
            for (TableInfo element : tableInfos) {
                JSONObject searchElement = getSearchElement(element);
                //给列表加当前用户组是否有权限
                if (role.getRolePrivileges() != null && role.getRolePrivileges().length > 0) {
                    for (RolePrivilege rolePrivilege : role.getRolePrivileges()) {
                        if (ObjectType.TABLE.printName.equals(rolePrivilege.getGrantedOn())) {
                            String name = rolePrivilege.getName();
                            if (name.indexOf("*") > -1) {
                                if (name.toLowerCase().indexOf(element.getDbName().toLowerCase()) > -1) {
                                    searchElement.put("tableAuth", true);
                                    break;
                                }
                            } else {
                                if (name.equalsIgnoreCase(element.getRegion() + "." + element.getDbName() + "." + element.getName())) {
                                    searchElement.put("tableAuth", true);
                                    break;
                                }
                            }
                        }
                    }
                }
                //====
                results.add(searchElement);
            }
        }
        TableInfo firstTable = tableInfos.stream().findFirst().get();
        tasks.put("results", results);
        tasks.put("nextMarker", firstTable.getNextMarker());
        tasks.put("previousMarker", firstTable.getPreviousMarker());
        //设置缓存标量
        cache.put(tableInfo, tasks);
        return tasks;
    }

    public JSONObject getSearchElement(TableInfo element) {

//        JSONObject object = new JSONObject();
//        //表id
//        object.put("id", element.getId());
//        //标题 中文名+库名+表名
//        object.put("title", element.title());
//        object.put("cnName", element.getCnName());
//        object.put("db_name", element.getDbName());
//        object.put("table_name", element.getName());
//        //所在区域
//        object.put("region", element.getRegion());
//        //数据分层 取消
//        object.put("hierarchical", element.getHierarchical());
//        //主题域 取消
//        object.put("topic", element.getSubject());
//        //所属应用数据写死 取消
//        object.put("application", element.getApplication());
//        //表类型 取消
//        object.put("table_type", element.getUpdateType());
//        //更新频次 取消
//        object.put("interval", element.getInterval());
//        //分区类型 取消
//        object.put("Partition_type", element.getPartitionType());
//        //生命周期 取消
//        object.put("lifecycle", element.getLifecycle());
//
//        object.put("description", element.getDescription());
//
//        object.put("subject", element.getSubject());
//        //表owner
//        object.put("owner", element.getOwner());
//        //创建时间
//        object.put("create_time", element.getCreateTime());
//        //更新时间
//        object.put("update_time", element.getUpdateTime());
//        //表行数
//        object.put("num_rows", element.getNumRows());
//        //占用存储
//        object.put("byte_size", element.getByteSize());
//        //location
//        object.put("location", element.getLocation());
//
//
//        object.put("collect", element.getCollect() == null ? 0 : element.getCollect());
//
//        object.put("count", element.getCount());
//        //是否是高频表
//        try {
//            object.put("level", govTagService.getTableVisitTimesTag(element.getCount()));
//
//            object.put("storage", getStorageSizeTag(element));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        object.put("recents", element.getUserList());
//        //查询当前表的收藏个数
//        object.put("flag", element.getFlag() == 1);
//
//        object.put("tags", element.getTags());
//        List<DataGrade> dataGradeList=dataGradeService.getByTableId(element.getId());
//        if (CollectionUtils.isNotEmpty(dataGradeList)){
//            dataGradeList=dataGradeList.stream().filter(dataGrade -> dataGrade.getGradeType()==1).collect(toList());
//            if (CollectionUtils.isNotEmpty(dataGradeList)){
//                object.put("tableLevel", dataGradeList.get(0).getGrade());
//            }
//        }
//        return object;
        JSONObject object = new JSONObject();
        //表id
        object.put("id", element.getId());
        //标题 中文名+库名+表名
        object.put("title", element.title());
        object.put("cnName", element.getCnName());
        object.put("db_name", element.getDbName());
        object.put("table_name", element.getName());
        //所在区域
        object.put("region", element.getRegion());
//        //数据分层 取消
//        object.put("hierarchical", element.getHierarchical());
//        //主题域 取消
//        object.put("topic", element.getSubject());
//        //所属应用数据写死 取消
//        object.put("application", element.getApplication());
//        //表类型 取消
//        object.put("table_type", element.getUpdateType());
//        //更新频次 取消
//        object.put("interval", element.getInterval());
        //分区类型 取消
        object.put("Partition_type", element.getPartitionType());
//        //生命周期 取消
//        object.put("lifecycle", element.getLifecycle());

        object.put("description", element.getDescription());

//        object.put("subject", element.getSubject());
        //表owner
        object.put("owner", element.getOwner());
        //创建时间
        object.put("create_time", element.getCreateTime());
        //更新时间
        object.put("update_time", element.getUpdateTime());
        //最后一次访问时间
        object.put("lastAccessTime", element.getLastAccessTime());
        //访问次数
        object.put("recentVisitCount", element.getRecentVisitCount());
        //表行数
        object.put("num_rows", element.getNumRows());
        //占用存储
        object.put("byte_size", element.getByteSize());
        //location
        object.put("location", element.getLocation());


        object.put("collect", element.getCollect() == null ? 0 : element.getCollect());

        object.put("count", element.getCount());
        //是否是高频表
        try {
//            object.put("level", govTagService.getTableVisitTimesTag(element.getCount()));
            object.put("level", govTagService.getTableVisitTimesTag(element.getRecentVisitCount().doubleValue()));

            object.put("storage", getStorageSizeTag(element));
        } catch (Exception e) {
            e.printStackTrace();
        }
        object.put("recents", element.getUserList());
        //查询当前表的收藏个数
        object.put("flag", element.getFlag() == 1);

        object.put("tags", element.getTags());

        //模型
        object.put("listModel", element.getListModel());
        return object;
    }

    private String getStorageSizeTag(TableInfo element) {
        GovTagEntity tag = govTagService.getSingleTag(
                String.format("%s.%s.%s", element.getRegion(), element.getDbName(), element.getName()),
                "tableStorageSizeGrade");
        if (tag != null) {
            return tag.getTagValue();
        }
        return null;
    }


    public boolean syncInfo(LakeCatClient lakeCatClient, String tenantName, String region, Boolean flag) {
        List<String> regions = new ArrayList<>();
        regions.add(region);
        //根据规则id获取报警信息
        //return alertSendThread.doSendMonitorAlertInfo(lakeCatClient, regions, tenantName, flag);
        return alertSendThread.doSendMonitorAlertInfoSingle(lakeCatClient, regions, tenantName, flag);
    }

    public boolean sync(String tenantName, String region, Boolean flag, boolean update) {
        List<TableInfo> tableInfos = Lists.newArrayList();
        PagedList<Database> databasePagedList = getDbInfo(tenantName, region);
        List<TableInfo> currentTableInfos = tableInfoMapper.getNameAndIdMapByRegion2(region);
        if (databasePagedList != null) {
            Database[] databases = databasePagedList.getObjects();
            log.info("当前租户为:" + tenantName + "region" + region + "  数据库数量为" + databases.length);
            if (databases.length > 0) {
                for (Database database : databases) {
                    List<Table> totalTables = getTableInfo(database, tenantName);
                    log.info("当前租户为:" + tenantName + "  数据库数量为" + database.getDatabaseName() + "，表的数量为:" + totalTables.size());
                    if (CollectionUtils.isNotEmpty(totalTables)) {
                        for (Table table : totalTables) {
                            TableInfo tableInfo = new TableInfo();
                            tableInfo.setDbName(table.getDatabaseName());
                            tableInfo.setName(table.getTableName());
                            tableInfo.setOwner(table.getOwner());
                            Map<String, String> properties = table.getParameters();
                            String towner = properties.get("owner");
                            if (towner != null) {
                                String[] split = towner.split("#");
                                if (split.length == 2) {
                                    tableInfo.setOwner(split[1]);
                                } else {
                                    tableInfo.setOwner(split[0]);
                                }
                            }
                            tableInfo.setColumns(transColumnOutPut(table.getFields()));
                            List<Column> partitions = table.getPartitionKeys();
                            tableInfo.setPartitionKeys(transColumnOutPut(partitions));
                            if (!partitions.isEmpty()) {
                                tableInfo.setPartitionType(1);
                            } else {
                                tableInfo.setPartitionType(0);
                            }

                            if (table.getParameters() != null) {
                                tableInfo.setDescription(table.getParameters().getOrDefault("comment", ""));
                            }
                            StorageDescriptor tableStorage = table.getStorageDescriptor();
                            if (tableStorage != null) {
                                String location = tableStorage.getLocation();
                                tableInfo.setLocation(location);
                                tableInfo.setSdFileFormat(tableStorage.getSourceShortName());
                            }
                            try {
                                tableInfo.setRegion(region);
                            } catch (Exception e) {
                                log.error("", e);
                                continue;
                            }
                            tableInfo.setType(LakecatTableUtils.getTableType(table));
                            tableInfos.add(tableInfo);
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(tableInfos)) {
                    List<String> newCurrent = tableInfos.stream().map(x -> {
                        return x.getRegion() + "." + x.getDbName() + "." + x.getName();
                    }).distinct().collect(toList());

                    List<String> current = Lists.newArrayList();
                    setTenantName(tenantName);
                    List<TableInfo> insert = Lists.newArrayList();
                    if (CollectionUtils.isNotEmpty(currentTableInfos)) {
                        current = currentTableInfos.stream().map(x -> {
                            return x.getRegion() + "." + x.getDbName() + "." + x.getName();
                        }).distinct().collect(toList());
                        for (TableInfo tableInfo : tableInfos) {
                            boolean find = false;
                            for (TableInfo curerentTableInfo : currentTableInfos) {
                                if ((curerentTableInfo.getRegion() + "." + curerentTableInfo.getDbName() + "." + curerentTableInfo.getName())
                                        .equals(tableInfo.getRegion() + "." + tableInfo.getDbName() + "." + tableInfo.getName())) {
                                    find = true;
                                    if (update) {
                                        tableInfo.setId(curerentTableInfo.getId());
                                        tableInfo.setSubject(curerentTableInfo.getSubject());
                                        tableInfo.setDescription(curerentTableInfo.getDescription());
                                        tableInfoMapper.updateById(tableInfo);
                                    }
                                    break;
                                }
                            }
                            if (!find) {
                                insert.add(tableInfo);
                            }
                        }
                    } else {
                        insert.addAll(tableInfos);
                    }
                    if (CollectionUtils.isNotEmpty(insert)) {
                        List<List<TableInfo>> lists = PubMethod.subList(insert, 100);
                        for (List<TableInfo> list : lists) {
                            setTenantName(tenantName);
                            tableInfoMapper.batchSave(list);
                        }
                        insert.clear();
                    }
                    current.removeAll(newCurrent);
                    if (!flag && CollectionUtils.isNotEmpty(current)) {
                        for (TableInfo t : currentTableInfos) {
                            String key = t.getRegion() + "." + t.getDbName() + "." + t.getName();
                            setTenantName(tenantName);
                            if (current.contains(key)) {
                                tableInfoMapper.deleteTable(t.getDbName(), t.getName(), t.getRegion());
                                dataGradeService.deleteTableById(t.getId());
                            }

                        }
                    }
                } else {
                    if (!flag && CollectionUtils.isNotEmpty(currentTableInfos)) {
                        for (TableInfo t : currentTableInfos) {
                            setTenantName(tenantName);
                            tableInfoMapper.deleteTable(t.getDbName(), t.getName(), t.getRegion());
                            dataGradeService.deleteTableById(t.getId());
                        }
                    }
                }
            }
        } else {
           /* if (!flag && CollectionUtils.isNotEmpty(currentTableInfos)) {
                for (TableInfo t : currentTableInfos) {
                    setTenantName(tenantName);
                    tableInfoMapper.deleteTable(t.getDbName(), t.getName(), t.getRegion());
                    dataGradeService.deleteTableById(t.getId());
                }
            }*/
        }
        tableInfos.clear();
        return true;
    }


    private void setTenantName(String tenantName) {
        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        String compareForTenantName = userInfo.getTenantName();
        if (!compareForTenantName.equals(tenantName)) {
            userInfo.setTenantName(tenantName);
        }
    }

    private List<Table> getTableInfo(Database database, String tenantName) {
        List<Table> totalTables = new ArrayList<>();
        String databaseName = String.valueOf(database.getDatabaseName());
        ListTablesRequest listTablesRequest = new ListTablesRequest();
        listTablesRequest.setCatalogName(database.getCatalogName());
        listTablesRequest.setDatabaseName(databaseName);
        listTablesRequest.setProjectId(tenantName);
        listTablesRequest.setMaxResults(1000);
        String pageToken = "";
        while (pageToken != null) {
            if (!"".equals(pageToken)) {
                listTablesRequest.setNextToken(pageToken);
            }
            try {
                PagedList<Table> tablePagedList = InfTraceContextHolder.get().getLakeCatClient().listTables(listTablesRequest);
                Table[] tables = tablePagedList.getObjects();
                Collections.addAll(totalTables, tables);
                pageToken = tablePagedList.getNextMarker();
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return totalTables;
    }


    private PagedList<Database> getDbInfo(String tenantName, String region) {
        try {
            ListDatabasesRequest listDatabasesRequest = new ListDatabasesRequest();
            listDatabasesRequest.setProjectId(tenantName);
            try {
                listDatabasesRequest.setCatalogName(region);
            } catch (Exception e) {
                log.error("");
            }
            return InfTraceContextHolder.get().getLakeCatClient().listDatabases(listDatabasesRequest);
        } catch (Exception e) {
        }
        return null;
    }


    private void setLastActivity(String tenantName, TableInfo tableInfo, List<LastActivityInfo> lastActivityInfoList) {
        try {
            TableInfoReq tableInfoReq = new TableInfoReq();
            tableInfoReq.setRegion(tableInfo.getRegion());
            tableInfoReq.setDatabaseName(tableInfo.getDbName());
            tableInfoReq.setTableName(tableInfo.getName());
            tableInfoReq.setTenantName(tenantName);
            Long count = 0L;//给大整数赋初值为0
            List<TableUsageProfileGroupByUser> profiles = iTableInfoService.tableProfileInfo(tableInfoReq, 30);
            if (profiles != null && !profiles.isEmpty()) {
                for (TableUsageProfileGroupByUser profile : profiles) {
                    if (profile == null) {
                        continue;
                    }
                    count += profile.getSumCount().longValue();
                    LastActivityInfo lastActivityInfo = new LastActivityInfo();
                    lastActivityInfo.setRegion(tableInfo.getRegion());
                    lastActivityInfo.setDbName(tableInfo.getDbName());
                    lastActivityInfo.setTableName(tableInfo.getName());
                    lastActivityInfo.setUserId(profile.getUserId());
                    lastActivityInfo.setSumCount(profile.getSumCount());
                    lastActivityInfo.setAvgCount(profile.getAvgCount());
                    lastActivityInfo.setSole(tableInfo.getSole());
                    lastActivityInfo.setRecentlyVisitedTimestamp(DateUtil.getDateToString(profile.getRecentlyVisitedTimestamp()));
                    lastActivityInfoList.add(lastActivityInfo);
                }
            }
            tableInfo.setLastActivityCount(count);
        } catch (Exception e) {
            log.error("", e);
        }
    }


    @Override
    public void syncLast() {
        log.info("开始同步最近访问数据");
        List<LastActivityInfo> lastActivityInfoList = new ArrayList<>();
        List<TableInfo> tableInfoList = new ArrayList<>();
        List<TableInfo> currentTableInfos = tableInfoMapper.getTableInfoList();
        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        LakeCatClient lakeCatClient = InfTraceContextHolder.get().getLakeCatClient();
        String tenantName = userInfo.getTenantName();


        List<List<TableInfo>> listTableInfo = PubMethod.subList(currentTableInfos, 100);
        int i = 0;
        for (List<TableInfo> tableInfos : listTableInfo) {
            i++;
            Iterator<TableInfo> iterator = tableInfos.iterator();
            while (iterator.hasNext()) {
                TableInfo currentTableInfo = iterator.next();
                log.info("正在执行的表为 " + currentTableInfo.getRegion() + "." + currentTableInfo.getDbName() + "." + currentTableInfo.getName());
                try {
                    setLastActivity(tenantName, currentTableInfo, lastActivityInfoList);
                } catch (Exception e) {
                }
                String tableName = currentTableInfo.getName();
                String databaseName = currentTableInfo.getDbName();
                String region = currentTableInfo.getRegion();
                String catologName = CatalogNameEnum.getCatalogName(region);
                try {
                    GetTableRequest getTableRequest = new GetTableRequest(tenantName,
                            catologName, databaseName, tableName);
                    Table table = lakeCatClient.getTable(getTableRequest);
                    Map<String, String> properties = table.getParameters();
                    String transientLastDdlTime = properties.get("transient_lastDdlTime");
                    if (transientLastDdlTime != null) {
                        currentTableInfo.setTransientLastDdlTime(DateUtil.getDateToString(Long.parseLong(transientLastDdlTime) * 1000));
                    }
                } catch (Exception e) {
                }
                tableInfoList.add(currentTableInfo);
            }
            if (CollectionUtils.isNotEmpty(tableInfos)) {
                tableInfoMapper.batchUpdate(tableInfos);
                tableInfos.clear();
            }
            if (CollectionUtils.isNotEmpty(lastActivityInfoList)) {
                tableInfoMapper.batchSaveForUsers(lastActivityInfoList);
                lastActivityInfoList.clear();
            }

            System.out.println("***********************************************该批次执行完成" + i);
        }
    }


    @Override
    public void synchronization(String tenantName, String region, Boolean flag, boolean update) {
        sync(tenantName, region, flag, update);
    }


    @Override
    public List<JSONObject> init() {
        return collectMapper.searchList();
    }


    @Override
    public List<String> getDBList(String PROJECT_ID, String region) {
        Database[] dbListBySDK = getDBListBySDK(PROJECT_ID, region);
        if (dbListBySDK == null) {
            return new ArrayList<>();
        }
        List<String> DBList = new ArrayList<>();
        for (Database database : dbListBySDK) {
            String databaseName = database.getDatabaseName();
            DBList.add(databaseName);
        }
        return DBList;
    }

    @Override
    public List<Database> searchDbList(String PROJECT_ID, String region) {
        Database[] dbListBySDK = getDBListBySDK(PROJECT_ID, region);
        if (dbListBySDK == null) {
            return new ArrayList<>();
        }
        List<Database> DBList = new ArrayList<>();
        DBList.addAll(Arrays.asList(dbListBySDK));
        return DBList;
    }


    /**
     * 数据预览
     *
     * @param table: 库名.表名, region
     */
    @Override
    public List<Object> getData(String table, String region, Integer size, String tenantName, String userName) {
        if (size == null) {
            size = 5;
        }
        TableDataInfo sqlData = tableInfoMapper.getSqlData(table, region, size);

        if (sqlData == null || sqlData.getData() == null || JSON.parseArray(sqlData.getData()).isEmpty()) {
            String sql = "select * from " + table + " limit " + size;
            JSONArray sqlResult = gatewayJDBCUtil.executeSqlHasResult(sql, region, tenantName, userName, InfTraceContextHolder.get().getUuid(), table.split("\\.")[0]);//olapJdbcUtil.getSqlResult(sql, region, tenantName, userName);
            sqlData = new TableDataInfo();
            sqlData.setTableName(table);
            sqlData.setRegion(region);
            sqlData.setSql(sql);
            sqlData.setSize(size);
            sqlData.setData(sqlResult.toJSONString());
            tableInfoMapper.insertForData(sqlData);
        }
        //是否有查询数据预览的权限
        return JSON.parseArray(sqlData.getData());
    }

    @Override
    public boolean changeTableDetail(TableInfo entity) throws BusinessException {
        entity.setUpdateTime(LocalDateTime.now().toString());
        String sole = entity.getSole();
        List<TableTagInfo> collect = entity.getTagList();
        List<TableTagInfo> tagList = collect.stream().map(data -> {
            data.setSole(sole);
            return data;
        }).collect(toList());

        boolean b = alterOwnerByLakecat(entity, entity.getOwner(), entity.getRegion(), entity.getTenantName())
                && tableInfoMapper.updateDetailByKey(entity);
        //查询tags
        List<TableTagInfo> search = tableTagInfoService.search(entity.getSole());
        List<String> currentStr = tagList.stream().map(x -> {
            return x.getSole();
        }).distinct().collect(toList());
        if (search != null) {
            if (search.size() > tagList.size()) {
                for (TableTagInfo tableTagInfo : search) {
                    if (currentStr.contains(tableTagInfo.getSole())) {
                        continue;
                    }
                    tableTagInfoService.deleteTags(tableTagInfo.getId());
                }
            }
        }

        b = b && tableTagInfoService.addTableTag(tagList);
        cacheTableInfo(entity.getId(), tableInfoService.getTableDetail(entity));
        return b;
    }


    /*
     * sql校验
     * */
    @Override
    public void isRightSQL(String sql, String region, String tenantName, String userName) throws BusinessException {
        String SQL = "explain " + sql;
        olapJdbcUtil.getSqlResult(SQL, region, tenantName, userName);
    }

    /*
     * 删除，更改字段
     * */
    @Override
    public boolean alterColumns(TableInfo entity) throws BusinessException {
        //是否有修改字段的权限

        Map<String, Column> oldColumns = getColumnInputsMap(
                JSON.parseArray(getTableDetail(entity).getColumns()), "id", new ArrayList<>());
        Map<String, Column> newColumns = getColumnInputsMap(JSON.parseArray(entity.getColumns()), "id", new ArrayList<>());
        alterColumns(oldColumns, newColumns, entity);
        entity.setUpdateTime(LocalDateTime.now().toString());
        boolean b = this.updateById(entity);
        cacheTableInfo(entity.getId(), entity);
        return b;
    }

    /*
     * 删除，更改字段
     * */
    @Override
    public boolean alterColumns(AlterColumnVo alterColumnVo) throws BusinessException {
        //是否有修改字段的权限
        alterColumnsByLakecat(alterColumnVo);
//        TableInfo tableInfo = tableInfoMapper.selectById(alterColumnVo.getId());
//        if (tableInfo == null) {
//            return true;
//        }
//        tableInfo.setUpdateTime(LocalDateTime.now().toString());
//        tableInfo.setColumns(alterColumnVo.getColumns());
//        boolean b = this.updateById(tableInfo);
//        cacheTableInfo(tableInfo.getId(), tableInfo);
        return true;
    }

    // 添加字段
    @Override
    public boolean addColumns(TableInfo entity) throws BusinessException {

        List<String> list = new ArrayList<>();
        Map<String, Column> newColumns = getColumnInputsMap(JSON.parseArray(entity.getColumns()), "name", list);
        long count = list.stream().distinct().count();
        if (list.size() != count) {
            return false;
        }
        String tableName = entity.getName();
        String dbName = entity.getDbName();
        String catalogName = CatalogNameEnum.getCatalogNameByRegion(entity.getRegion());
        TableInfo tableDetail = getTableDetail(entity);
        Map<String, Column> oldColumns = getColumnInputsMap(JSON.parseArray(tableDetail.getColumns()), "name", new ArrayList<>());
        List<Column> columnList = new ArrayList<>();
        boolean isAddColumn = false;
        for (Map.Entry<String, Column> newColumnEntry : newColumns.entrySet()) {
            if (!oldColumns.containsKey(newColumnEntry.getKey())) {
                isAddColumn = true;
                columnList.add(newColumnEntry.getValue());
            }
        }

        try {
            if (isAddColumn) {
                ColumnChangeInput columnChangeInput = new ColumnChangeInput();
                columnChangeInput.setChangeType(Operation.ADD_COLUMN);
                columnChangeInput.setColumnList(columnList);
                iLakeCatClientService.get().alterColumn(
                        new AlterColumnRequest(entity.getTenantName(), catalogName, dbName, tableName, columnChangeInput));
            }

            GetTableRequest getTableRequest = new GetTableRequest(entity.getTenantName(), catalogName, dbName, tableName);
            Table table = iLakeCatClientService.get().getTable(getTableRequest);
            entity.setColumns(transColumnOutPut(table.getFields()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage(), -1);
        }

        entity.setUpdateTime(LocalDateTime.now().toString());
//        boolean b = this.updateById(entity);
//        cacheTableInfo(entity.getId(), entity);
        return true;
    }


    @Override
    public boolean alterTableBetweenByLakecat(Long id, String owner, String region,
                                              String targetTenantName, String originTenantName, String dbName,
                                              String tableName) throws BusinessException {
        try {
            TableInfo tableInfo = new TableInfo();
            tableInfo.setRegion(region).setDbName(dbName).setTableName(tableName);

            GetTableRequest getTableRequest = new GetTableRequest(originTenantName, CatalogNameEnum.getCatalogName(region), dbName, tableName);
            Table table = iLakeCatClientService.get().getTable(getTableRequest);
            AlterTableRequest request = new AlterTableRequest();
            request.setCatalogName(CatalogNameEnum.getCatalogName(region));
            request.setDatabaseName(dbName);
            request.setProjectId(targetTenantName);
            AlterTableInput alterTableInput = new AlterTableInput();
            TableInput tableInput = new TableInput();
            tableInput.setTableType(table.getTableType());
            tableInput.setCatalogName(table.getCatalogName());
            tableInput.setAuthSourceType(table.getAuthSourceType());
            tableInput.setAccountId(table.getAccountId());
            tableInput.setLastAccessTime(table.getLastAccessTime());
            tableInput.setViewExpandedText(table.getViewExpandedText());
            tableInput.setViewOriginalText(table.getViewOriginalText());
            //tableInput.setLastAnalyzedTime(table.get());
            StorageDescriptor storageDescriptor = table.getStorageDescriptor();
            tableInput.setStorageDescriptor(storageDescriptor);
            tableInput.setDescription(table.getDescription());
            tableInput.setCreateTime(table.getCreateTime());
            tableInput.setLmsMvcc(table.isLmsMvcc());
            tableInput.setTableName(tableName);
            tableInput.setPartitionKeys(table.getPartitionKeys());
            tableInput.setParameters(table.getParameters());
            tableInput.setRetention(table.getRetention().intValue());
            // Alter owner
            tableInput.setOwner(owner);
            tableInput.setOwnerType(table.getOwnerType());
            alterTableInput.setTable(tableInput);
            request.setInput(alterTableInput);
            request.setTableName(tableName);
            iLakeCatClientService.get().alterTable(request);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Alter Owner Exception: {}", e.getMessage());
            throw new BusinessException("Alter Owner Exception: " + e.getMessage(), -1);
        }
    }

    @Override
    public boolean alterOwnerByLakecat(TableInfo tableInfo, String owner, String region, String tenantName) throws BusinessException {
        try {

//            TableInfo tableDetail = tableInfoMapper.getTableDetail(id);
            tableInfo.setRegion(region).setDbName(tableInfo.getDbName()).setTableName(tableInfo.getTableName());
            TableInfo tableDetail = getTableDetail(tableInfo);

            String dbName = tableDetail.getDbName();
            String name = tableDetail.getName();
            GetTableRequest getTableRequest = new GetTableRequest(tenantName, CatalogNameEnum.getCatalogName(region), dbName, name);
            Table table = iLakeCatClientService.get().getTable(getTableRequest);
            AlterTableRequest request = new AlterTableRequest();
            request.setCatalogName(CatalogNameEnum.getCatalogName(region));
            request.setDatabaseName(dbName);
            request.setProjectId(tenantName);
            AlterTableInput alterTableInput = new AlterTableInput();
            TableInput tableInput = new TableInput();
            tableInput.setTableType(table.getTableType());
            tableInput.setCatalogName(table.getCatalogName());
            tableInput.setAuthSourceType(table.getAuthSourceType());
            tableInput.setAccountId(table.getAccountId());
            tableInput.setLastAccessTime(table.getLastAccessTime());
            tableInput.setViewExpandedText(table.getViewExpandedText());
            tableInput.setViewOriginalText(table.getViewOriginalText());
            //tableInput.setLastAnalyzedTime(table.get());
            StorageDescriptor storageDescriptor = table.getStorageDescriptor();
            tableInput.setStorageDescriptor(storageDescriptor);
            tableInput.setDescription(tableInfo.getDescription());
            tableInput.setCreateTime(table.getCreateTime());
            tableInput.setLmsMvcc(table.isLmsMvcc());
            tableInput.setTableName(name);
            tableInput.setPartitionKeys(table.getPartitionKeys());
            tableInput.setParameters(table.getParameters());
            tableInput.setRetention(table.getRetention().intValue());
            // Alter owner
            tableInput.setOwner(owner);
            tableInput.setOwnerType(table.getOwnerType());
            alterTableInput.setTable(tableInput);
            request.setInput(alterTableInput);
            request.setTableName(name);
            iLakeCatClientService.get().alterTable(request);

//            boolean b = tableInfoMapper.updateOwner(id, owner);
            tableInfo.setOwner(owner);
            boolean b = tableInfoMapper.updateOwnerByName(tableInfo);
            return b;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Alter Owner Exception: {}", e.getMessage());
            throw new BusinessException("Alter Owner Exception: " + e.getMessage(), -1);
        }
    }


    @Override
    public boolean createTableByLakecat(Long id, String owner, String region, String tenantName) throws BusinessException {
        TableInfo tableDetail = tableInfoMapper.getTableDetail(id);
        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        String outputTenantName = userInfo.getTenantName();
        try {
            String dbName = tableDetail.getDbName();
            String name = tableDetail.getName();
            DatabaseInfo databaseInfo = new DatabaseInfo();
            databaseInfo.setRegion(region);
            databaseInfo.setDatabaseName(dbName);
            databaseInfo.setDescription("");
            databaseInfo.setLocationUri("");
            databaseInfo.setUserId(tableDetail.getOwner());
            databaseInfo.setProjectId(tenantName);
            createDatabase(databaseInfo);
            GetTableRequest getTableRequest = new GetTableRequest(outputTenantName, CatalogNameEnum.getCatalogName(region), dbName, name);
            Table table = iLakeCatClientService.get().getTable(getTableRequest);
            CreateTableRequest request = new CreateTableRequest();
            request.setCatalogName(CatalogNameEnum.getCatalogName(region));
            request.setDatabaseName(dbName);
            request.setProjectId(userInfo.getTenantName());
            TableInput tableInput = new TableInput();
            tableInput.setTableType(table.getTableType());
            tableInput.setCatalogName(table.getCatalogName());
            tableInput.setAuthSourceType(table.getAuthSourceType());
            tableInput.setAccountId(table.getAccountId());
            tableInput.setLastAccessTime(table.getLastAccessTime());
            tableInput.setViewExpandedText(table.getViewExpandedText());
            tableInput.setViewOriginalText(table.getViewOriginalText());
            //tableInput.setLastAnalyzedTime(table.get());
            StorageDescriptor storageDescriptor = table.getStorageDescriptor();
            tableInput.setStorageDescriptor(storageDescriptor);
            tableInput.setDescription(table.getDescription());
            tableInput.setCreateTime(table.getCreateTime());
            tableInput.setLmsMvcc(table.isLmsMvcc());
            tableInput.setTableName(name);
            tableInput.setPartitionKeys(table.getPartitionKeys());
            tableInput.setParameters(table.getParameters());
            tableInput.setRetention(table.getRetention().intValue());
            // Alter owner
            tableInput.setOwner(owner);
            tableInput.setOwnerType(table.getOwnerType());
            request.setInput(tableInput);
            iLakeCatClientService.get().createTable(request);
            tableDetail.setOwner(owner);
            userInfo.setTenantName(tenantName);
            InfTraceContextHolder.get().setUserInfo(userInfo);
            tableDetail.setCreateType(0);
            return tableInfoMapper.insertForTableInfo(tableDetail);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Alter Owner Exception: {}", e.getMessage());
            return false;
        }
    }


    private List<Column> transition(List<Column> schema) {
        List<Column> columns = new ArrayList<>();
        for (Column output : schema) {
            Column input = new Column();
            input.setColumnName(output.getColumnName());
            input.setComment(output.getComment());
            input.setColType(output.getColType());
            columns.add(input);
        }
        return columns;
    }


    private void alterColumns(Map<String, Column> oldColumns, Map<String, Column> newColumns,
                              TableInfo entity) throws BusinessException {

        //是否有修改表字段的权限

        String tableName = entity.getName();
        String dbName = entity.getDbName();
        String catalogName = CatalogNameEnum.getCatalogNameByRegion(entity.getRegion());
        Map<String, Column> changeColumnMap = new HashMap<>();
        Map<String, String> renameColumnMap = new HashMap<>();
        List<String> dropColumnList = new ArrayList<>();
        boolean isDropColumn = false;
        boolean isChangeColumn = false;
        boolean isRenameColumn = false;
        for (Map.Entry<String, Column> oldColumnEntry : oldColumns.entrySet()) {
            String oldColumnId = oldColumnEntry.getKey();
            Column oldColumn = oldColumnEntry.getValue();
            if (!newColumns.containsKey(oldColumnId)) {
                isDropColumn = true;
                dropColumnList.add(oldColumn.getColumnName());
            } else {
                Column newColumn = newColumns.get(oldColumnId);
                if (!oldColumn.getColumnName().equals(newColumn.getColumnName())) {
                    isRenameColumn = true;
                    renameColumnMap.put(oldColumn.getColumnName(), newColumn.getColumnName());
                }

                if (!oldColumn.getColType().equals(newColumn.getColType()) || !oldColumn.getComment()
                        .equals(newColumn.getComment())) {
                    isChangeColumn = true;
                    changeColumnMap.put(newColumn.getColumnName(), newColumn);
                }
            }
        }

        try {
            if (isDropColumn) {
                ColumnChangeInput columnChangeInput = new ColumnChangeInput();
                columnChangeInput.setChangeType(Operation.DROP_COLUMN);
                columnChangeInput.setDropColumnList(dropColumnList);
                iLakeCatClientService.get().alterColumn(
                        new AlterColumnRequest(entity.getTenantName(), catalogName, dbName, tableName, columnChangeInput));
            }

            if (isRenameColumn) {
                ColumnChangeInput columnChangeInput = new ColumnChangeInput();
                columnChangeInput.setChangeType(Operation.RENAME_COLUMN);
                columnChangeInput.setRenameColumnMap(renameColumnMap);
                iLakeCatClientService.get().alterColumn(
                        new AlterColumnRequest(entity.getTenantName(), catalogName, dbName, tableName, columnChangeInput));
            }

            if (isChangeColumn) {
                ColumnChangeInput columnChangeInput = new ColumnChangeInput();
                columnChangeInput.setChangeType(Operation.CHANGE_COLUMN);
                columnChangeInput.setChangeColumnMap(changeColumnMap);
                iLakeCatClientService.get().alterColumn(
                        new AlterColumnRequest(entity.getTenantName(), catalogName, dbName, tableName, columnChangeInput));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage(), -1);
        }

    }

    private void alterColumnsByLakecat(AlterColumnVo alterColumnVo) throws BusinessException {
        String catalogName = CatalogNameEnum.getCatalogNameByRegion(alterColumnVo.getRegion());
        Map<String, Column> changeColumnMap = new HashMap<>();
        Map<String, String> renameColumnMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(alterColumnVo.getAlterList())) {
            for (ColumnVo columnVo : alterColumnVo.getAlterList()) {
                Column column = new Column();
                BeanUtils.copyProperties(columnVo, column);
                column.setColumnName(columnVo.getBeforeColumnName());
                changeColumnMap.put(columnVo.getBeforeColumnName(), column);
            }
        }
        if (CollectionUtils.isNotEmpty(alterColumnVo.getRenameList())) {
            for (ColumnVo columnVo : alterColumnVo.getRenameList()) {
                renameColumnMap.put(columnVo.getBeforeColumnName(), columnVo.getColumnName());
            }
        }
        if (changeColumnMap.size() > 0) {
            ColumnChangeInput columnChangeInput = new ColumnChangeInput();
            columnChangeInput.setChangeType(Operation.CHANGE_COLUMN);
            columnChangeInput.setChangeColumnMap(changeColumnMap);
            iLakeCatClientService.get().alterColumn(
                    new AlterColumnRequest(InfTraceContextHolder.get().getTenantName(), catalogName, alterColumnVo.getDbName(), alterColumnVo.getName(), columnChangeInput));
        }
        if (renameColumnMap.size() > 0) {
            ColumnChangeInput columnChangeInput = new ColumnChangeInput();
            columnChangeInput.setChangeType(Operation.RENAME_COLUMN);
            columnChangeInput.setRenameColumnMap(renameColumnMap);
            iLakeCatClientService.get().alterColumn(
                    new AlterColumnRequest(InfTraceContextHolder.get().getTenantName(), catalogName, alterColumnVo.getDbName(), alterColumnVo.getName(), columnChangeInput));
        }

    }

    private Map<String, Column> getColumnInputsMap(JSONArray jsonArray, String key, List<String> names) {
        Map<String, Column> columnInputs = new HashMap<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Column columnInput = new Column();
            String name = jsonObject.getString("name");
            columnInput.setColumnName(name);
            columnInput.setColType(jsonObject.getString("type"));
            columnInput.setComment(jsonObject.getString("comment"));
            columnInputs.put(jsonObject.getString(key), columnInput);
            names.add(name);
        }
        return columnInputs;
    }

    public static String transColumnOutPut(List<Column> columnOutput) {
        try {
            List<JSONObject> Column = new ArrayList<>();
            for (Column output : columnOutput) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", output.getColumnName());
                jsonObject.put("type", LakecatTableUtils.lmsDataTypeTohmsDataType(output.getColType()));
                jsonObject.put("comment", output.getComment());
                Column.add(jsonObject);
            }
            return Column.toString();
        } catch (Exception e) {

        }
        return "";
    }

    public static String transColumnOutPut(List<Column> columnOutput, List<String> partition) {
        try {
            List<JSONObject> Column = new ArrayList<>();
            for (Column output : columnOutput) {
                JSONObject jsonObject = new JSONObject();
                if (!partition.contains(output.getColumnName())) {
                    continue;
                }
                jsonObject.put("name", output.getColumnName());
                jsonObject.put("type", LakecatTableUtils.lmsDataTypeTohmsDataType(output.getColType()));
                jsonObject.put("comment", output.getComment());
                Column.add(jsonObject);
            }
            return Column.toString();
        } catch (Exception e) {

        }
        return "";
    }

    private Database[] getDBListBySDK(String PROJECT_ID, String region) {
        try {
            ListDatabasesRequest listDatabasesRequest = new ListDatabasesRequest();
            listDatabasesRequest.setProjectId(PROJECT_ID);
            if (InfTraceContextHolder.gcp()) {
                listDatabasesRequest.setCatalogName(region);
            } else {
                listDatabasesRequest.setCatalogName("shareit_" + region);
            }
            PagedList<Database> databasePagedList = iLakeCatClientService.get().listDatabases(listDatabasesRequest);
            return databasePagedList.getObjects();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Database getDBByName(String tenantName, String region, String dbName) {
        try {
            GetDatabaseRequest request = new GetDatabaseRequest();
            request.setProjectId(tenantName);
            if (InfTraceContextHolder.gcp()) {
                request.setCatalogName(region);
            } else {
                request.setCatalogName("shareit_" + region);

            }
            request.setDatabaseName(dbName);
            return iLakeCatClientService.get().getDatabase(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public JSONObject route(JSONObject args) {
        String owner = args.getString("owner");
        BillOwnerDepartment departmentByOwner = ibillService.getTableName(owner);
        if (departmentByOwner == null) {
            //记录下来这个用户没有默认分桶
            OwnerNotDefaultBucketRecord ownerForRecord = new OwnerNotDefaultBucketRecord();
            ownerForRecord.setOwner(owner);
            tableInfoMapper.insertForRecord(ownerForRecord);
            return null;
        }
        String department = departmentByOwner.getDepartment();

        String region = args.getString("region");
        String dbName = args.getString("dbName");
        String tableName = args.getString("tableName");
        DepartmentRegionRouteInfo bucketNameInfo = tableInfoMapper.getBucketName(department, region);
        if (bucketNameInfo == null || bucketNameInfo.getBucketName() == null) {
            //记录下来这个用户没有默认分桶
            OwnerNotDefaultBucketRecord ownerForRecord = new OwnerNotDefaultBucketRecord();
            ownerForRecord.setOwner(owner);
            ownerForRecord.setRegion(region);
            ownerForRecord.setDepartment(department);
            tableInfoMapper.insertForRecord(ownerForRecord);
            return null;
        }
        String bucketName = bucketNameInfo.getBucketName();
        String aliasName = bucketNameInfo.getAliasName();
        JSONObject result = new JSONObject();
        if (region.equals("ue1")) {
            String path = "s3://" + bucketName + "/" + aliasName + "/" + dbName + "/" + tableName;
            result.put("path", path);
        } else {
            String path = "obs://" + bucketName + "/" + aliasName + "/" + dbName + "/" + tableName;
            result.put("path", path);
        }
        return result;
    }

    @Override
    public String preciseSync(String userId, String region, String databaseName, String tableName, String tenantName) {

        LakeCatClient lakeCatClient = iLakeCatClientService.get();
        String catologName = region;//CatalogNameEnum.getCatalogName(region);
        GetTableRequest getTableRequest = new GetTableRequest(tenantName,
                catologName, databaseName, tableName);
        Table table;
        try {
            table = lakeCatClient.getTable(getTableRequest);
            upsertTable(table, region, tenantName);
            return BaseResponseCodeEnum.SUCCESS.getMessage();
        } catch (Exception e) {
            log.error("", e);
            return e.getMessage();
        }
    }

    @Override
    public void addCloumnLevel(AddColumnVo addColumnVo) {
        if (CollectionUtils.isNotEmpty(addColumnVo.getColsGrade())) {
            addColumnVo.getColsGrade().forEach(c -> {
                if (StringUtils.isBlank(c.getName())) {
                    c.setMaintainer(addColumnVo.getName());
                }
            });
            preciseSync("", addColumnVo.getCatalog(), addColumnVo.getDbName(), addColumnVo.getName(), defaultTenantName);
            TableInfo tableInfo = new TableInfo();
            tableInfo.setRegion(addColumnVo.getCatalog());
            tableInfo.setDbName(addColumnVo.getDbName());
            tableInfo.setName(addColumnVo.getName());
//            List<TableInfo> tableIdByNames = tableInfoMapper.getTableIdByName(tableInfo);
//            if (CollectionUtils.isNotEmpty(tableIdByNames)) {
            BatchDataGradeReq batchDataGradeReq = new BatchDataGradeReq();
            batchDataGradeReq.setRegion(addColumnVo.getCatalog());
            batchDataGradeReq.setDbName(addColumnVo.getDbName());
            batchDataGradeReq.setTableName(addColumnVo.getName());
            batchDataGradeReq.setColsGrade(addColumnVo.getColsGrade());
//                try {
            dataGradeService.batchAddGrades(batchDataGradeReq);
//                } catch (BusinessException e) {
//                    log.error("", e);
//                }
//            }
        }


    }

    public boolean existTable(String region, String databaseName, String tableName) {
        LakeCatClient lakeCatClient = iLakeCatClientService.get();
        String catologName = CatalogNameEnum.getCatalogName(region);
        GetTableRequest getTableRequest = new GetTableRequest(InfTraceContextHolder.get().getTenantName(),
                catologName, databaseName, tableName);
        Table table;
        try {
            table = lakeCatClient.getTable(getTableRequest);
            if (table != null) {
                return true;
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return false;
    }

    @Override
    public List<TableUsageProfileGroupByUser> tableProfileInfo(TableInfoReq tableInfoReq, Integer recentlyDays) {
        if (recentlyDays == null) {
            recentlyDays = 30;
        }
        TableProfileInfoReq tableProfileInfoReq = new TableProfileInfoReq();
        BeanUtils.copyProperties(tableInfoReq, tableProfileInfoReq);
        DateTime currentDateTime = new DateTime();
        tableProfileInfoReq.setEndTimestamp(currentDateTime.getMillis());
        tableProfileInfoReq.setStartTimestamp(currentDateTime.minusDays(recentlyDays).getMillis());
        String tableName = tableInfoReq.getTableName();
        if (tableName != null && !"".equals(tableName)) {
            tableProfileInfoReq.setTableName(tableName);
        }
        List<TableUsageProfileGroupByUser> usageProfileGroupByUser = tableProfileService.getUsageProfileGroupByUser(
                tableProfileInfoReq, null);
        if (CollectionUtils.isNotEmpty(usageProfileGroupByUser)) {
            return usageProfileGroupByUser.stream().map(x -> {
                x.setAvgCount(x.getSumCount().divide(
                        BigInteger.valueOf(30)));
                return x;
            }).collect(toList());
        }
        return null;
    }

    @Override
    public TableStorageInfo tableStorageInfo(TableInfoReq tableInfoReq, Long id) throws BusinessException {
        LakeCatParam lakeCatParam = LakeCatParam.builder().region(tableInfoReq.getRegion())
                .dbName(tableInfoReq.getDbName()).tableName(tableInfoReq.getTableName()).build();

        TableInfo tableDetail = iLakeCatClientService.getTable(lakeCatParam);
//        TableInfo tableDetail = getIfNotPresent(id, tableInfoReq);
        List<TableStorageInfo> tableStorageInfos = tableStorageInfoMapper.getLatestRecord(tableDetail.getRegion(),
                tableDetail.getDbName(), tableDetail.getName());
        if (CollectionUtils.isNotEmpty(tableStorageInfos)) {
            TableStorageInfo storageInfo = tableStorageInfos.get(0);
            if (storageInfo.getTotalStorage() != null) {
                int length = storageInfo.getTotalStorage().length();
                double totalStorage = Double.parseDouble(storageInfo.getTotalStorage());
                String value = "";
                if (length > 0 & length <= 3) {
                    value = divideStorageUnit(totalStorage, 0) + "Byte";
                } else if (length > 3 & length <= 6) {
                    value = divideStorageUnit(totalStorage, 1) + "KB";
                } else if (length > 6 & length <= 9) {
                    value = divideStorageUnit(totalStorage, 2) + "MB";
                } else if (length > 9 & length <= 12) {
                    value = divideStorageUnit(totalStorage, 3) + "GB";
                } else if (length > 12) {
                    value = divideStorageUnit(totalStorage, 4) + "TB";
                }
                //转换单位为 G
                storageInfo.setTotalStorage(value);
                setStorageTypeTags(storageInfo);
            }
            return storageInfo;
        } else {
            TableStorageInfo storageInfo = new TableStorageInfo();
            storageInfo.setLocation(tableDetail.getLocation());
            storageInfo.setStorageFileFormat(tableDetail.getSdFileFormat());
            return storageInfo;
        }
    }

    private void setStorageTypeTags(TableStorageInfo storageInfo) {
        List<String> tags = new ArrayList<>();
        addStorageTypeTags(storageInfo.getTableStandardSize(), tags, CloudStorageType.STANDARD);
        addStorageTypeTags(storageInfo.getTableDeepSize(), tags, CloudStorageType.DEEP);
        addStorageTypeTags(storageInfo.getTableIntelligentSize(), tags, CloudStorageType.INTELLIGENT);
        addStorageTypeTags(storageInfo.getTableArchiveSize(), tags, CloudStorageType.ARCHIVE);
        storageInfo.setTags(tags);
    }

    private void addStorageTypeTags(String size, List<String> tags, CloudStorageType cloudStorageType) {
        if (StringUtils.isNotEmpty(size) && Double.parseDouble(size) > 0) {
            tags.add(cloudStorageType.desc);
        }
    }

    private double divideStorageUnit(double totalStorage, int power) {
        return MathUtils.formatDouble(totalStorage / Math.pow(1024, power), 2);
    }

    @Override
    public TableOutputInfo tableOutputInfo(TableInfoReq tableInfoReq) {
        TableOutputInfo tableOutputInfo = new TableOutputInfo();

        String databaseName = tableInfoReq.getDatabaseName();
        String region = tableInfoReq.getRegion();
        String tableName = tableInfoReq.getTableName();
        long l = System.currentTimeMillis();
        System.out.println("开始执行查询~");
        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        String tenantName = userInfo.getTenantName();
        JSONObject oneTableName = dsUtilForLakecat.getOneTableName(tableName, databaseName, region, tenantName);
        if (oneTableName == null) {
            return null;
        }
        long s = System.currentTimeMillis();
        System.out.println(s - l);
        String id = oneTableName.getString("id");
        String name = oneTableName.getString("name");
        if (StringUtils.isBlank(name)) {
            return null;
        }
        tableOutputInfo.setTaskId(id);
        tableOutputInfo.setTaskName(name);
        GovJobInfo jobIdByName = igovService.getJobIdByName(name);
        if (jobIdByName != null) {
            tableOutputInfo.setGovJobId(jobIdByName.getId());
        }
        List<GovTagEntity> tagEntities = govTagService.getJobTags(name);
        List<String> tags = tagEntities.stream().map(GovTagEntity::getTagValue).collect(toList());
        if (tags.contains("用量小")) {
            tableOutputInfo.setFlag(true);
        }
        tableOutputInfo.setComputingGovTags(tags);
        return tableOutputInfo;
    }


    @Override
    public List<TablePrivilegeInfo> tablePrivilegeInfo(TableInfoReq tableInfoReq) throws BusinessException {
        List<TablePrivilegeInfo> list = new ArrayList<>();
        String qualifiedName = getQualifiedName(tableInfoReq);

        List<UserGroupVo> userGroupVos = tableInfoMapper.selectAllUserGroupName();
        Map<String, List<UserGroupVo>> collect = userGroupVos.stream().collect(Collectors.groupingBy(UserGroupVo::getUuid));

        if (!SqlUtils.containsSqlInjection(qualifiedName)) {
            String a = getQualifiedName(tableInfoReq);
            List<PermissionRecordInfo> userByTableInfo = permissionRecordMapper.findUserByTableInfo(
                    a);
            TablePrivilegeInfo info;
            for (PermissionRecordInfo pri : userByTableInfo) {
                info = new TablePrivilegeInfo();
                info.setRequestTime(pri.getUpdateTime().split("\\.")[0]);
                info.setProposer(pri.getProposer());
                info.setCertigier(pri.getCertigier());
                String tableRecoveryState = pri.getTableRecoveryState();
                if (StringUtils.isNotEmpty(tableRecoveryState)) {
                    String[] split = tableRecoveryState.split(",");
                    for (String table : split) {
                        String[] split1 = table.split(":");
                        if (split1[0].equals(qualifiedName)) {
                            info.setRecoveryState(Integer.parseInt(split1[1]));
                        }
                    }
                }
                List<String> permissionList = Arrays.stream(pri.getPermission().split(CommonConts.LIST_DATA_INLINE_DELIMITER)).map(data -> {
                    return CommonParameters.operationConvert.get(data);
                }).collect(toList());
                info.setPrivilegeList(permissionList);

                info.setReason(pri.getReason());
                info.setPermissionTableId(pri.getId());
                if (StringUtils.isNotEmpty(pri.getCycle())) {
                    info.setCycle(CommonParameters.intcycleMap.get(Integer.parseInt(pri.getCycle())));
                }

                boolean flag = true;
                if (StringUtils.isNotEmpty(pri.getGrantUser())) {
                    String grantUserStr = pri.getGrantUser();
                    String[] grantUsers = grantUserStr.split(CommonConts.LIST_DATA_INLINE_DELIMITER);
                    for (String userId : grantUsers) {
                        TablePrivilegeInfo info1 = new TablePrivilegeInfo();
                        BeanUtils.copyProperties(info, info1);
                        info1.setUserId(userId.trim());
                        List<UserGroupVo> userGroupVos1 = collect.get(userId.trim());
                        if (userGroupVos1 != null && !userGroupVos1.isEmpty()) {
                            info1.setUserGroup(userGroupVos1.stream().findFirst().get().getName());
                        }
                        list.add(info1);
                        flag = false;
                    }
                }
                if (flag && StringUtils.isNotEmpty(pri.getApplyUser())) {
                    List<UserGroupVo> userGroupVos1 = collect.get(pri.getApplyUser());
                    if (userGroupVos1 != null && !userGroupVos1.isEmpty()) {
                        info.setUserGroup(userGroupVos1.stream().findFirst().get().getName());
                    }
                    info.setUserId(pri.getApplyUser());
                    list.add(info);
                }

            }
        }
        return list;
    }


    @Override
    public PageInfo<TablePrivilegeInfo> tablePrivilegeInfoPages(TableInfoReq tableInfoReq) throws BusinessException {
        List<TablePrivilegeInfo> tablePrivilegeInfos = tablePrivilegeInfo(tableInfoReq);

        List<TablePrivilegeInfo> collect = tablePrivilegeInfos.stream().filter(filter -> {
            boolean usergroup = true;
            boolean type = true;
            if (StringUtils.isNotEmpty(tableInfoReq.getUserGroup())) {
                usergroup = false;
                if (StringUtils.isNotEmpty(filter.getUserGroup())) {
                    usergroup = filter.getUserGroup().contains(tableInfoReq.getUserGroup());
                }
            }
            if (tableInfoReq.getTypeList() != null && !tableInfoReq.getTypeList().isEmpty()) {
                for (String ty : tableInfoReq.getTypeList()) {
                    if (filter.getPrivilegeList().contains(ty)) {
                        type = true;
                        break;
                    }
                    type = false;
                }

            }
            return usergroup && type;
        }).collect(toList());

        List<TablePrivilegeInfo> result = PageUtil.startPage(collect, tableInfoReq.getPageNum(), tableInfoReq.getPageSize());
        PageInfo<TablePrivilegeInfo> tablePrivilegeInfoPageInfo = new PageInfo<>(result);
        tablePrivilegeInfoPageInfo.setPageNum(tableInfoReq.getPageNum());
        tablePrivilegeInfoPageInfo.setPageSize(tableInfoReq.getPageSize());
        tablePrivilegeInfoPageInfo.setTotal(collect.size());
        return tablePrivilegeInfoPageInfo;
    }

    @Override
    public TableSummaryDescInfo tableSummaryDescInfo(TableInfoReq tableInfoReq, Long id) throws BusinessException {
        LakeCatParam lakeCatParam = LakeCatParam.builder().region(tableInfoReq.getRegion())
                .dbName(tableInfoReq.getDbName()).tableName(tableInfoReq.getTableName()).build();

        TableInfo tableDetail = iLakeCatClientService.getTable(lakeCatParam);

        checkTable(tableDetail);
        TableSummaryDescInfo descInfo = new TableSummaryDescInfo();
        BeanUtils.copyProperties(tableDetail, descInfo);
        if (StringUtils.isNotEmpty(tableDetail.getCreateTime()) && tableDetail.getCreateTime().length() >= 10) {
            Period period = new Period(new DateTime(tableDetail.getCreateTime().substring(0, 10)), new DateTime(), PeriodType.days());
            descInfo.setTableAge(period.getDays());
        }
        try {
            String catologName = CatalogNameEnum.getCatalogNameByRegion(tableDetail.getRegion());
            BigInteger usageProfilesByTable = tableProfileService.getUsageProfilesByTable(catologName,
                    tableDetail.getDbName(), tableDetail.getName(), tableInfoReq.getTenantName());

            descInfo.setRecentlyVisitedTimes(usageProfilesByTable);
            descInfo.setLastActivityCount(usageProfilesByTable.longValue());
        } catch (Exception e) {
            descInfo.setLastActivityCount(0L);
            log.warn("Get profile exception: {}.{}, error msg: {}", tableDetail.getDbName(), tableDetail.getName(), e.getMessage());
        }
        return descInfo;
    }

    private void checkTable(TableInfo tableDetail) throws BusinessException {
        if (tableDetail == null) {
            throw new BusinessException("没有找到表，请检查参数是否有误", -1);
        }
    }

    @Override
    public boolean setCollect(JSONObject entity) {

        CollectInfo collect = new CollectInfo();
        collect.setUserId(entity.getString("userId"));
        collect.setTableId(entity.getLong("id"));
        Boolean isCollect = entity.getBoolean("isCollect");
        if (isCollect) {
            collect.setSole(entity.getString("region") + "." + entity.getString("dbName") + "." + entity.getString("tableName"));
            collect.setStatus(1);
        } else {
            collect.setSole(entity.getString("tableName"));
            collect.setStatus(0);
        }
        List<CollectInfo> search = collectMapper.searchNew(collect);
        if (search.size() > 0) {
            collectMapper.updateByNameNew(collect);
        } else {
            collectMapper.insertForCollect(collect);
        }
        return true;

    }

    private void upsertTable(Table table, String region, String tenantName) {
        if (table != null) {
            List<TableInfo> tableListForSave = new ArrayList<>();
            TableInfo tableInfo = new TableInfo();
            tableInfo.setDbName(table.getDatabaseName());
            tableInfo.setName(table.getTableName());
            tableInfo.setOwner(table.getOwner());
            tableInfo.setColumns(transColumnOutPut(table.getFields()));
            List<Column> partitions = table.getPartitionKeys();
            tableInfo.setPartitionKeys(transColumnOutPut(partitions));
            if (!partitions.isEmpty()) {
                tableInfo.setPartitionType(1);
            }
            Map<String, String> properties = table.getParameters();
            String transientLastDdlTime = properties.get("transient_lastDdlTime");
            if (transientLastDdlTime != null) {
                tableInfo.setTransientLastDdlTime(DateUtil.getDateToString(Long.parseLong(transientLastDdlTime) * 1000));
            }
            String towner = properties.get("owner");
            if (towner != null) {
                String[] split = towner.split("#");
                if (split.length == 2) {
                    tableInfo.setOwner(split[1]);
                } else {
                    tableInfo.setOwner(split[0]);
                }
            }
            String partitionSpec = properties.get("default-partition-spec");
            if (partitionSpec != null) {
                JSONObject partition = JSON.parseObject(partitionSpec);
                JSONArray fields = partition.getJSONArray("fields");
                List<String> columnPartitions = new ArrayList<>();
                for (int i = 0; i < fields.size(); i++) {
                    JSONObject column = fields.getJSONObject(i);
                    columnPartitions.add(column.getString("name"));
                }
                if (!columnPartitions.isEmpty()) {
                    tableInfo.setPartitionType(1);
                    tableInfo.setPartitionKeys(transColumnOutPut(table.getFields(), columnPartitions));
                }
            }
            tableInfo.setDescription(table.getParameters().getOrDefault("comment", ""));
            tableInfo.setCreateTime(DateUtil.getDateToString(table.getCreateTime()));
            StorageDescriptor storageDescriptor = table.getStorageDescriptor();
            String location = storageDescriptor.getLocation();
            tableInfo.setLocation(location);
            tableInfo.setRegion(region);
            tableInfo.setType(LakecatTableUtils.getTableType(table));
            tableInfo.setSdFileFormat(storageDescriptor.getFileFormat());
            Long count = 0L;//给大整数赋初值为0
            tableInfo.setLastActivityCount(count);
            List<TableInfo> tableInfoList = tableInfoMapper.selectTables(tableInfo.getRegion(), tableInfo.getDbName(), tableInfo.getName());
            if (CollectionUtils.isEmpty(tableInfoList)) {
                tableListForSave.add(tableInfo);
                tableInfoMapper.batchSave(tableListForSave);
            }
        }
    }

    private TableInfo getIfNotPresent(Long id, TableInfoReq tableInfoReq) {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setRegion(tableInfoReq.getRegion());
        tableInfo.setDbName(tableInfoReq.getDatabaseName());
        tableInfo.setName(tableInfoReq.getTableName());
        return getIfNotPresent(id, tableInfo);
    }

    public TableInfo getIfNotPresent(Long id, TableInfo tableDetail) {
        return getIfNotPresent(id, tableDetail, false);
    }


    public TableInfo getIfNotPresent(Long id, TableInfo tableDetail, boolean reload) {
        if (openCache) {
            if (id != null) {
                if (!tableInfoCacheMap.containsKey(id.toString()) || reload) {
                    tableInfoCacheMap.put(id.toString(), tableInfoMapper.getTableDetail(id));
                }
                return tableInfoCacheMap.get(id.toString());
            }
            if (tableDetail != null) {
                String qualifiedName = getQualifiedName(tableDetail);
                if (tableInfoCacheMap.containsKey(qualifiedName) && !reload) {
                    return tableInfoCacheMap.get(qualifiedName);
                }
                return getTableDetail(tableDetail.getRegion(), tableDetail.getDbName(), tableDetail.getName());
            }
        } else {
            if (id != null) {
                return tableInfoMapper.getTableDetail(id);
            }
            if (tableDetail != null) {
                return getTableDetail(tableDetail.getRegion(), tableDetail.getDbName(), tableDetail.getName());
            }
        }
        return null;
    }

    private TableInfo getTableDetail(String region, String databaseName, String tableName) {
        Map<String, Object> map = new HashMap<>();
        map.put("region", region);
        map.put("db_name", databaseName);
        map.put("name", tableName);
        List<TableInfo> tableInfos = tableInfoMapper.selectByMap(map);
        if (CollectionUtils.isNotEmpty(tableInfos)) {
            TableInfo tableInfo = tableInfos.get(0);
            cacheTableInfo(tableInfo.getId(), tableInfo);
            return tableInfo;
        }
        return null;
    }

    private void cacheTableInfo(Long id, TableInfo tableDetail) {
        if (openCache) {
            tableInfoCacheMap.put(id.toString(), tableDetail);
            tableInfoCacheMap.put(getQualifiedName(tableDetail), tableDetail);
        }
    }

    private void cacheTableInfo(String key, TableInfo tableDetail) {
        if (openCache) {
            tableInfoCacheMap.put(key, tableDetail);
            tableInfoCacheMap.put(getQualifiedName(tableDetail), tableDetail);
        }
    }

    private String getQualifiedName(TableInfo tableDetail) {
        return getQualifiedName(tableDetail.getRegion(), tableDetail.getDbName(), tableDetail.getName());
    }

    private String getQualifiedName(TableInfoReq tableInfoReq) {
        return getQualifiedName(tableInfoReq.getRegion(), tableInfoReq.getDatabaseName(), tableInfoReq.getTableName());
    }

    private String getQualifiedName(String region, String dbName, String tableName) {
        try {
            return String.format("%s.%s.%s", CatalogNameEnum.getCatalogName(region), dbName, tableName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format("%s.%s.%s", region, dbName, tableName);
    }


//    public static void main(String[] args) {
//        RoleInputs roleInputs=new RoleInputs();
//        roleInputs.setRoleName("groupwx7l4jo5");
//        roleInputs.setObjectNames(new String[]{"ksyun_cn-beijing-6"+"."+"stage"});
//        roleInputs.setOperation(new String[]{"创建表","删除库","描述库","修改库"});
//        System.out.println(GsonUtil.toJson(roleInputs,false));
//    }
}


