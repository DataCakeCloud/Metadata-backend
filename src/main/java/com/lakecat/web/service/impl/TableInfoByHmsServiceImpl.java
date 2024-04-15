package com.lakecat.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lakecat.web.config.GlobalConfig;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.entity.DatabaseInfo;
import com.lakecat.web.entity.LakeCatParam;
import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.mapper.TableInfoMapper;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.service.ITableInfoByHMSService;
import com.lakecat.web.service.ITableInfoService;
import com.lakecat.web.utils.OLAPJDBCUtil;
import com.lakecat.web.utils.SqlUtils;
import io.lakecat.catalog.common.plugin.request.GetTableRequest;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.*;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @author slj
 */
@Slf4j
@Service
@Component
public class TableInfoByHmsServiceImpl extends ServiceImpl <TableInfoMapper, TableInfo> implements ITableInfoByHMSService {
    private static final String SPACE = " ";
    private static final String HIVE_URIS_KEY = "hive.metastore.uris";

/*    @Value("${UE1_CLOUDPATH}")
    private String UE1_CLOUDPATH;


    @Value("${SG1_CLOUDPATH}")
    private String SG1_CLOUDPATH;


    @Value("${SG2_CLOUDPATH}")
    private String SG2_CLOUDPATH;*/

    private static Map <String, HiveConf> regionHiveConfMap = new HashMap <>();

    @Autowired
    OLAPJDBCUtil olapJdbcUtil;

    @Autowired
    GlobalConfig globalConfig;
    @Autowired
    TableInfoMapper tableInfoMapper;

    @Autowired
    ITableInfoService iTableInfoService;

    @Autowired
    CatalogNameEnum CatalogNameEnum;


    @Autowired
    ILakeCatClientService iLakeCatClientService;


    private void initRegionHiveConf(String region) {
        HiveConf hiveConf = new HiveConf();
        hiveConf.set(HIVE_URIS_KEY, globalConfig.getHiveMetastoreUris(region).trim());

        hiveConf.setLong("hive.metastore.client.socket.timeout", 60000);
        hiveConf.setLong("hive.hmshandler.retry.interval", 100);
        hiveConf.setLong("hive.hmshandler.retry.attempts", Integer.MAX_VALUE);

        regionHiveConfMap.put(region, hiveConf);
    }

