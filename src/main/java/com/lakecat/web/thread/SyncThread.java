package com.lakecat.web.thread;

import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.entity.LastActivityInfo;
import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.entity.TableInfoReq;
import com.lakecat.web.entity.TableUsageProfileGroupByUser;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.mapper.TableInfoMapper;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.service.ITableInfoService;
import com.lakecat.web.service.impl.DataGradeServiceImpl;
import com.lakecat.web.utils.DateUtil;
import com.lakecat.web.utils.LakecatTableUtils;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.model.*;
import io.lakecat.catalog.common.plugin.request.ListDatabasesRequest;
import io.lakecat.catalog.common.plugin.request.ListTablesRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
@Log4j2
public class SyncThread {
    /**
     * corePoolSize 线程池维护线程的最少数量
     * maximumPoolSize 线程池维护线程的最大数量
     * keepAliveTime 线程池维护线程所允许的空闲时间
     * unit 线程池维护线程所允许的空闲的空闲时间单位
     * workQueue 线程池所使用的缓冲队列
     * handler 线程池对拒绝任务的处理策略
     */
    public static ExecutorService pool = new ThreadPoolExecutor(1, 1,
            1, TimeUnit.SECONDS, new ArrayBlockingQueue <>(1), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("MONITOR_ALERT_COMPLETABLEFUTURE");
            thread.setDaemon(true);
            return thread;
        }
    }, new ThreadPoolExecutor.CallerRunsPolicy());


    private static ExecutorService poolNext = new ThreadPoolExecutor(200, 200,
            5, TimeUnit.SECONDS, new ArrayBlockingQueue <>(10000), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("MONITOR_ALERT_COMPLETABLEFUTURE");
            thread.setDaemon(true);
            return thread;
        }
    }, new ThreadPoolExecutor.CallerRunsPolicy());

    @Autowired
    DataGradeServiceImpl dataGradeService;


    @Autowired
    TableInfoMapper tableInfoMapper;


    @Autowired
    ITableInfoService iTableInfoService;

    @Autowired
    CatalogNameEnum CatalogNameEnum;


    public static CopyOnWriteArraySet <String> LakecatTableNameSet = new CopyOnWriteArraySet <>();

    //多线程执行
    public synchronized boolean doSendMonitorAlertInfo(LakeCatClient lakeCatClient, List <String> args, String tenantName, Boolean flag) {

        if (args.isEmpty()) {
            return true;
        }
        LakecatTableNameSet.clear();
        Map <String, Map <String, Long>> nameAndIdMap = getTableNameAndIdMap();
        try {
            List <CompletableFuture <Integer>> sendStatesFutureList = args
                    .stream()
                    .map(arg -> syncByRegion(lakeCatClient, arg, nameAndIdMap, LakecatTableNameSet, tenantName))
                    .collect(Collectors.toList());

            CompletableFuture <Void> allFutures = CompletableFuture
                    .allOf(sendStatesFutureList.toArray(new CompletableFuture[sendStatesFutureList.size()]));
            //获取子任务的结果
            allFutures.join();
            System.out.println("执行同步完成");
        } catch (Exception e) {
        }
        if (flag) {
            return true;
        }
        System.out.println(nameAndIdMap.get("new").size() + "=========" + nameAndIdMap.get("old").size() + "******************开始执行删除逻辑********************" + LakecatTableNameSet.size());
        for (Map.Entry <String, Long> entry : nameAndIdMap.get("new").entrySet()) {
            if (!LakecatTableNameSet.contains(entry.getKey())) {
                System.out.println("删除" + entry.getKey() + ";");
                String[] dbNameAndName = entry.getKey().split("\\.");
                tableInfoMapper.deleteTable(dbNameAndName[1], dbNameAndName[2], dbNameAndName[0]);
                dataGradeService.deleteTableById(entry.getValue());
                log.info("delete: {}", entry);
            }
        }
        for (Map.Entry <String, Long> entry : nameAndIdMap.get("old").entrySet()) {
            System.out.println("删除" + entry.getKey() + ";");
            tableInfoMapper.deleteTableById(entry.getValue());
            dataGradeService.deleteTableById(entry.getValue());
            log.info("delete: {}", entry);
        }
        LakecatTableNameSet.clear();
        System.out.println("执行删除无效表完成");
        return true;
    }

    /**
     * 单线程执行
     * @param lakeCatClient
     * @param args
     * @param tenantName
     * @param flag
     * @return
     */
    public synchronized boolean doSendMonitorAlertInfoSingle(LakeCatClient lakeCatClient, List <String> args, String tenantName, Boolean flag) {

        if (args.isEmpty()) {
            return true;
        }
        LakecatTableNameSet.clear();
        Map <String, Map <String, Long>> nameAndIdMap = getTableNameAndIdMap();
        args.forEach(arg->{
            syncByRegionSingle(lakeCatClient, arg, nameAndIdMap, LakecatTableNameSet, tenantName);
        });
        if (flag) {
            return true;
        }
        System.out.println(nameAndIdMap.get("new").size() + "=========" + nameAndIdMap.get("old").size() + "******************开始执行删除逻辑********************" + LakecatTableNameSet.size());
        for (Map.Entry <String, Long> entry : nameAndIdMap.get("new").entrySet()) {
            if (!LakecatTableNameSet.contains(entry.getKey())) {
                System.out.println("删除" + entry.getKey() + ";");
                String[] dbNameAndName = entry.getKey().split("\\.");
                tableInfoMapper.deleteTable(dbNameAndName[1], dbNameAndName[2], dbNameAndName[0]);
                dataGradeService.deleteTableById(entry.getValue());
                log.info("delete: {}", entry);
            }
        }
        for (Map.Entry <String, Long> entry : nameAndIdMap.get("old").entrySet()) {
            System.out.println("删除" + entry.getKey() + ";");
            tableInfoMapper.deleteTableById(entry.getValue());
            dataGradeService.deleteTableById(entry.getValue());
            log.info("delete: {}", entry);
        }
        LakecatTableNameSet.clear();
        System.out.println("执行删除无效表完成");
        return true;
    }


    public static <T> List <List <T>> splitList(List <T> list, int groupSize) {
        int length = list.size();
        /**
         * num 可以分成的组数
         **/
        int num = (length + groupSize - 1) / groupSize;
        //用于存放最后结果
        List <List <T>> result = new ArrayList <>(num);
        for (int i = 0; i < num; i++) {
            // 开始位置
            int fromIndex = i * groupSize;
            // 结束位置
            int toIndex = (i + 1) * groupSize < length ? (i + 1) * groupSize : length;
            result.add(list.subList(fromIndex, toIndex));
        }
        return result;
    }


    public static void main(String[] args) {

        List <Integer> list = new ArrayList <>();
        for (int i = 0; i < 10001; i++) {
            list.add(i);
        }

        List <List <Integer>> lists = splitList(list, 500);
        for (List <Integer> integers : lists) {
            System.out.println(integers.size());
        }

    }

    /**
     * 开启线程分region同步
     *
     * @return CompletableFuture
     */
    private CompletableFuture <Integer> syncByRegion(LakeCatClient lakeCatClient, String region,
                                                     Map <String, Map <String, Long>> nameAndIdMap,
                                                     CopyOnWriteArraySet <String> LakecatTableNameSet,
                                                     String tenantName) {
        return CompletableFuture.supplyAsync(() -> {
            //再起线程
            try {
                List <CompletableFuture <Integer>> sendStatesFutureList = new ArrayList <>();
                Database[] dbList = getDBListBySDK(lakeCatClient, region, tenantName);
                for (Database database : dbList) {
                    //再起线程
                    List <Table> tableListBySDK = getTableListBySDK(lakeCatClient, database, tenantName);
                    if (tableListBySDK.isEmpty()) {
                        continue;
                    }
                    if (tableListBySDK.size() <= 1000) {
                        sendStatesFutureList.add(doSyncByDatabase(tableListBySDK, region, nameAndIdMap, LakecatTableNameSet, tenantName));
                    } else {
                        List <List <Table>> lists = splitList(tableListBySDK, 500);
                        for (List <Table> list : lists) {
                            sendStatesFutureList.add(doSyncByDatabase(list, region, nameAndIdMap, LakecatTableNameSet, tenantName));
                        }
                    }
                }
                System.out.println(region + ":开启线程个数" + sendStatesFutureList.size());
                CompletableFuture <Void> allFutures = CompletableFuture
                        .allOf(sendStatesFutureList.toArray(new CompletableFuture[sendStatesFutureList.size()]));
                //获取子任务的结果
                allFutures.join();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }, pool);
    }

    private void syncByRegionSingle(LakeCatClient lakeCatClient, String region,
                                                     Map <String, Map <String, Long>> nameAndIdMap,
                                                     CopyOnWriteArraySet <String> LakecatTableNameSet,
                                                     String tenantName) {
        Database[] dbList = getDBListBySDK(lakeCatClient, region, tenantName);
        for (Database database : dbList) {
            //再起线程
            List <Table> tableListBySDK = getTableListBySDK(lakeCatClient, database, tenantName);
            if (tableListBySDK.isEmpty()) {
                continue;
            }
            if (tableListBySDK.size() <= 1000) {
                doSyncByDatabaseSingle(tableListBySDK, region, nameAndIdMap, LakecatTableNameSet, tenantName);
            } else {
                List <List <Table>> lists = splitList(tableListBySDK, 500);
                for (List <Table> list : lists) {
                    doSyncByDatabaseSingle(list, region, nameAndIdMap, LakecatTableNameSet, tenantName);
                }
            }
        }
    }


    private Database[] getDBListBySDK(LakeCatClient lakeCatClient, String region, String tenantName) {
        ListDatabasesRequest listDatabasesRequest = new ListDatabasesRequest();
        listDatabasesRequest.setProjectId(tenantName);
        try {
            listDatabasesRequest.setCatalogName(CatalogNameEnum.getCatalogNameByRegion(region));
        } catch (Exception e) {
            e.printStackTrace();
        }
        PagedList <Database> databasePagedList = lakeCatClient.listDatabases(listDatabasesRequest);
        return databasePagedList.getObjects();
    }


    /**
     * 开启线程写数据到拨打记录表、回调表
     */

    public CompletableFuture <Integer> doSyncByDatabase(List <Table> tableListBySDK,
                                                        String region, Map <String, Map <String, Long>> nameAndIdMap,
                                                        CopyOnWriteArraySet <String> LakecatTableNameSet, String tenantName) {
        return CompletableFuture.supplyAsync(() -> {

            List <TableInfo> tableListForUpdate = new ArrayList <>();
            List <TableInfo> tableListForSave = new ArrayList <>();
            List <LastActivityInfo> lastActivityInfoList = new ArrayList <>();
            for (Table table : tableListBySDK) {
                Map <String, String> properties = table.getParameters();
                String transientLastDdlTime = properties.get("transient_lastDdlTime");
                TableInfo tableInfo = new TableInfo();
                tableInfo.setDbName(table.getDatabaseName());
                tableInfo.setName(table.getTableName());
                tableInfo.setOwner(table.getOwner());
                if (transientLastDdlTime != null) {
                    try {
                        tableInfo.setTransientLastDdlTime(DateUtil.getDateToString(Long.parseLong(transientLastDdlTime) * 1000));
                    } catch (Exception e) {
                        System.out.println(transientLastDdlTime);
                    }
                }
                tableInfo.setColumns(transColumnOutPut(table.getFields()));
                List <Column> partitions = table.getPartitionKeys();
                tableInfo.setPartitionKeys(transColumnOutPut(partitions));
                if (!partitions.isEmpty()) {
                    tableInfo.setPartitionType(1);
                } else {
                    tableInfo.setPartitionType(0);
                }
                tableInfo.setDescription(table.getParameters().getOrDefault("comment", ""));
                tableInfo.setCreateTime(DateUtil.getDateToString(table.getCreateTime()));
                StorageDescriptor tableStorage = table.getStorageDescriptor();
                String location = tableStorage.getLocation();
                tableInfo.setLocation(location);
                try {
                    tableInfo.setRegion(CatalogNameEnum.getRegion(table.getCatalogName()));
                } catch (Exception e) {
                    log.error("",e);
                    continue;
                }
                tableInfo.setType(LakecatTableUtils.getTableType(table));
                tableInfo.setSdFileFormat(tableStorage.getSourceShortName());
                String tableName = tableInfo.getRegion() + "." + table.getDatabaseName() + "." + table.getTableName();
                TableInfoReq tableInfoReq = new TableInfoReq();
                tableInfoReq.setRegion(tableInfo.getRegion());
                tableInfoReq.setDatabaseName(table.getDatabaseName());
                tableInfoReq.setTableName(table.getTableName());
                tableInfoReq.setTenantName(tenantName);
                Long count = 0L;//给大整数赋初值为0
                try {
                    List <TableUsageProfileGroupByUser> profiles = iTableInfoService.tableProfileInfo(tableInfoReq, 30);
                    if (profiles != null && !profiles.isEmpty()) {
                        for (TableUsageProfileGroupByUser profile : profiles) {
                            if (profile == null) {
                                continue;
                            }
                            count += profile.getSumCount().longValue();
                            LastActivityInfo lastActivityInfo = new LastActivityInfo();
                            lastActivityInfo.setRegion(region);
                            lastActivityInfo.setDbName(table.getDatabaseName());
                            lastActivityInfo.setTableName(table.getTableName());
                            lastActivityInfo.setSole(region + "." + table.getDatabaseName() + "." + table.getTableName());
                            lastActivityInfo.setUserId(profile.getUserId());
                            lastActivityInfo.setSumCount(profile.getSumCount());
                            lastActivityInfo.setAvgCount(profile.getAvgCount());
                            lastActivityInfo.setRecentlyVisitedTimestamp(DateUtil.getDateToString(profile.getRecentlyVisitedTimestamp()));
                            lastActivityInfoList.add(lastActivityInfo);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                tableInfo.setLastActivityCount(count);
                LakecatTableNameSet.add(tableName);

                if (nameAndIdMap.get("new").containsKey(tableName)) {
                    tableListForUpdate.add(tableInfo);
                } else {
                    tableListForSave.add(tableInfo);
                }
            }

            synchronized (SyncThread.class) {
                if (!tableListForUpdate.isEmpty()) {
                    try {
                        tableInfoMapper.batchUpdate(tableListForUpdate);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("更新失败");
                        for (TableInfo tableInfo : tableListForUpdate) {
                            System.out.println(tableInfo);
                        }
                    }

                }

                if (!tableListForSave.isEmpty()) {
                    try {
                        tableInfoMapper.batchSave(tableListForSave);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("插入失败");
                        for (TableInfo tableInfo : tableListForSave) {
                            System.out.println(tableInfo);
                        }
                    }
                }


                if (!lastActivityInfoList.isEmpty()) {
                    try {
                        tableInfoMapper.batchSaveForUsers(lastActivityInfoList);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("最新插入失败");
                        for (LastActivityInfo lastActivityInfo : lastActivityInfoList) {
                            System.out.println(lastActivityInfo);
                        }
                    }
                }
            }
            return 0;
        }, poolNext).whenCompleteAsync((s, e) -> {
            if (e != null) {
                log.warn("Detected a write failure:{}", e);
            }

        }, poolNext);
    }


    public void doSyncByDatabaseSingle(List <Table> tableListBySDK,
                                                        String region, Map <String, Map <String, Long>> nameAndIdMap,
                                                        CopyOnWriteArraySet <String> LakecatTableNameSet, String tenantName) {

        List <TableInfo> tableListForUpdate = new ArrayList <>();
        List <TableInfo> tableListForSave = new ArrayList <>();
        List <LastActivityInfo> lastActivityInfoList = new ArrayList <>();
        for (Table table : tableListBySDK) {
            Map <String, String> properties = table.getParameters();
            String transientLastDdlTime = properties.get("transient_lastDdlTime");
            TableInfo tableInfo = new TableInfo();
            tableInfo.setDbName(table.getDatabaseName());
            tableInfo.setName(table.getTableName());
            tableInfo.setOwner(table.getOwner());
            if (transientLastDdlTime != null) {
                try {
                    tableInfo.setTransientLastDdlTime(DateUtil.getDateToString(Long.parseLong(transientLastDdlTime) * 1000));
                } catch (Exception e) {
                    System.out.println(transientLastDdlTime);
                }
            }
            tableInfo.setColumns(transColumnOutPut(table.getFields()));
            List <Column> partitions = table.getPartitionKeys();
            tableInfo.setPartitionKeys(transColumnOutPut(partitions));
            if (!partitions.isEmpty()) {
                tableInfo.setPartitionType(1);
            } else {
                tableInfo.setPartitionType(0);
            }
            tableInfo.setDescription(table.getParameters().getOrDefault("comment", ""));
            tableInfo.setCreateTime(DateUtil.getDateToString(table.getCreateTime()));
            StorageDescriptor tableStorage = table.getStorageDescriptor();
            String location = tableStorage.getLocation();
            tableInfo.setLocation(location);
            try {
                tableInfo.setRegion(CatalogNameEnum.getRegion(table.getCatalogName()));
            } catch (Exception e) {
                log.error("",e);
                continue;
            }
            tableInfo.setType(LakecatTableUtils.getTableType(table));
            tableInfo.setSdFileFormat(tableStorage.getSourceShortName());
            String tableName = tableInfo.getRegion() + "." + table.getDatabaseName() + "." + table.getTableName();
            TableInfoReq tableInfoReq = new TableInfoReq();
            tableInfoReq.setRegion(tableInfo.getRegion());
            tableInfoReq.setDatabaseName(table.getDatabaseName());
            tableInfoReq.setTableName(table.getTableName());
            tableInfoReq.setTenantName(tenantName);
            Long count = 0L;//给大整数赋初值为0
            try {
                List <TableUsageProfileGroupByUser> profiles = iTableInfoService.tableProfileInfo(tableInfoReq, 30);
                if (profiles != null && !profiles.isEmpty()) {
                    for (TableUsageProfileGroupByUser profile : profiles) {
                        if (profile == null) {
                            continue;
                        }
                        count += profile.getSumCount().longValue();
                        LastActivityInfo lastActivityInfo = new LastActivityInfo();
                        lastActivityInfo.setRegion(region);
                        lastActivityInfo.setDbName(table.getDatabaseName());
                        lastActivityInfo.setTableName(table.getTableName());
                        lastActivityInfo.setUserId(profile.getUserId());
                        lastActivityInfo.setSole(region + "." + table.getDatabaseName() + "." + table.getTableName());
                        lastActivityInfo.setSumCount(profile.getSumCount());
                        lastActivityInfo.setAvgCount(profile.getAvgCount());
                        lastActivityInfo.setRecentlyVisitedTimestamp(DateUtil.getDateToString(profile.getRecentlyVisitedTimestamp()));
                        lastActivityInfoList.add(lastActivityInfo);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            tableInfo.setLastActivityCount(count);
            LakecatTableNameSet.add(tableName);

            if (nameAndIdMap.get("new").containsKey(tableName)) {
                tableListForUpdate.add(tableInfo);
            } else {
                tableListForSave.add(tableInfo);
            }
        }

        synchronized (SyncThread.class) {
            if (!tableListForUpdate.isEmpty()) {
                try {
                    tableInfoMapper.batchUpdate(tableListForUpdate);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("更新失败");
                    for (TableInfo tableInfo : tableListForUpdate) {
                        System.out.println(tableInfo);
                    }
                }

            }

            if (!tableListForSave.isEmpty()) {
                try {
                    tableInfoMapper.batchSave(tableListForSave);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("插入失败");
                    for (TableInfo tableInfo : tableListForSave) {
                        System.out.println(tableInfo);
                    }
                }
            }


            if (!lastActivityInfoList.isEmpty()) {
                try {
                    tableInfoMapper.batchSaveForUsers(lastActivityInfoList);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("最新插入失败");
                    for (LastActivityInfo lastActivityInfo : lastActivityInfoList) {
                        System.out.println(lastActivityInfo);
                    }
                }
            }
        }
    }


    private List <Table> getTableListBySDK(LakeCatClient lakeCatClient, Database database, String tenantName) {
        List <Table> all = new ArrayList <>();
        String databaseName = String.valueOf(database.getDatabaseName());
        ListTablesRequest listTablesRequest = new ListTablesRequest();
        listTablesRequest.setCatalogName(database.getCatalogName());
        listTablesRequest.setDatabaseName(databaseName);
        listTablesRequest.setProjectId(tenantName);
        listTablesRequest.setMaxResults(1000);
        String pageToken = "";
        int i = 0;
        while (pageToken != null) {
            if (!"".equals(pageToken)) {
                listTablesRequest.setNextToken(pageToken);
            }
            try {
                PagedList <Table> tablePagedList = lakeCatClient.listTables(listTablesRequest);
                Table[] tables = tablePagedList.getObjects();
                Collections.addAll(all, tables);
                pageToken = tablePagedList.getNextMarker();
                i++;

            } catch (Exception e) {
                System.out.println("报错的信息" + databaseName);
                e.printStackTrace();
            }

        }
        return all;
    }

    private Map <String, Map <String, Long>> getTableNameAndIdMap() {
        Map <String, Map <String, Long>> result = new HashMap <>();
        List <TableInfo> nameAndIdMapByRegion = tableInfoMapper.getNameAndIdMapByRegion();
        HashMap <String, Long> newMap = new HashMap <>();
        HashMap <String, Long> oldMap = new HashMap <>();
        for (TableInfo m : nameAndIdMapByRegion) {
            if (newMap.containsKey(m.getName())) {
                oldMap.put(m.getId() + "", m.getId());
                continue;
            }
            newMap.put(m.getName(), m.getId());
        }

        result.put("new", newMap);
        result.put("old", oldMap);
        return result;
    }


    private String transColumnOutPut(List <Column> columnOutput) {
        try {
            List <JSONObject> Column = new ArrayList <>();
            for (Column output : columnOutput) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", output.getColumnName());
                jsonObject.put("type", LakecatTableUtils.lmsDataTypeTohmsDataType(output.getColType()));
                jsonObject.put("comment", output.getComment());
                Column.add(jsonObject);
            }
            return Column.toString();
        }catch (Exception e){

        }
        return "";
    }

}
