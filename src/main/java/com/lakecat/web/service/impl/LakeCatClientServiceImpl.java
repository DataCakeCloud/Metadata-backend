package com.lakecat.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lakecat.web.entity.*;
import com.lakecat.web.mapper.DictMapper;
import com.lakecat.web.mapper.TableInfoMapper;
import com.lakecat.web.utils.GsonUtil;
import com.lakecat.web.utils.PageUtil;
import io.lakecat.catalog.client.CatalogUserInformation;
import io.lakecat.catalog.client.LakeCatClient;

import com.lakecat.web.config.GlobalConfig;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.service.ILakeCatClientService;
import io.lakecat.catalog.common.LakeCatConf;
import io.lakecat.catalog.common.ObjectType;
import io.lakecat.catalog.common.Operation;
import io.lakecat.catalog.common.lineage.*;
import io.lakecat.catalog.common.model.*;
import com.lakecat.web.utils.DateUtil;
import io.lakecat.catalog.common.model.discovery.*;
import io.lakecat.catalog.common.model.glossary.Category;
import io.lakecat.catalog.common.model.glossary.Glossary;
import io.lakecat.catalog.common.plugin.request.*;
import io.lakecat.catalog.common.plugin.request.input.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import io.lakecat.catalog.common.plugin.request.GetTableRequest;
import io.lakecat.catalog.common.plugin.request.SearchDiscoveryNamesRequest;
import io.lakecat.catalog.common.plugin.request.TableSearchRequest;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class LakeCatClientServiceImpl implements ILakeCatClientService {

    @Autowired
    GlobalConfig globalConfig;

    @Autowired
    CatalogNameEnum CatalogNameEnum;

    @Autowired
    public DictMapper dictMapper;

    @Autowired
    private TableInfoMapper tableInfoMapper;

    public static final String MODEL = "MODEL";

    public static final String TAG = "TAG";

    public static final String SECURITY_LEVEL = "SECURITY_LEVEL";

    public static final String LIFECYCLE = "LIFECYCLE";

    public static final String USE_DIRECTION = "USE_DIRECTION";

    public static final String CREATE_BY = "CREATE_BY";

    public static Map<String, Operation> operation = new HashMap<>();

    static {
        operation.put("查询", Operation.SELECT_TABLE);
        operation.put("编辑", Operation.ALTER_TABLE);
        operation.put("描述", Operation.DESC_TABLE);
        operation.put("插入", Operation.INSERT_TABLE);
        operation.put("删除", Operation.DROP_TABLE);
    }

    @Override
    public LakeCatClient get() {
        try {
            return LakeCatClient.getInstance(getConfiguration(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Configuration getConfiguration(String region) {
        Configuration conf = new Configuration();
        conf.set(LakeCatConf.CATALOG_HOST, globalConfig.getLsUrl(region));
        conf.setInt(LakeCatConf.CATALOG_PORT, globalConfig.getLsPort(region));
        conf.set(CatalogUserInformation.LAKECAT_USER_NAME, globalConfig.getLsUserName(region));
        conf.set(CatalogUserInformation.LAKECAT_USER_PASSWORD, globalConfig.getLsrPassword(region));
        return conf;
    }

    @Override
    public Configuration getConfiguration() {
        Configuration conf = new Configuration();
        conf.set(LakeCatConf.CATALOG_HOST, globalConfig.getLsUrl());
        conf.setInt(LakeCatConf.CATALOG_PORT, globalConfig.getLsPort());
        conf.set(CatalogUserInformation.LAKECAT_USER_NAME, globalConfig.getLsUserName());
        conf.set(CatalogUserInformation.LAKECAT_USER_PASSWORD, globalConfig.getLsrPassword());
        return conf;
    }

    @Override
    public TableInfo getTable(LakeCatParam lakeCatParam) {
        LakeCatClient lakeCatClient = get();
        //String projectId, String keywords, String catalogName
        String tenantName = InfTraceContextHolder.get().getTenantName();
        String catologName = CatalogNameEnum.getCatalogNameByRegion(lakeCatParam.getRegion());
        GetTableRequest getTableRequest = new GetTableRequest(tenantName,
                catologName, lakeCatParam.getDbName(), lakeCatParam.getTableName());
        Table table = lakeCatClient.getTable(getTableRequest);
        return tableConvertTableInfo(table, lakeCatParam);
    }

    @Override
    public TableInfo updateTable(TableInfo tableInfo) {

        //原表
        LakeCatClient lakeCatClient = get();
        String tenantName = InfTraceContextHolder.get().getTenantName();
        GetTableRequest getTableRequest = new GetTableRequest(tenantName,
                CatalogNameEnum.getCatalogNameByRegion(tableInfo.getRegion()), tableInfo.getDbName(), tableInfo.getTableName());
        Table table = lakeCatClient.getTable(getTableRequest);

        AlterTableRequest request = new AlterTableRequest();
        request.setCatalogName(CatalogNameEnum.getCatalogNameByRegion(tableInfo.getRegion()));
        request.setDatabaseName(tableInfo.getDbName());
        request.setTableName(tableInfo.getTableName());
        request.setProjectId(tenantName);

        AlterTableInput alterTableInput = new AlterTableInput();
        TableInput tableInput = new TableInput();

        //表的级别 以及使用说明
        if (StringUtils.isNotEmpty(tableInfo.getSecurityLevel())) {
            table.getParameters().put(SECURITY_LEVEL, tableInfo.getSecurityLevel());
        }
        if (tableInfo.getLifecycle() > 0) {
            table.getParameters().put(LIFECYCLE, tableInfo.getLifecycle().toString());
        }
        if (StringUtils.isNotEmpty(tableInfo.getUseDirection())) {
            table.getParameters().put(USE_DIRECTION, tableInfo.getUseDirection());
        }
        if (StringUtils.isNotEmpty(tableInfo.getCreateBy())) {
            table.getParameters().put(CREATE_BY, tableInfo.getCreateBy());
        }
        tableInput.setParameters(table.getParameters());

        //表的描述
        if (StringUtils.isNotEmpty(tableInfo.getDescription())) {
            tableInput.setDescription(tableInfo.getDescription());
        } else {
            tableInput.setDescription(table.getDescription());
        }

        //业务组  九号是用owner
        if (StringUtils.isNotEmpty(tableInfo.getOwner())) {
            tableInput.setOwner(tableInfo.getOwner());
        } else {
            tableInput.setOwner(table.getOwner());
        }

        //标签和模型这2不用改表直接可结束  但都得知道前面和后面的  都是新添加再删除
        //标签 添加
        if (StringUtils.isNotEmpty(tableInfo.getTargetTagName())) {
            if (tableInfo.getTargetTagCategoryId() != null && tableInfo.getTargetTagCategoryId() > 0) {
//                TableInfo addReationShip = new TableInfo();
//                addReationShip.setCategoryId(tableInfo.getTargetTagCategoryId());
//                addReationShip.setRegion(tableInfo.getRegion());
//                addReationShip.setDbName(tableInfo.getDbName());
//                addReationShip.setTableName(tableInfo.getTableName());
//                addModelTableRelationship(addReationShip);
                Model model = new Model();
                model.setId(tableInfo.getTargetTagCategoryId()).setName(tableInfo.getTargetTagName());
                //这里只修改categroy
                updateCategory(model);
            } else {
                //创建category 标签
                Model model = new Model();
                Glossary glossary = getCacheGlossary(TAG);
                if (glossary == null) {
                    model.setName(TAG);
                    creteGlossary(model);
                    glossary = getCacheGlossary(TAG);
                }
                model.setGlossaryId(glossary.getId());
                model.setName(tableInfo.getTargetTagName());
                Category category = createCategory(model);
                TableInfo addReationShip = new TableInfo();
                addReationShip.setCategoryId(category.getId());
                addReationShip.setRegion(tableInfo.getRegion());
                addReationShip.setDbName(tableInfo.getDbName());
                addReationShip.setTableName(tableInfo.getTableName());
                addModelTableRelationship(addReationShip);
            }

        }

        //创建表的标签添加
        if (tableInfo.getListTag() != null && !tableInfo.getListTag().isEmpty()) {
            List<Category> listTag = tableInfo.getListTag();
            for (Category category : listTag) {
                //创建category 标签
                Model model = new Model();
                Glossary glossary = getCacheGlossary(TAG);
                model.setGlossaryId(glossary.getId());
                model.setName(category.getName());
                Category categoryRes = createCategory(model);
                TableInfo addReationShip = new TableInfo();
                addReationShip.setCategoryId(categoryRes.getId());
                addReationShip.setRegion(tableInfo.getRegion());
                addReationShip.setDbName(tableInfo.getDbName());
                addReationShip.setTableName(tableInfo.getTableName());
                addModelTableRelationship(addReationShip);
            }
        }

        //标签 删除
        if (tableInfo.getSourceTagCategoryId() != null && tableInfo.getSourceTagCategoryId() > 0) {
            TableInfo deleteReationShip = new TableInfo();
            deleteReationShip.setCategoryId(tableInfo.getSourceTagCategoryId());
            deleteReationShip.setRegion(tableInfo.getRegion());
            deleteReationShip.setDbName(tableInfo.getDbName());
            deleteReationShip.setTableName(tableInfo.getTableName());
            deleteModelTableRelationship(deleteReationShip);
        }

        //模型  删除
        if (tableInfo.getSourceModelCategoryId() != null && tableInfo.getSourceModelCategoryId() > 0) {
            TableInfo deleteReationShip = new TableInfo();
            deleteReationShip.setCategoryId(tableInfo.getSourceModelCategoryId());
            deleteReationShip.setRegion(tableInfo.getRegion());
            deleteReationShip.setDbName(tableInfo.getDbName());
            deleteReationShip.setTableName(tableInfo.getTableName());
            deleteModelTableRelationship(deleteReationShip);
        }

        //模型  添加
        if (tableInfo.getTargetModelCategoryId() != null && tableInfo.getTargetModelCategoryId() > 0) {
            TableInfo addReationShip = new TableInfo();
            addReationShip.setCategoryId(tableInfo.getTargetModelCategoryId());
            addReationShip.setRegion(tableInfo.getRegion());
            addReationShip.setDbName(tableInfo.getDbName());
            addReationShip.setTableName(tableInfo.getTableName());
            addModelTableRelationship(addReationShip);
        }

        //private String catalogName;
        tableInput.setCatalogName(CatalogNameEnum.getCatalogNameByRegion(tableInfo.getRegion()));
        tableInput.setDatabaseName(table.getDatabaseName());
        tableInput.setTableName(table.getTableName());
        tableInput.setAuthSourceType(table.getAuthSourceType());
        tableInput.setAccountId(table.getAccountId());
        tableInput.setOwnerType(table.getOwnerType());
        tableInput.setCreateTime(table.getCreateTime());
        tableInput.setLastAccessTime(table.getLastAccessTime());
        tableInput.setRetention(table.getRetention().intValue());
        tableInput.setPartitionKeys(table.getPartitionKeys());
        tableInput.setTableType(table.getTableType());
        tableInput.setStorageDescriptor(table.getStorageDescriptor());
        tableInput.setLmsMvcc(table.isLmsMvcc());
        tableInput.setViewExpandedText(table.getViewExpandedText());
        tableInput.setViewOriginalText(table.getViewOriginalText());

        alterTableInput.setTable(tableInput);
        request.setInput(alterTableInput);
        get().alterTable(request);
        return null;
    }


    /**
     * 获取模型与表的关系
     */
    public TableCategories getTableRelationship(TableInfo tableInfo) {
        LakeCatClient lakeCatClient = get();
        String tenantName = InfTraceContextHolder.get().getTenantName();
        GetTableCategoriesRequest getTableCategoriesRequest = new GetTableCategoriesRequest();
        getTableCategoriesRequest.setProjectId(tenantName);
        String qualifiedName = CatalogNameEnum.getCatalogNameByRegion(tableInfo.getRegion()) + "." + tableInfo.getDbName() + "." + tableInfo.getTableName();
        getTableCategoriesRequest.setQualifiedName(qualifiedName);
        return lakeCatClient.getTableCategories(getTableCategoriesRequest);
    }


    /**
     * 删除模型与表的关系
     *
     * @param tableInfo
     */
    public void deleteModelTableRelationship(TableInfo tableInfo) {
        LakeCatClient lakeCatClient = get();
        String tenantName = InfTraceContextHolder.get().getTenantName();
        RemoveCategoryRelationRequest removeCategoryRelationRequest = new RemoveCategoryRelationRequest();
        removeCategoryRelationRequest.setProjectId(tenantName);
        removeCategoryRelationRequest.setCategoryId(tableInfo.getCategoryId());
        String qualifiedName = CatalogNameEnum.getCatalogNameByRegion(tableInfo.getRegion()) + "." + tableInfo.getDbName() + "." + tableInfo.getTableName();
        removeCategoryRelationRequest.setQualifiedName(qualifiedName);
        lakeCatClient.removeCategoryRelation(removeCategoryRelationRequest);
    }

    /**
     * 添加模型与表关系
     */
    public void addModelTableRelationship(TableInfo tableInfo) {
        LakeCatClient lakeCatClient = get();
        String tenantName = InfTraceContextHolder.get().getTenantName();
        AddCategoryRelationRequest addCategoryRelationRequest = new AddCategoryRelationRequest();
        addCategoryRelationRequest.setProjectId(tenantName);
        addCategoryRelationRequest.setCategoryId(tableInfo.getCategoryId());
        String qualifiedName = CatalogNameEnum.getCatalogNameByRegion(tableInfo.getRegion()) + "." + tableInfo.getDbName() + "." + tableInfo.getTableName();
        addCategoryRelationRequest.setQualifiedName(qualifiedName);
        lakeCatClient.addCategoryRelation(addCategoryRelationRequest);
    }


    /**
     * 获取最新分区
     */
    @Cacheable(cacheNames = {"tablePartitionCorrelation"}, key = "'getLatestPartitionName-'+#sole")
    public String getLatestPartitionName(TableInfo tableInfo, String sole) {
        LakeCatClient lakeCatClient = get();
        String tenantName = InfTraceContextHolder.get().getTenantName();
        GetLatestPartitionNameRequest getLatestPartitionNameRequest = new GetLatestPartitionNameRequest();
        getLatestPartitionNameRequest.setProjectId(tenantName);
        getLatestPartitionNameRequest.setCatalogName(CatalogNameEnum.getCatalogNameByRegion(tableInfo.getRegion()));
        getLatestPartitionNameRequest.setDatabaseName(tableInfo.getDbName());
        getLatestPartitionNameRequest.setTableName(tableInfo.getTableName());
        return lakeCatClient.getLatestPartitionName(getLatestPartitionNameRequest);
    }

    @CacheEvict(cacheNames = {"tablePartitionCorrelation"}, key = "'getLatestPartitionName-'+#sole")
    public Boolean clearCachePartitionName(TableInfo tableInfo, String sole) {
        log.info("success cache key :tablePartitionCorrelation-" + sole);
        return true;
    }


    /**
     * 获取分区数
     */
    @Cacheable(cacheNames = {"tablePartitionCorrelation"}, key = "'getPartitionCount-'+#sole")
    public Integer getPartitionCount(TableInfo tableInfo, String sole) {
        LakeCatClient lakeCatClient = get();
        String tenantName = InfTraceContextHolder.get().getTenantName();
        GetPartitionCountRequest getPartitionCountRequest = new GetPartitionCountRequest();
        getPartitionCountRequest.setProjectId(tenantName);
        getPartitionCountRequest.setCatalogName(CatalogNameEnum.getCatalogNameByRegion(tableInfo.getRegion()));
        getPartitionCountRequest.setDatabaseName(tableInfo.getDbName());
        getPartitionCountRequest.setTableName(tableInfo.getTableName());
        PartitionFilterInput filterInput = new PartitionFilterInput();
        getPartitionCountRequest.setInput(filterInput);
        return lakeCatClient.getPartitionCount(getPartitionCountRequest);
    }

    @CacheEvict(cacheNames = {"tablePartitionCorrelation"}, key = "'getPartitionCount-'+#sole")
    public Boolean clearCachePartitionCount(TableInfo tableInfo, String sole) {
        log.info("success cache key :tablePartitionCorrelation-" + sole);
        return true;
    }

    public TableInfo tableConvertTableInfo(Table table, LakeCatParam lakeCatParam) {
        TableInfo tableInfo = new TableInfo();
        if (table == null) {
            return null;
        }
        String region = table.getCatalogName();
        if (table.getCatalogName().contains("shareit_")) {
            region = table.getCatalogName().split("_")[1];
        }
        List<Column> columns = table.getStorageDescriptor().getColumns();
        List<ColumnResponse> collect = table.getPartitionKeys().stream()
                .map(ColumnResponse::convertColumn).collect(Collectors.toList());

        List<Column> partitionKeys = table.getPartitionKeys();
        if (partitionKeys == null || partitionKeys.isEmpty()) {
            tableInfo.setPartitionType(0);
        }

        Map<String, String> parameters = table.getParameters();

        if (!parameters.isEmpty()) {
            String lifecycle = parameters.get(LIFECYCLE);
            if (StringUtils.isNotEmpty(lifecycle)) {
                tableInfo.setLifecycle(Integer.parseInt(parameters.get(LIFECYCLE)));
            }
            tableInfo.setSecurityLevel(parameters.get(SECURITY_LEVEL));
            tableInfo.setUseDirection(parameters.get(USE_DIRECTION));
            tableInfo.setCreateBy(parameters.get(CREATE_BY));
        }

        String location = table.getStorageDescriptor().getLocation();
        String storageType = null;
        if (StringUtils.isNotEmpty(location)) {
            storageType = getStorageType(location);
        }

        tableInfo.setCatalogName(table.getCatalogName())
                .setRegion(region)
                .setStorageType(storageType)
                .setDbName(table.getDatabaseName())
                .setTableName(table.getTableName())
                .setName(table.getTableName())
                .setLocation(location)
                .setDescription(table.getDescription())
                .setPartitionKeyList(collect)
                .setPartitionType(collect.isEmpty() ? 0 : 1)
                .setColumnList(columns)
                .setOwner(table.getOwner())
                .setOutputFormat(table.getStorageDescriptor().getOutputFormat())
                .setInputFormat(table.getStorageDescriptor().getInputFormat())
                .setFileFormat(table.getStorageDescriptor().getSourceShortName())
                .setKey(region + "." + table.getDatabaseName() + "." + table.getTableName())
                .setCreateTime(DateUtil.getDateToString(table.getCreateTime() + 28800 * 1000))
                .setUpdateTime(DateUtil.getDateToString(table.getLastAccessTime() + 28800 * 1000));

        if (StringUtils.isNotEmpty(storageType)) {
            tableInfo.setFileFormat(storageType + "," + table.getStorageDescriptor().getSourceShortName());
        }


        TableStats tableStats = table.getTableStats();
        if (tableStats != null) {
            if (tableStats.getNumRows() > 0) {
                tableInfo.setNumRows((int) table.getTableStats().getNumRows());
            }
            if (tableStats.getByteSizeMB() > 0) {
                tableInfo.setByteSize((int) table.getTableStats().getByteSizeMB());
            }
        }
        return tableInfo;
    }


    public String getStorageType(String location) {
        if (location.startsWith("obs://")) {
            return "OBS";
        }
        if (location.startsWith("s3://")) {
            return "S3";
        }
        if (location.startsWith("ks3://")) {
            return "KS3";
        }
        if (location.startsWith("hdfs://")) {
            return "HDFS";
        }
        if (location.startsWith("file:/")) {
            return "LOCAL";
        }
        return null;
    }

    @Override
    public List<TableInfo> searchTable(LakeCatParam lakeCatParam) {
        LakeCatClient lakeCatClient = get();
        //String projectId, String keywords, String catalogName
        String tenantName = InfTraceContextHolder.get().getTenantName();

        String catalog = null;

        if (StringUtils.isNotEmpty(lakeCatParam.getRegion())) {
            catalog = CatalogNameEnum.getCatalogNameByRegion(lakeCatParam.getRegion());
        }

        FilterConditionInput conditionInput = new FilterConditionInput();
        List<Condition> conditions = new ArrayList<>();
        //库过滤
        if (StringUtils.isNotEmpty(lakeCatParam.getDbName())) {
            Map<String, Object> filterDatabaseMap = new HashMap<>();
            filterDatabaseMap.put("databaseName", lakeCatParam.getDbName());
            Condition condition = new Condition();
            condition.setConditionalSymbol("EQUAL");
            condition.setFilterJson(filterDatabaseMap);
            conditions.add(condition);
        }

        //二次模糊搜索
        if (StringUtils.isNotEmpty(lakeCatParam.getSecondaryKeywords())) {
            Map<String, Object> filterDatabaseMap = new HashMap<>();
            filterDatabaseMap.put("tableName", lakeCatParam.getSecondaryKeywords());
            Condition condition = new Condition();
            condition.setConditionalSymbol("LIKE");
            condition.setFilterJson(filterDatabaseMap);
            conditions.add(condition);
        }
        conditionInput.setConditions(conditions);
        //shareit租户区域转catalog
        String keywords = processKeywords(lakeCatParam.getKeywords());
        TableSearchRequest tableSearchRequest = new TableSearchRequest(tenantName, keywords, catalog, conditionInput);
        tableSearchRequest.setLogicalOperator(SearchBaseRequest.LogicalOperator.OR);

        tableSearchRequest.setLimit(Integer.MAX_VALUE);
        if (lakeCatParam.getSize() != null && lakeCatParam.getSize() > 0) {
            tableSearchRequest.setLimit(lakeCatParam.getSize());
        }

        if (StringUtils.isNotEmpty(lakeCatParam.getPageToken())) {
            tableSearchRequest.setPageToken(lakeCatParam.getPageToken());
        }
        if (StringUtils.isNotEmpty(lakeCatParam.getOwner())) {
            tableSearchRequest.setOwner(lakeCatParam.getOwner());
        }

        //如果不选中 就是传的最外层的
        if (lakeCatParam.getCategoryId() != null && lakeCatParam.getCategoryId() > 0) {
            tableSearchRequest.setCategoryId(lakeCatParam.getCategoryId());
        }
        tableSearchRequest.setWithCategories(true);
        PagedList<TableCategories> tableCategoriesPagedList = lakeCatClient.searchTableWithCategories(tableSearchRequest);
        //这2个塞到下一次的传值中
        TableCategories[] tableSearches = tableCategoriesPagedList.getObjects();


        if (StringUtils.isNotEmpty(tableCategoriesPagedList.getNextMarker())) {
            lakeCatParam.setNextMarker(tableCategoriesPagedList.getNextMarker());
        }
        if (StringUtils.isNotEmpty(tableCategoriesPagedList.getPreviousMarker())) {
            lakeCatParam.setPreviousMarker(tableCategoriesPagedList.getPreviousMarker());
        }
        return convertTableInfo(tableSearches, lakeCatParam);
    }

    public static String processKeywords(String keywords) {
        if (StringUtils.isEmpty(keywords)) {
            return keywords;
        }
        String tenantName = InfTraceContextHolder.get().getTenantName();
        if (tenantName.equals("shareit") && (keywords.trim().startsWith("sg1.") ||
                keywords.trim().startsWith("ue1.") || keywords.trim().startsWith("sg2."))) {
            return "shareit_" + keywords;
        }
        return keywords;
    }


    /**
     * 获取库列表
     *
     * @param lakeCatParam
     * @return
     */
    public DatabaseSearch[] searchDatabase(LakeCatParam lakeCatParam) {
        String tenantName = InfTraceContextHolder.get().getTenantName();
        String catalog = null;
        FilterConditionInput conditionInput = new FilterConditionInput();
        List<Condition> conditions = new ArrayList<>();

        conditionInput.setConditions(conditions);
        if (StringUtils.isNotEmpty(lakeCatParam.getRegion())) {
            catalog = CatalogNameEnum.getCatalogNameByRegion(lakeCatParam.getRegion());
        }
        DatabaseSearchRequest databaseSearchRequest = new DatabaseSearchRequest(tenantName, null, catalog, conditionInput);
        databaseSearchRequest.setLogicalOperator(SearchBaseRequest.LogicalOperator.OR);
        databaseSearchRequest.setWithCategories(false);

        PagedList<DatabaseSearch> databaseSearchPagedList = get().searchDatabase(databaseSearchRequest);
        DatabaseSearch[] objects = databaseSearchPagedList.getObjects();
        return objects;
    }


    public List<TableInfo> convertTableInfo(TableCategories[] tableSearches, LakeCatParam lakeCatParam) {
        Glossary glossary = getCacheGlossary(MODEL);

        //默认模型 0 是经典  其他的是其他模型id
        DictInfo dictInfo = new DictInfo();
        dictInfo.setDictType(MODEL);
        List<DictInfo> search = dictMapper.search(dictInfo);
        Map<String, List<DictInfo>> effectiveMap = search.stream().collect(Collectors.groupingBy(DictInfo::getKey));

        List<TableInfo> tableInfos = new ArrayList<>();
        if (tableSearches != null) {
            tableInfos = Arrays.stream(tableSearches).map(data -> {
                TableInfo tableInfo = new TableInfo();
                List<Column> partitionKeys = data.getPartitionKeys();
                if (partitionKeys == null || partitionKeys.isEmpty()) {
                    tableInfo.setPartitionType(0);
                }
                String catalogName = data.getCatalogName();
                String region = catalogName;
                if (catalogName.contains("shareit_")) {
                    region = catalogName.split("_")[1];
                }
                if (data.getLastAccessTime() == 0) {
                    data.setLastAccessTime(data.getCreateTime() + 28800 * 1000);
                }
//                data.get
                log.info(" table  creatime source is :" + DateUtil.getDateToString(data.getCreateTime()));
                String dateToString = DateUtil.getDateToString(data.getLastAccessTime());
                tableInfo.setCatalogName(data.getCatalogName())
                        .setRegion(region)
                        .setDbName(data.getDatabaseName())
                        .setTableName(data.getTableName())
                        .setScore(data.getScore())
                        .setLastAccessTime(dateToString)
                        .setRecentVisitCount(data.getRecentVisitCount())
                        .setLocation(data.getLocation())
                        .setName(data.getTableName())
                        .setTitle(data.getDatabaseName() + "." + data.getTableName())
                        .setOwner(data.getOwner())
                        .setNextMarker(lakeCatParam.getNextMarker())
                        .setPreviousMarker(lakeCatParam.getPreviousMarker())
                        .setDescription(data.getDescription())
                        .setKey(data.getCatalogName() + "." + data.getDatabaseName() + "." + data.getTableName())
                        .setCreateTime(DateUtil.getDateToString(data.getCreateTime() + 28800 * 1000));


                log.info(" data creatime is :" + tableInfo.getCreateTime());
                //设置分层模型
                List<Category> tableCategory = data.getCategories();
                if (tableCategory != null && !tableCategory.isEmpty()) {
                    tableCategory = tableCategory.stream().filter(t -> t.getGlossaryId().equals(glossary.getId()))
                            .collect(toList());
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
                    tableInfo.setListModel(responseList);
                }
                return tableInfo;
            }).collect(Collectors.toList());
        }
        return tableInfos;
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
    public List<TableInfo> searchDiscoveryNames(LakeCatParam lakeCatParam) {
        LakeCatClient lakeCatClient = get();
        String tenantName = InfTraceContextHolder.get().getTenantName();
        String catalog = null;
        if (StringUtils.isNotEmpty(lakeCatParam.getRegion())) {
            catalog = CatalogNameEnum.getCatalogNameByRegion(lakeCatParam.getRegion());
        }

        FilterConditionInput conditionInput = new FilterConditionInput();
        List<Condition> conditions = new ArrayList<>();

        conditionInput.setConditions(conditions);

        SearchDiscoveryNamesRequest searchDiscoveryNamesRequest = new SearchDiscoveryNamesRequest(tenantName,
                lakeCatParam.getKeywords(), catalog, ObjectType.TABLE, conditionInput);

        if (StringUtils.isNotEmpty(lakeCatParam.getPageToken())) {
            searchDiscoveryNamesRequest.setPageToken(lakeCatParam.getPageToken());
        }
        searchDiscoveryNamesRequest.setLimit(Integer.MAX_VALUE);
        if (lakeCatParam.getSize() != null && lakeCatParam.getSize() > 0) {
            searchDiscoveryNamesRequest.setLimit(lakeCatParam.getSize());
        }
        if (StringUtils.isNotEmpty(lakeCatParam.getOwner())) {
            searchDiscoveryNamesRequest.setOwner(lakeCatParam.getOwner());
        }

        PagedList<String> stringPagedList = lakeCatClient.searchDiscoveryNames(searchDiscoveryNamesRequest);

        String nextPagetoken = null;
        String previousPagetoken = null;
        if (StringUtils.isNotEmpty(stringPagedList.getNextMarker())) {
            nextPagetoken = stringPagedList.getNextMarker();
        }
        if (StringUtils.isNotEmpty(stringPagedList.getPreviousMarker())) {
            previousPagetoken = stringPagedList.getPreviousMarker();
        }

        String[] objects = stringPagedList.getObjects();
        List<TableInfo> result = new ArrayList<>();
        if (objects != null) {
            List<String> list = Arrays.asList(objects);
            String finalNextPagetoken = nextPagetoken;
            String finalPreviousPagetoken = previousPagetoken;
            result = list.stream().map(data -> {
                TableInfo tableInfo = new TableInfo();
                String[] split = data.split("\\.");
                if (split[0].contains("shareit")) {
                    tableInfo.setRegion(split[0].split("_")[1]);
                } else {
                    tableInfo.setRegion(split[0]);
                }
                tableInfo.setCatalogName(split[0]).setDbName(split[1]).setTableName(split[2]).setKey(data)
                        .setNextMarker(finalNextPagetoken).setPreviousMarker(finalPreviousPagetoken);
                return tableInfo;
            }).collect(Collectors.toList());
        }
        return result;
    }


    public Database getDataBase(String catalogName, String dbName) {
        /*ListDatabasesRequest listDatabasesRequest = new ListDatabasesRequest();
        listDatabasesRequest.setProjectId(InfTraceContextHolder.get().getTenantName());
        listDatabasesRequest.setCatalogName(catalogName);
        PagedList<Database>databasePagedList=get().listDatabases(listDatabasesRequest);
        if (databasePagedList != null) {
            Database[] databases = databasePagedList.getObjects();
            for (Database database:databases){
                if (database.getDatabaseName().equals(dbName)){
                    log.info("database1-->{}",GsonUtil.toJson(database,false));
                    //return database;
                }
            }
        }*/
        GetDatabaseRequest getDatabaseRequest = new GetDatabaseRequest();
        getDatabaseRequest.setProjectId(InfTraceContextHolder.get().getTenantName());
        getDatabaseRequest.setCatalogName(catalogName);
        getDatabaseRequest.setDatabaseName(dbName);
        LakeCatClient lakeCatClient = get();
        Database database = lakeCatClient.getDatabase(getDatabaseRequest);
        return database;
    }

    public void alterDatabase(String catalogName, String dbName, String owner) {
        GetDatabaseRequest getDatabaseRequest = new GetDatabaseRequest();
        getDatabaseRequest.setProjectId(InfTraceContextHolder.get().getTenantName());
        getDatabaseRequest.setCatalogName(catalogName);
        getDatabaseRequest.setDatabaseName(dbName);
        LakeCatClient lakeCatClient = get();
        Database database = lakeCatClient.getDatabase(getDatabaseRequest);
        AlterDatabaseRequest alterDatabaseRequest = new AlterDatabaseRequest();
        alterDatabaseRequest.setProjectId(InfTraceContextHolder.get().getTenantName());
        alterDatabaseRequest.setCatalogName(catalogName);
        alterDatabaseRequest.setDatabaseName(dbName);
        DatabaseInput databaseInput = new DatabaseInput();
        databaseInput.setAccountId(database.getAccountId());
        databaseInput.setAuthSourceType(database.getAuthSourceType());
        databaseInput.setCatalogName(database.getCatalogName());
        databaseInput.setCreateTime(database.getCreateTime());
        databaseInput.setDatabaseName(database.getDatabaseName());
        databaseInput.setDescription(database.getDescription());
        databaseInput.setLocationUri(database.getLocationUri());
        databaseInput.setOwner(owner);
        databaseInput.setOwnerType(database.getOwnerType());
        databaseInput.setParameters(database.getParameters());
        alterDatabaseRequest.setInput(databaseInput);
        lakeCatClient.alterDatabase(alterDatabaseRequest);
    }


    public void alterDatabase(DatabaseInfo entity) {
        LakeCatClient lakeCatClient = get();
        AlterDatabaseRequest alterDatabaseRequest = new AlterDatabaseRequest();
        alterDatabaseRequest.setProjectId(InfTraceContextHolder.get().getTenantName());
        if (StringUtils.isNotEmpty(entity.getCatalogName())) {
            alterDatabaseRequest.setCatalogName(entity.getCatalogName());
        } else {
            alterDatabaseRequest.setCatalogName(CatalogNameEnum.getCatalogNameByRegion(entity.getRegion()));
        }
        alterDatabaseRequest.setDatabaseName(entity.getDatabaseName());
        DatabaseInput databaseInput = new DatabaseInput();
        databaseInput.setDatabaseName(entity.getDatabaseName());
        databaseInput.setDescription(entity.getDescription());
        databaseInput.setLocationUri(entity.getLocationUri());
        databaseInput.setOwner(entity.getUserGroupName());
        alterDatabaseRequest.setInput(databaseInput);
        lakeCatClient.alterDatabase(alterDatabaseRequest);
    }


    public void toTablePrivilegeForRole(String roleName, String objectname) {
        String comment = "operUser=" + InfTraceContextHolder.get().getUserName() + ",applyUser=" + InfTraceContextHolder.get().getUserName();
        try {
            String userGroupName = tableInfoMapper.selectUserGroupName(roleName);
            if (StringUtils.isNoneBlank(userGroupName)) {
                comment = comment + ",userGroup=" + userGroupName;
            }
        } catch (Exception e) {
            log.error("", e);
        }
        AlterRoleRequest requestAlter = new AlterRoleRequest();
        requestAlter.setProjectId(InfTraceContextHolder.get().getTenantName());
        //requestAlter.setRoleName(roleName);
        for (Operation value : operation.values()) {
            RoleInput roleInput = new RoleInput();
            roleInput.setObjectName(objectname);
            roleInput.setObjectType(ObjectType.TABLE.name());
            roleInput.setRoleName(roleName);
            roleInput.setOperation(value);
            roleInput.setComment(comment);
            requestAlter.setInput(roleInput);
            LakeCatClient lakeCatClient = get();
            log.info("requesttable------------------------------------------>{}", GsonUtil.toJson(requestAlter, false));
            lakeCatClient.grantPrivilegeToRole(requestAlter);
        }
    }

    @Override
    public Role getRole(String roleName) {
        try {
            LakeCatClient lakeCatClient = get();
            GetRoleRequest getRoleRequest = new GetRoleRequest();
            getRoleRequest.setProjectId(InfTraceContextHolder.get().getTenantName());
            getRoleRequest.setRoleName(roleName);
            return lakeCatClient.getRole(getRoleRequest);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }


    public Category listDbTree() {
        //区域
        Category category = new Category();
        category.setName("经典模型");
        category.setId(0);

        //类型
        List<Category> typeList = new ArrayList<>();
        Category typeCategory = new Category();
        typeCategory.setName("Hive");
        typeList.add(typeCategory);
        category.setChildren(typeList);

        //类型
        List<Category> dbList = new ArrayList<>();
        Category db = new Category();
        db.setName("$DB");
        dbList.add(db);
        typeCategory.setChildren(dbList);
        //库
//        try {
//            String tenantName = InfTraceContextHolder.get().getTenantName();
//            ListDatabasesRequest listDatabasesRequest = new ListDatabasesRequest();
//            listDatabasesRequest.setProjectId(tenantName);
//            String catalog = category.getName();
//            listDatabasesRequest.setCatalogName(catalog);
//            PagedList<Database> databasePagedList = get().listDatabases(listDatabasesRequest);
//            List<Database> databaseList = Arrays.asList(databasePagedList.getObjects());
//            List<Category> collect = databaseList.stream().map(data -> {
//                Category db = new Category();
//                db.setName(data.getDatabaseName());
//                db.setDescription(data.getDescription());
//                db.setCreateTime(data.getCreateTime());
//                db.setUpdateTime(data.getCreateTime());
//                return db;
//            }).collect(Collectors.toList());
//            typeCategory.setChildren(collect);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return category;
    }

    public PageInfo<DatabaseSearch> pagesDb(DatabaseInfo databaseInfo) {
        try {
//            String tenantName = InfTraceContextHolder.get().getTenantName();
//            ListDatabasesRequest listDatabasesRequest = new ListDatabasesRequest();
//            listDatabasesRequest.setProjectId(tenantName);
//            String catalog = CatalogNameEnum.getCatalogNameByRegion(databaseInfo.getRegion());
//            listDatabasesRequest.setCatalogName(catalog);
//            PagedList<Database> databasePagedList = get().listDatabases(listDatabasesRequest);
            LakeCatParam build = LakeCatParam.builder().region(databaseInfo.getRegion()).build();
            DatabaseSearch[] databaseSearches = searchDatabase(build);
            List<DatabaseSearch> databaseList = Arrays.asList(databaseSearches);


            databaseList = databaseList.stream().filter(data -> {
                boolean name = true;
                boolean description = true;
                boolean owner = true;
                boolean time = true;

                if (StringUtils.isNotEmpty(databaseInfo.getDatabaseName())) {
                    name = data.getDatabaseName().contains(databaseInfo.getDatabaseName());
                }

                if (StringUtils.isNotEmpty(databaseInfo.getDescription())) {
                    description = false;
                    if (StringUtils.isNotEmpty(data.getDescription())) {
                        description = data.getDescription().contains(databaseInfo.getDescription());
                    }
                }
                if (StringUtils.isNotEmpty(databaseInfo.getOwner())) {
                    owner = false;
                    if (StringUtils.isNotEmpty(data.getOwner())) {
                        owner = data.getOwner().contains(databaseInfo.getOwner());
                    }
                }

                if (databaseInfo.getStartTime() != null) {
                    time = false;
                    if (data.getCreateTime() > 0) {
                        time = data.getCreateTime() > databaseInfo.getStartTime()
                                && data.getCreateTime() < databaseInfo.getEndTime();
                    }
                }
                return name && description && owner && time;
            }).sorted(Comparator.comparing(DatabaseSearch::getDatabaseName)).collect(Collectors.toList());

            List<DatabaseSearch> result = PageUtil.startPage(databaseList, databaseInfo.getPageNum(), databaseInfo.getPageSize());
            PageInfo<DatabaseSearch> databasePageInfo = new PageInfo<>(result);
            databasePageInfo.setPageNum(databaseInfo.getPageNum());
            databasePageInfo.setPageSize(databaseInfo.getPageSize());
            databasePageInfo.setTotal(databaseList.size());

            return databasePageInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取的是模型的目录树
     */
    public Glossary getGlossary(String type) {
        String tenantName = InfTraceContextHolder.get().getTenantName();
        LakeCatClient lakeCatClient = get();
        GetGlossaryRequest getGlossaryRequest = new GetGlossaryRequest();
        getGlossaryRequest.setGlossaryName(type);
        getGlossaryRequest.setProjectId(tenantName);
        Glossary glossary = lakeCatClient.getGlossary(getGlossaryRequest);
        return glossary;
    }

    /**
     * 获取的是模型的目录树
     */
//    @Cacheable(cacheNames = {"model"}, key = "'getGlossary-'+#type")
    public Glossary getCacheGlossary(String type) {
        String tenantName = InfTraceContextHolder.get().getTenantName();
        LakeCatClient lakeCatClient = get();
        GetGlossaryRequest getGlossaryRequest = new GetGlossaryRequest();
        getGlossaryRequest.setGlossaryName(type);
        getGlossaryRequest.setProjectId(tenantName);
        Glossary glossary = lakeCatClient.getGlossary(getGlossaryRequest);
        return glossary;
    }


    /**
     * 创建模型
     */
    public void creteGlossary(Model model) {
        String tenantName = InfTraceContextHolder.get().getTenantName();
        LakeCatClient lakeCatClient = get();
        CreateGlossaryRequest createGlossaryRequest = new CreateGlossaryRequest();
        createGlossaryRequest.setProjectId(tenantName);
        GlossaryInput glossaryInput = new GlossaryInput();
        glossaryInput.setName(model.getName());
        if (StringUtils.isNotEmpty(model.getDescription())) {
            glossaryInput.setDescription(model.getDescription());
        }
        createGlossaryRequest.setInput(glossaryInput);
        lakeCatClient.createGlossary(createGlossaryRequest);
    }

    /**
     * 更新模型
     */
    public Category updateGlossary(Model model) {
        String tenantName = InfTraceContextHolder.get().getTenantName();
        LakeCatClient lakeCatClient = get();
        AlterGlossaryRequest alterGlossaryRequest = new AlterGlossaryRequest();
        alterGlossaryRequest.setProjectId(tenantName);
        GlossaryInput glossaryInput = new GlossaryInput();
        glossaryInput.setName(model.getName());
        glossaryInput.setDescription(model.getDescription());
        alterGlossaryRequest.setInput(glossaryInput);
        lakeCatClient.alterGlossary(alterGlossaryRequest);
        return getCategory(model.getId());
    }


    /**
     * 删除模型
     */
    public void deleteGlossary(Integer id) {
        String tenantName = InfTraceContextHolder.get().getTenantName();
        LakeCatClient lakeCatClient = get();
        DeleteGlossaryRequest deleteGlossaryRequest = new DeleteGlossaryRequest();
        deleteGlossaryRequest.setProjectId(tenantName);
        deleteGlossaryRequest.setId(id);
        lakeCatClient.deleteGlossary(deleteGlossaryRequest);
    }


    /**
     * 获取类目详细信息  以及子类目信息
     */
    public Category getCategory(Integer id) {
        String tenantName = InfTraceContextHolder.get().getTenantName();
        LakeCatClient lakeCatClient = get();
        GetCategoryRequest getCategoryRequest = new GetCategoryRequest();
        getCategoryRequest.setProjectId(tenantName);
        getCategoryRequest.setId(id);
        Category category = lakeCatClient.getCategory(getCategoryRequest);
        return category;
    }


    /**
     * 创建类目
     */
    public Category createCategory(Model model) {
        String tenantName = InfTraceContextHolder.get().getTenantName();
        LakeCatClient lakeCatClient = get();
        CreateCategoryRequest createCategoryRequest = new CreateCategoryRequest();
        createCategoryRequest.setProjectId(tenantName);
        CategoryInput categoryInput = new CategoryInput();
        if (model.getParentId() != null && model.getParentId() > 0) {
            categoryInput.setParentCategoryId(model.getParentId());
        }
        categoryInput.setName(model.getName());
        categoryInput.setDescription(model.getDescription());
        categoryInput.setGlossaryId(model.getGlossaryId());
        if (model.getGlossaryId() == null) {
            Glossary glossary = getCacheGlossary(model.getGlossaryName());
            if (glossary == null) {
                Model modelGlossary = new Model();
                modelGlossary.setName(model.getGlossaryName());
                creteGlossary(modelGlossary);
                glossary = getCacheGlossary(model.getGlossaryName());
            }

            categoryInput.setGlossaryId(glossary.getId());
        }
        createCategoryRequest.setInput(categoryInput);
        return lakeCatClient.createCategory(createCategoryRequest);
    }

    /**
     * 更新类目
     */
    public Category updateCategory(Model model) {
        String tenantName = InfTraceContextHolder.get().getTenantName();
        LakeCatClient lakeCatClient = get();
        AlterCategoryRequest alterCategoryRequest = new AlterCategoryRequest();
        alterCategoryRequest.setProjectId(tenantName);
        alterCategoryRequest.setId(model.getId());
        CategoryInput categoryInput = new CategoryInput();
        categoryInput.setName(model.getName());
        categoryInput.setDescription(model.getDescription());
        alterCategoryRequest.setInput(categoryInput);
        lakeCatClient.alterCategory(alterCategoryRequest);
        return getCategory(model.getId());
    }


    /**
     * 删除类目
     */
    public void deleteCategory(Integer id) {
        String tenantName = InfTraceContextHolder.get().getTenantName();
        LakeCatClient lakeCatClient = get();
        DeleteCategoryRequest deleteCategoryRequest = new DeleteCategoryRequest();
        deleteCategoryRequest.setProjectId(tenantName);
        deleteCategoryRequest.setId(id);
        lakeCatClient.deleteCategory(deleteCategoryRequest);
    }


    /**
     * 经典模型count
     *
     * @param lakeCatParam
     */
    public List<CatalogTableCount> getTableCountByCatalog(LakeCatParam lakeCatParam) {
        String tenantName = InfTraceContextHolder.get().getTenantName();
        LakeCatClient lakeCatClient = get();

        GetCatalogTableCountRequest catalogTableCountRequest = new GetCatalogTableCountRequest();
        if (StringUtils.isNotEmpty(lakeCatParam.getRegion())) {
            catalogTableCountRequest.setCatalogName(CatalogNameEnum.getCatalogNameByRegion(lakeCatParam.getRegion()));
        }
        catalogTableCountRequest.setProjectId(tenantName);
        if (StringUtils.isNotEmpty(lakeCatParam.getKeywords())) {
            catalogTableCountRequest.setKeyword(processKeywords(lakeCatParam.getKeywords()));
        }
        catalogTableCountRequest.setLogicalOperator(SearchBaseRequest.LogicalOperator.OR);
        FilterConditionInput filterConditionInput = new FilterConditionInput();
        List<Condition> conditions = new ArrayList<>();
        filterConditionInput.setConditions(conditions);
        catalogTableCountRequest.setInput(filterConditionInput);


        List<CatalogTableCount> tableCountByCatalog = lakeCatClient.getTableCountByCatalog(catalogTableCountRequest);
        if (tableCountByCatalog == null) {
            return new ArrayList<>();
        }
        return tableCountByCatalog;
    }


    /**
     * 其他模型count
     *
     * @param lakeCatParam
     */
    public ObjectCount getObjectCountByCategory(LakeCatParam lakeCatParam) {
        String tenantName = InfTraceContextHolder.get().getTenantName();
        LakeCatClient lakeCatClient = get();
        GetObjectCountRequest getObjectCountRequest = new GetObjectCountRequest();

        if (StringUtils.isNotEmpty(lakeCatParam.getCatalogName())) {
            getObjectCountRequest.setCatalogName(CatalogNameEnum.getCatalogNameByRegion(lakeCatParam.getRegion()));
        }
        getObjectCountRequest.setProjectId(tenantName);
        getObjectCountRequest.setCategoryId(lakeCatParam.getCategoryId());
        if (StringUtils.isNotEmpty(lakeCatParam.getKeywords())) {
            getObjectCountRequest.setKeyword(processKeywords(lakeCatParam.getKeywords()));
        }

        FilterConditionInput filterConditionInput = new FilterConditionInput();
        List<Condition> conditions = new ArrayList<>();
        filterConditionInput.setConditions(conditions);
        getObjectCountRequest.setInput(filterConditionInput);


        getObjectCountRequest.setLogicalOperator(SearchBaseRequest.LogicalOperator.OR);
        return lakeCatClient.getObjectCountByCategory(getObjectCountRequest);
    }


    //血缘相关 lineageGraph
    public LineageInfo getLineageGraph(LakeCatParam lakeCatParam) {
        LakeCatClient lakeCatClient = get();
        String tenantName = InfTraceContextHolder.get().getTenantName();

        SearchDataLineageRequest searchDataLineageRequest = new SearchDataLineageRequest();
        searchDataLineageRequest.setDbType(EDbType.HIVE);
        searchDataLineageRequest.setProjectId(tenantName);

        String qualifiedName = CatalogNameEnum.getCatalogNameByRegion(lakeCatParam.getRegion()) + "." + lakeCatParam.getDbName() + "." + lakeCatParam.getTableName();
        searchDataLineageRequest.setQualifiedName(qualifiedName);
        searchDataLineageRequest.setDepth(lakeCatParam.getDepth());

        String objectType = lakeCatParam.getObjectType();
        String eLineageType = lakeCatParam.getLineageType();
        searchDataLineageRequest.setLineageType(ELineageType.valueOf(eLineageType));
        searchDataLineageRequest.setObjectType(ELineageObjectType.valueOf(objectType));

        String direction = lakeCatParam.getDirection();
        searchDataLineageRequest.setDirection(ELineageDirection.valueOf(direction));
        searchDataLineageRequest.setStartTime(0L);


        LineageInfo lineageInfo = lakeCatClient.searchDataLineageGraph(searchDataLineageRequest);
        return lineageInfo;
    }

    //血缘相关
    public LineageFact getLineageFact(LakeCatParam lakeCatParam) {
        LakeCatClient lakeCatClient = get();
        String tenantName = InfTraceContextHolder.get().getTenantName();
        if (StringUtils.isEmpty(lakeCatParam.getJobFactId())) {
            return null;
        }
        GetDataLineageFactRequest getDataLineageFactRequest = new GetDataLineageFactRequest(lakeCatParam.getJobFactId());
        getDataLineageFactRequest.setProjectId(tenantName);

        LineageFact dataLineageFact = lakeCatClient.getDataLineageFact(getDataLineageFactRequest);
        return dataLineageFact;
    }

}