    public IMetaStoreClient getMSC(String region, String tenantName, String userName) {
        try {
            if (region == null) {
                region = CatalogNameEnum.getDefaultRegion();
            }
            if (!regionHiveConfMap.containsKey(region)) {
                initRegionHiveConf(region);
            }
            HiveConf hiveConf = regionHiveConfMap.get(region);
            if (StringUtils.isNotEmpty(hiveConf.get(HIVE_URIS_KEY))) {
                initRegionHiveConf(region);
            }
            HiveConf entries = regionHiveConfMap.get(region);
            entries.set("bdp-query-user", userName);
            entries.set("bdp-query-tenancy", tenantName);
            // 支持并发,该并发访问patch自HIVE-10956引入
            return Hive.get(regionHiveConfMap.get(region)).getMSC();

        } catch (HiveException | MetaException e) {
            e.printStackTrace();
            log.error("Get Hms client error: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public TableInfo createTable(TableInfo entity) {
        try {
            String region = entity.getRegion();
            String tenantName = entity.getTenantName();
            String owner = entity.getOwner();
            String dbName = entity.getDbName();
            String name = entity.getName();
            String catalogName = CatalogNameEnum.getCatalogNameByRegion(entity.getRegion());
            entity.setCatalogName(catalogName);
            io.lakecat.catalog.common.model.Database dbByName = iTableInfoService.getDBByName(tenantName, region, dbName);
            String location = dbByName.getLocationUri() + "/" + name;
            entity.setLocation(location);
            olapJdbcUtil.getSqlResult(getCreateTableSql(entity), region, tenantName, owner);
            Table table;
            //原逻辑
            table = getMSC(region, tenantName, owner).getTable(dbName, name);
            StorageDescriptor sd = table.getSd();
            entity.setColumns(transColumnOutPutByHMS(sd.getCols()));
            List <FieldSchema> partitionKeys = table.getPartitionKeys();
            entity.setPartitionKeys(transColumnOutPutByHMS(partitionKeys));
            entity.setDescription(table.getParameters().getOrDefault("comment", ""));
            if (table.getPartitionKeysSize() > 0) {
                entity.setPartitionType(1);
            }
            if (StringUtils.isNotEmpty(table.getOwner())) {
                entity.setOwner(table.getOwner());
            }
            entity.setLocation(sd.getLocation());
            log.info("TableInfo: {}", entity);


            HashMap <String, Object> selectMap = new HashMap <>();
            selectMap.put("region", entity.getRegion());
            selectMap.put("db_name", entity.getDbName());
            selectMap.put("name", entity.getName());
            if (!tableInfoMapper.selectByMap(selectMap).isEmpty()) {
                tableInfoMapper.updateTableInfo(entity);
            } else {
                this.save(entity);
            }
            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("Create table warn: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String getCreateTableSql(TableInfo entity) {
        String userSql = null;
        entity.setName(entity.getName().trim());
        entity.setDbName(entity.getDbName().trim());

        try {
            userSql = URLDecoder.decode(new String(Base64.getDecoder().decode(entity.getSql())), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String noCommentsSql = userSql.replaceAll("--.*", "");
        noCommentsSql = noCommentsSql.replaceAll("COMMENT\\s+'(\\\\'|[^'])*'", "COMMENT '1'");
        String patternString = "CREATE\\s+[^\\)]*\\)";
        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(noCommentsSql);

        String createTable="";
        if (matcher.find()) {
            createTable = matcher.group();
        }
        //System.out.println(createTable);
        CreateTable createTableStatement=null;
        try {
            createTableStatement = (CreateTable) CCJSqlParserUtil.parse(createTable);
        } catch (JSQLParserException e) {
            throw new RuntimeException("hivesql语句不规范。");
        }
        String dbname=createTableStatement.getTable().getSchemaName();
        String tableName=createTableStatement.getTable().getName();
        if (StringUtils.isNoneBlank(dbname)&&!"null".equalsIgnoreCase(dbname)){
            dbname=dbname.replaceAll("`","");
            if (!dbname.equalsIgnoreCase(entity.getDbName().trim())){
                throw new RuntimeException("库名不一致。");
            }
        }
        if (StringUtils.isNoneBlank(tableName)){
            tableName=tableName.replaceAll("`","");
            if (!tableName.equalsIgnoreCase(entity.getName().trim())){
                throw new RuntimeException("表名不一致。");
            }
        }
        StringBuffer stringBuffer = new StringBuffer();
        checkAndSetSd(entity, stringBuffer, userSql);
        String hivesql=wrapHiveSql(stringBuffer,entity);
        return hivesql;
    }

    public static void checkAndSetSd(TableInfo entity, StringBuffer stringBuffer, String userSql) {
        userSql = SqlUtils.replaceLastSemicolon(userSql);
        log.info("User sql: {}", userSql);
        stringBuffer.append(userSql);
        String lowStrSql = SqlUtils.trimLine(userSql).toLowerCase(Locale.ROOT);
        String fileFormat = entity.getSdFileFormat();
        // 文件格式校验逻辑
        /*if (StringUtils.isNotEmpty(fileFormat)) {
            String storedAs = " stored as ";
            if (matchSqlStrValid(lowStrSql, storedAs, fileFormat, "FileFormat") && matchSqlStrValid(lowStrSql, " using ", fileFormat, "FileFormat")) {
                if (StringUtils.isNotEmpty(entity.getFileDelimiter()) && ("textfile".equalsIgnoreCase(fileFormat) || "text".equalsIgnoreCase(fileFormat))) {
                    stringBuffer.append(" row format delimited fields terminated by '")
                            .append(entity.getFileDelimiter()).append("' ");
                }
                stringBuffer.append(storedAs).append(fileFormat).append(SPACE);
            }
        }
*/
        String location = entity.getLocation();
        /*if (StringUtils.isNotEmpty(location)) {
            String matchStr = " location ";
            if (matchSqlStrValid(lowStrSql, matchStr, "'" + location + "'", "Location")) {
                stringBuffer.append(matchStr).append("'").append(location).append("'").append(SPACE);
            }
        }*/
    }

    /**
     * 匹配进逻辑返回true
     *
     * @param lowStrSql
     * @param matchStr
     * @param validValue
     * @param validKey
     * @return
     * @throws RuntimeException
     */
    public static boolean matchSqlStrValid(String lowStrSql, String matchStr, String validValue, String validKey) {
        if (SqlUtils.matchKeyword(lowStrSql, matchStr) && !lowStrSql.startsWith(matchStr)) {
            // TODO
            String substring = lowStrSql.split(matchStr)[1];
            if (StringUtils.isNotEmpty(substring) && validValue != null) {
                String sqlFileFormat = substring.split("\\s+")[0];
                if ("text".equals(sqlFileFormat)) {
                    sqlFileFormat = "textfile";
                }
                if (!validValue.equalsIgnoreCase(sqlFileFormat)) {
                    throw new RuntimeException(getConsistentExceptionStr(validKey));
                }
            }
            //匹配到用户传入字符串
            return false;
        }
        return true;
    }

    private static String getConsistentExceptionStr(String key) {
        return String.format("不一致错误, 请检查 '%s' 是否输入不一致。", key);
    }


    @Override
    public void createDatabase(DatabaseInfo entity) {
        org.apache.hadoop.hive.metastore.api.Database database = new org.apache.hadoop.hive.metastore.api.Database();
        database.setName(entity.getDatabaseName());
        database.setLocationUri(getLocationUri(entity.getLocationUri(), entity.getRegion(), entity.getDatabaseName()));
        database.setDescription(entity.getDescription());
        Map <String, String> parameters = new HashMap <>();
        parameters.put("lms_name", CatalogNameEnum.getCatalogNameByRegion(entity.getRegion()));
        database.setParameters(parameters);
        try {
            getMSC(entity.getRegion(), entity.getProjectId(), entity.getUserId()).createDatabase(database);
        } catch (TException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }


    public String getLocationUri(String locationUri, String region, String databaseName) {
        return locationUri;
        /*if (StringUtils.isNotBlank(locationUri)) {
            return locationUri;
        } else {
            String cloudPath = "";
            switch (region) {
                case "ue1":
                    cloudPath = UE1_CLOUDPATH;
                    break;
                case "sg1":
                    cloudPath = SG1_CLOUDPATH;
                    break;
                case "sg2":
                    cloudPath = SG2_CLOUDPATH;
                    break;
                default:
                    throw new IllegalArgumentException("参数格式错误");
            }
            return cloudPath + "data/" + databaseName;
        }*/
    }


    @Override
    public boolean alterColumnsByHMS(TableInfo entity) {

        /**
         * 判断字段是否重复
         */
        List <String> listNew = new ArrayList <>();
        List <FieldSchema> fieldSchemasForNew = transField(JSON.parseArray(entity.getColumns()), listNew);
        long count = listNew.stream().distinct().count();
        if (listNew.size() != count) {
            return false;
        }
        String dbName = entity.getDbName();
        String tableName = entity.getName();
        // hms alter
        LakeCatParam lakeCatParam = LakeCatParam.builder().region(entity.getRegion())
                .dbName(dbName).tableName(tableName).build();

        TableInfo tableDetail = iLakeCatClientService.getTable(lakeCatParam);
        try {
            List <String> listOld = new ArrayList <>();
            List <FieldSchema> fieldSchemasForOld = transField(JSON.parseArray(tableDetail.getColumns()), listOld);
            // 通过 spark DDL 的方式是为了（用户使用spark datasource 时） spark自身读取自身维护的properties 保持行为一致。
            if (fieldSchemasForNew.size() > fieldSchemasForOld.size()) {
                addColumnsBySparkDDL(dbName, tableName, fieldSchemasForNew, entity, fieldSchemasForOld);
            } else {
                alterColumnsBySparkDDL(dbName, tableName, fieldSchemasForNew, entity, fieldSchemasForOld);
            }
            entity.setUpdateTime(LocalDateTime.now().toString());
            return tableInfoMapper.updateTableByKey(entity);
        } catch (TException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 一次只能改一个字段
     *
     * @param dbName
     * @param tableName
     * @param fieldSchemas
     * @param tableInfo
     * @param cols
     * @throws BusinessException
     */
    private void alterColumnsBySparkDDL(String dbName, String tableName, List <FieldSchema> fieldSchemas,
                                        TableInfo tableInfo, List <FieldSchema> cols)
            throws BusinessException {
        List <FieldSchema> list = fieldSchemas.stream().filter(u -> {
            return cols.stream().filter(e -> {
                if (u.getName().equalsIgnoreCase(e.getName()) && u.getType().equalsIgnoreCase(e.getType())) {
                    return !Objects.equals(u.getComment(), e.getComment());
                }
                return false;
            }).count() > 0;
        }).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(list)) {
            for (FieldSchema fieldSchema : list) {
                String sql = String.format(" Alter table %s.%s change %s %s %s comment '%s'", dbName, tableName,
                        fieldSchema.getName(), fieldSchema.getName(), fieldSchema.getType(), fieldSchema.getComment());
                executeSqlByOlap(sql, tableInfo);
            }
        }
    }

    private void addColumnsBySparkDDL(String dbName, String tableName, List <FieldSchema> fieldSchemasForNew,
                                      TableInfo tableInfo, List <FieldSchema> fieldSchemasForOld) throws BusinessException {

        if (!fieldSchemasForNew.isEmpty()) {
            fieldSchemasForNew = fieldSchemasForNew.stream().filter(e -> {
                return !fieldSchemasForOld.contains(e);
            }).collect(Collectors.toList());
            for (FieldSchema fieldSchema : fieldSchemasForNew) {
                String sql = String.format(" Alter table %s.%s add column %s %s comment '%s'", dbName, tableName,
                        fieldSchema.getName(), fieldSchema.getType(), fieldSchema.getComment());
                executeSqlByOlap(sql, tableInfo);
            }
        }
    }

    private void executeSqlByOlap(String sql, TableInfo tableInfo) throws BusinessException {
        log.info("Alter column start, SQL: {}", sql);
        olapJdbcUtil.getSqlResult(sql, tableInfo.getRegion(), tableInfo.getTenantName(), tableInfo.getUserName());
        log.info("Alter column success, SQL: {}", sql);
    }

    @Override
    public List <String> getDBList(String region, String tenantName, String userName) {
        List <String> databases = null;
        try {
            databases = getMSC(region, tenantName, userName).getAllDatabases();
        } catch (TException e) {
            e.printStackTrace();
        }
        return databases;
    }

    @Override
    public List <String> getTableList(String region, String dbName, String tenantName, String userName) {
        List <String> tables = null;
        try {
            tables = getMSC(region, tenantName, userName).getAllTables(dbName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tables;
    }

    @Override
    public Boolean checkCreate(TableInfo entity) {
        String tenantName = entity.getTenantName();
        String region = entity.getRegion();
        String dbName = entity.getDbName();
        String name = entity.getName();
        io.lakecat.catalog.common.model.Database dbByName = iTableInfoService.getDBByName(tenantName, region, dbName);
        String locationUri = dbByName.getLocationUri();
       /* if (locationUri == null || !locationUri.startsWith("s3://")) {
            //通过region 获取云资源url
            String storage = CatalogNameEnum.getStorageByRegion(entity.getRegion());
            if (storage == null || storage.equals("0")) {
                return false;
            }
            entity.setLocation(storage + "/" + dbName + "/" + name);

        } else {
            String location = dbByName.getLocationUri() + "/" + name;
            entity.setLocation(location);
        }*/
        getCreateTableSql(entity);
        return true;
    }

    private List <FieldSchema> transField(JSONArray jsonArray, List <String> names) {
        List <FieldSchema> fieldSchemas = new ArrayList <>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            FieldSchema fieldSchema = new FieldSchema();
            String name = jsonObject.getString("name");
            fieldSchema.setName(name);
            fieldSchema.setType(jsonObject.getString("type").toLowerCase());
            fieldSchema.setComment(jsonObject.getString("comment"));
            fieldSchemas.add(fieldSchema);
            names.add(name);
        }
        return fieldSchemas;
    }

    private static String transColumnOutPutByHMS(List <FieldSchema> fieldSchema) {
        List <JSONObject> Column = new ArrayList <>();
        int nub = 1;
        for (FieldSchema output : fieldSchema) {
            nub++;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", nub);
            jsonObject.put("name", output.getName());
            jsonObject.put("type", output.getType());
            jsonObject.put("comment", output.getComment());
            Column.add(jsonObject);
        }
        return Column.toString();
    }

    public static Pattern pattern = Pattern.compile("TBLPROPERTIES\\s*\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public  static String wrapHiveSql(StringBuffer stringBuffer,TableInfo entity){
        String sql=stringBuffer.toString();
        if (sql.endsWith(";")){
            sql=sql.substring(0,sql.length()-1);
        }
        Matcher matcher = pattern.matcher(sql);
        boolean find=false;
        while (matcher.find()) {
            String match = matcher.group(1);
            sql=sql.replace(match,match+","+"'lms_name'='"+entity.getCatalogName()+"'");
            find=true;
            break;
        }
        if (!find){
            sql=sql+SPACE+"TBLPROPERTIES('lms_name'='"+entity.getCatalogName()+"')";
        }
        return sql;
    }

    public static void main(String[] args) throws Exception{
        /*String s="CREATE TABLE IF NOT EXISTS dbname.tablename (\n" +
                "  id string COMMENT 'ID' ,\n" +
                "  name string COMMENT '名称'\n" +
                ")\n" +
                "USING ICEBERG\n" +
                "COMMENT '测试表'\n" +
                "PARTITIONED BY ( `dt` string COMMENT '分区时间yyyy-MM-dd') TBLPROPERTIES('lms_name'='1232');";

        System.out.println(wraphivesql(new StringBuffer(s)));*/
        String hiveSql = "-- CREATE TABLE TEMPLATE\n" +
                "-- 左侧必填：创建区域、数据源类型、库名、表名、location、fileformat。\n" +
                "-- SQL窗口定义schema和分区即可，选填：字段 COMMENT 注释，表  COMMENT 注释，location 自定义示例：s3(obs)示例：\"s3://bucket/../databaseName/tableName\"\n" +
                "CREATE external TABLE `dwd_t_electric_vehicle_result`(      \n" +
                "   `id` bigint COMMENT '主键',                        \n" +
                "   `uid` bigint COMMENT '用户id',                     \n" +
                "   `wnumber` string COMMENT '序列号',                  \n" +
                "   `travel_days` int COMMENT '骑行天数',                \n" +
                "   `year_total_mileage` bigint COMMENT '骑行总\\'里程',     \n" +
                "   `travel_count` int COMMENT '骑行轨迹数',              \n" +
                "   `longest_travel_time` string COMMENT '骑行最远的一天',  \n" +
                "   `longest_travel_mileage` bigint COMMENT '骑行最远的里程',  \n" +
                "   `single_longest_travel_mileage` bigint COMMENT '骑行最远的一条轨迹',  \n" +
                "   `start_time` bigint COMMENT '骑行最远的一条轨迹的开始时间戳',   \n" +
                "   `end_time` bigint COMMENT '骑行最远的一条轨迹的结束时间戳',     \n" +
                "   `early_peak_travel_days` int COMMENT '早高峰骑行天数',  \n" +
                "   `evening_peak_travel_days` int COMMENT '晚高峰骑行天数') \n" +
                " PARTITIONED BY (                                   \n" +
                "   `dt` string COMMENT '')                          \n" +
                "stored as parquet\n" +
                " TBLPROPERTIES ('parquet.compression'='SNAPPY')";
        String noCommentsSql = hiveSql.replaceAll("--.*", "");
        noCommentsSql = noCommentsSql.replaceAll("COMMENT\\s+'(\\\\'|[^'])*'", "COMMENT '1'");



        String patternString = "CREATE\\s+[^\\)]*\\)";
        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(noCommentsSql);

        String createTable="";
        if (matcher.find()) {
            createTable = matcher.group();
        }
        System.out.println(createTable);
        CreateTable createTableStatement=null;
        try {
            createTableStatement = (CreateTable) CCJSqlParserUtil.parse(createTable);
        } catch (JSQLParserException e) {
            throw new RuntimeException("hivesql语句不规范。");
        }
        String dbname=createTableStatement.getTable().getSchemaName();
        String tableName=createTableStatement.getTable().getName();
        System.out.println(dbname);
        System.out.println(tableName);
        TableInfo tableInfo=new TableInfo();
        tableInfo.setCatalogName("beij");
        System.out.println(wrapHiveSql(new StringBuffer(hiveSql),tableInfo));
    }
}
