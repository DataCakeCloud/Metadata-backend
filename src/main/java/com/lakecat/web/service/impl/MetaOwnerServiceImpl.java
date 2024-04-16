package com.lakecat.web.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.entity.*;
import com.lakecat.web.entity.owner.ActiveTable;
import com.lakecat.web.entity.owner.MetadataIntegrity;
import com.lakecat.web.entity.owner.OwnerTableIntegrity;
import com.lakecat.web.entity.owner.ResourceUsage;
import com.lakecat.web.entity.owner.TableIntegrity;
import com.lakecat.web.entity.owner.TableStorage;
import com.lakecat.web.entity.owner.TableUsageProfile;
import com.lakecat.web.mapper.GovMapper;
import com.lakecat.web.mapper.LastActivityMapper;
import com.lakecat.web.mapper.TableInfoMapper;
import com.lakecat.web.mapper.TableStorageInfoMapper;
import com.lakecat.web.service.*;
import com.lakecat.web.utils.CacheUtils;
import com.lakecat.web.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MetaOwnerServiceImpl implements IMetaOwnerService {

    @Autowired
    LastActivityMapper lastActivityMapper;

    @Autowired
    TableStorageInfoMapper tableStorageInfoMapper;

    @Autowired
    IGovService govService;

    @Autowired
    TableInfoMapper tableInfoMapper;

    @Autowired
    ITableInfoService tableInfoService;


    @Autowired
    ITableProfileService tableProfileService;

    @Autowired
    ILakeCatClientService iLakeCatClientService;

    @Autowired
    @Qualifier("threadPool")
    private ExecutorService executorService;

    /**
     * 僵尸数据，非活跃天数
     */
    private static final Integer nonActiveInterval = 30;

    private static ExpiringMap<String, Object> cacheMap = CacheUtils.getCacheMap(600, 60 * 60 * 3, String.class, Object.class);
    /**
     * 元数据完整读缓存
     */
    private static ExpiringMap<String, OwnerTableIntegrity> metadataCacheMapIntegrity = CacheUtils.getCacheMap(20000, 60 * 60 * 6, String.class, OwnerTableIntegrity.class);

    private static final String tableCountKey = "table_count";
    private static final String tableTimesKey = "table_times";
    private static final String cacheActiveTablePrefix = "active_table_";
    private static final String cacheTableUsageProfilePrefix = "table_usage_profile_";
    private static final String cacheTableStoragePrefix = "table_storage_";
    private static final String cacheResourceUsagePrefix = "resource_usage_";
    private static final String cacheMetadataIntegrityPrefix = "metadata_integrity_";

    private static final Set<String> invalidOwnerName = new HashSet<>(Arrays.asList("test", "root"));


    @Override
    public ActiveTable activeTable(String owner) {
        try {
            String uniqueKey = cacheActiveTablePrefix+owner;
            if (!cacheMap.containsKey(uniqueKey)) {
                if (!cacheMap.containsKey(tableCountKey)) {
                    setAndGetActiveTableStat();
                }
                setAndGetOwnerActiveTableStat(owner);
            }
            return (ActiveTable) cacheMap.get(uniqueKey);
        } catch (Exception e) {
            log.warn("Exception: {}", e.getMessage());
            return new ActiveTable();
        }
    }

    private void setAndGetActiveTableStat() {
        Map<String, Double> sumActiveTableStat = lastActivityMapper.sumActiveTableStatNew();
        Double tableCount = formatValue(sumActiveTableStat, "table_count");
        Double tableTimes = formatValue(sumActiveTableStat, "table_times");
        cacheMap.put(tableCountKey, tableCount);
        cacheMap.put(tableTimesKey, tableTimes);
    }

    private void setAndGetOwnerActiveTableStat(String owner) {
        Map<String, Double> sumActiveTableStat = lastActivityMapper.ownerActiveTableStatNew(owner);
        double tableCount = formatValue(sumActiveTableStat, "table_count");
        //Double tableTimes = sumActiveTableStat.get("table_times");
        ActiveTable table = new ActiveTable();
        TableUsageProfile tableUsageProfile = new TableUsageProfile();
        table.setTableCount((Double) cacheMap.get(tableCountKey));
        table.setOwnerTableCount(tableCount);
        table.setOwnerTableRatio(MathUtils.formatDouble(table.getOwnerTableCount()/table.getTableCount(), 4));
        /*tableUsageProfile.setPlatformVisitFrequency((Double) cacheMap.get(tableTimesKey));
        tableUsageProfile.setOwnerVisitFrequency(tableTimes);
        tableUsageProfile.setOwnerVisitFrequencyRatio(MathUtils.formatDouble(tableUsageProfile.getOwnerVisitFrequency()/tableUsageProfile.getPlatformVisitFrequency(), 4));*/
        double ownerVisitActivity = formatValue(sumActiveTableStat, "table_times_avg") / 100;
        tableUsageProfile.setOwnerVisitActivity(ownerVisitActivity >= 1 ? 1 : MathUtils.formatDouble(ownerVisitActivity, 4));
        tableUsageProfile.setPlatformVisitActivity(0.957);
        cacheMap.put(cacheActiveTablePrefix+owner, table);
        cacheMap.put(cacheTableUsageProfilePrefix+owner, tableUsageProfile);
    }

    @Override
    public TableStorage tableStorage(String owner) {
        try {
            String uniqueKey = cacheTableStoragePrefix+owner;
            if (!cacheMap.containsKey(uniqueKey)) {
                TableStorage tableStorage = new TableStorage();
                Map<String, Double> storageInfoMapperOwnerZombieTables = tableStorageInfoMapper.getOwnerZombieTables(owner);
                tableStorage.setTableStorageSize(formatValue(storageInfoMapperOwnerZombieTables, "total_storage"));
                tableStorage.setOwnerZombieDataSize(formatValue(storageInfoMapperOwnerZombieTables, "zombie_size"));
                if (tableStorage.getTableStorageSize() > 0) {
                    tableStorage.setOwnerZombieDataSizeRatio(MathUtils.formatDouble(tableStorage.getOwnerZombieDataSize()/tableStorage.getTableStorageSize(), 4));
                }
                cacheMap.put(uniqueKey, tableStorage);
            }
            return (TableStorage) cacheMap.get(uniqueKey);
        } catch (Exception e) {
            log.warn("Exception: {}", e.getMessage());
            return new TableStorage();
        }
    }

    @Override
    public ResourceUsage resourceUsage(String owner) {
        try {
            String uniqueKey = cacheResourceUsagePrefix+owner;
            if (!cacheMap.containsKey(uniqueKey)) {
                Map<String, Double> ownerSummaryStatistics = govService.getOwnerSummaryStatistics(owner);
                ResourceUsage resourceUsage = new ResourceUsage();
                resourceUsage.setOwnerTaskCount(formatValue(ownerSummaryStatistics, "task_count"));
                resourceUsage.setAvgCpuUsed(formatValue(ownerSummaryStatistics, "cpu_use_ratio"));
                resourceUsage.setAvgMemoryUsed(formatValue(ownerSummaryStatistics, "mem_use_ratio"));
                cacheMap.put(uniqueKey, resourceUsage);
            }
            return (ResourceUsage) cacheMap.get(uniqueKey);
        } catch (Exception e) {
            log.warn("Exception: {}", e.getMessage());
            return new ResourceUsage();
        }

    }

    private double formatValue(Map<String, Double> ownerSummaryStatistics, String key) {
        if (ownerSummaryStatistics == null || !ownerSummaryStatistics.containsKey(key)) {
            return 0;
        }
        return MathUtils.formatDouble(Double.parseDouble(String.valueOf(ownerSummaryStatistics.get(key))), 4);
    }

    @Override
    public TableUsageProfile tableUsageProfile(String owner) {
        try {
            String uniqueKey = cacheTableUsageProfilePrefix+owner;
            if (!cacheMap.containsKey(uniqueKey)) {
                if (!cacheMap.containsKey(tableTimesKey)) {
                    setAndGetActiveTableStat();
                }
                setAndGetOwnerActiveTableStat(owner);
            }
            return (TableUsageProfile) cacheMap.get(uniqueKey);
        } catch (Exception e) {
            log.warn("Exception: {}", e.getMessage());
            return new TableUsageProfile();
        }
    }

    @Override
    public MetadataIntegrity metadataIntegrity(String owner) {
        try {
            String uniqueKey = cacheMetadataIntegrityPrefix+owner;
            if (!metadataCacheMapIntegrity.containsKey(uniqueKey)) {
                refreshMetadataIntegrity(owner);
            }
            OwnerTableIntegrity integrity = metadataCacheMapIntegrity.get(uniqueKey);
            MetadataIntegrity metadataIntegrity = new MetadataIntegrity();
            if (integrity != null) {
                metadataIntegrity.setOwnerIntegrity(integrity.getIntegrityRatio());
            }
            metadataIntegrity.setPlatformIntegrity(0.81);
            return metadataIntegrity;
        } catch (Exception e) {
            log.warn("Exception: {}", e.getMessage());
            return new MetadataIntegrity();
        }
    }

    @Override
    public void refreshMetadataIntegrity(String owner) {
        LakeCatParam lakeCatParam = LakeCatParam.builder().owner(owner).build();
        List<TableInfo> tableInfos = iLakeCatClientService.searchTable(lakeCatParam);
        if (owner == null) {
            metadataCacheMapIntegrity.clear();
        }

        log.info("table size: {}", tableInfos.size());
        AtomicInteger atomicInteger = new AtomicInteger(0);
        if (CollectionUtils.isNotEmpty(tableInfos)) {
            CurrentUser currentUser = InfTraceContextHolder.get().getUserInfo();
            for (TableInfo tableInfo : tableInfos) {
                try {
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            InfTraceContextHolder.get().setUserInfo(currentUser);
                            InfTraceContextHolder.get().setTenantName(currentUser.getTenantName());
//                        updateMetadataIntegrityCache(id);
                            updateMetadataIntegrityCache(tableInfo);
                        }
                    });
                } catch (Exception e) {
                    log.info("refresh fail table is :" + tableInfo.getRegion() + "." + tableInfo.getDbName() + "." + tableInfo.getTableName());
                }
                atomicInteger.incrementAndGet();
                log.info("atomicInteger value is :" + atomicInteger.get());
            }
            while (atomicInteger.get() < tableInfos.size()) {
                log.info("Waiting refresh metadata integrity... percent complete: {}%", MathUtils.formatDouble(atomicInteger.get() * 100.0 / tableInfos.size(), 2));
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            /**
             * 观察 分位值数据使用
             */
            Collection<OwnerTableIntegrity> values = metadataCacheMapIntegrity.values();
            List<OwnerTableIntegrity> collect = values.stream().sorted(new Comparator<OwnerTableIntegrity>() {
                @Override
                public int compare(OwnerTableIntegrity o1, OwnerTableIntegrity o2) {
                    return o1.getIntegrityRatio() > o2.getIntegrityRatio() ? 1 : 0;
                }
            }).collect(Collectors.toList());
            OwnerTableIntegrity oti;
            for (int i = 0; i < collect.size(); i++) {
                oti = collect.get(i);
                System.out.printf("%s,%f,%f,%f%n", i, oti.getCount(), oti.getSumIntegrity(), oti.getIntegrityRatio());
            }
        }


    }

    private void updateMetadataIntegrityCache(Long id) {
        TableInfo tableInfo = tableInfoService.getTableInfoIfNotPresent(id, null);
        double integrityValue = 0;
        if (tableInfo != null) {
            integrityValue += tableInfo.getInterval() != null ? 0.3 : 0;
            integrityValue += tableInfo.getDescription() != null ? 0.05 : 0;
            integrityValue += tableInfo.getCnName() != null ? 0.05 : 0;
            boolean hasOwnerFlag =
                StringUtils.isNotBlank(tableInfo.getOwner()) && !invalidOwnerName.contains(tableInfo.getOwner());
            integrityValue += hasOwnerFlag ? 0.2 : 0;
            integrityValue += checkTableVisit(tableInfo) ? 0.3 : 0;
            integrityValue += MathUtils.formatDouble(getColumnsIntegrityValue(tableInfo) * 0.1, 4);
            String uniqueKey = cacheMetadataIntegrityPrefix + tableInfo.getOwner();
            if (metadataCacheMapIntegrity.containsKey(uniqueKey)) {
                OwnerTableIntegrity integrity = metadataCacheMapIntegrity.get(uniqueKey);
                integrity.setCount(integrity.getCount() + 1);
                integrity.setSumIntegrity(integrity.getSumIntegrity() + integrityValue);
                integrity.setIntegrityRatio(
                    MathUtils.formatDouble(integrity.getSumIntegrity() / integrity.getCount(), 4));
            } else {
                OwnerTableIntegrity tableIntegrity = new OwnerTableIntegrity();
                tableIntegrity.setOwner(tableInfo.getOwner());
                tableIntegrity.setIntegrityRatio(integrityValue);
                tableIntegrity.setCount(1);
                tableIntegrity.setSumIntegrity(integrityValue);
                metadataCacheMapIntegrity.put(uniqueKey, tableIntegrity);
            }
            log.info("{}.{}.{}", tableInfo.getRegion(), tableInfo.getDbName(), tableInfo.getName());
        }
    }

    private void updateMetadataIntegrityCache(TableInfo tab) {
//        TableInfo tableInfo = tableInfoService.getTableInfoIfNotPresent(id, null);
        LakeCatParam build = LakeCatParam.builder().dbName(tab.getDbName()).tableName(tab.getTableName())
                .region(tab.getRegion()).build();
        TableInfo tableInfo = iLakeCatClientService.getTable(build);
        double integrityValue = 0;
        if (tableInfo != null) {
            integrityValue += tableInfo.getInterval() != null ? 0.3 : 0;
            integrityValue += tableInfo.getDescription() != null ? 0.05 : 0;
            integrityValue += tableInfo.getCnName() != null ? 0.05 : 0;
            boolean hasOwnerFlag =
                    StringUtils.isNotBlank(tableInfo.getOwner()) && !invalidOwnerName.contains(tableInfo.getOwner());
            integrityValue += hasOwnerFlag ? 0.2 : 0;
            integrityValue += checkTableVisit(tableInfo) ? 0.3 : 0;
            integrityValue += MathUtils.formatDouble(getColumnsIntegrityValue(tableInfo) * 0.1, 4);
            String uniqueKey = cacheMetadataIntegrityPrefix + tableInfo.getOwner();
            if (metadataCacheMapIntegrity.containsKey(uniqueKey)) {
                OwnerTableIntegrity integrity = metadataCacheMapIntegrity.get(uniqueKey);
                integrity.setCount(integrity.getCount() + 1);
                integrity.setSumIntegrity(integrity.getSumIntegrity() + integrityValue);
                integrity.setIntegrityRatio(
                        MathUtils.formatDouble(integrity.getSumIntegrity() / integrity.getCount(), 4));
            } else {
                OwnerTableIntegrity tableIntegrity = new OwnerTableIntegrity();
                tableIntegrity.setOwner(tableInfo.getOwner());
                tableIntegrity.setIntegrityRatio(integrityValue);
                tableIntegrity.setCount(1);
                tableIntegrity.setSumIntegrity(integrityValue);
                metadataCacheMapIntegrity.put(uniqueKey, tableIntegrity);
            }
            log.info("{}.{}.{}", tableInfo.getRegion(), tableInfo.getDbName(), tableInfo.getName());
        }
    }


    private double getColumnsIntegrityValue(TableInfo tableInfo) {
        JSONArray colsJson = JSONArray.parseArray(tableInfo.getColumns());
        double hasCommentCount = 0;
        String name;
        for (int i = 0; i < colsJson.size(); i++) {
            JSONObject jsonObject = colsJson.getJSONObject(i);
            name = jsonObject.getString("comment");
            if (StringUtils.isNotEmpty(name)) {
                hasCommentCount += 1;
            }
        }

        return hasCommentCount/colsJson.size();
    }

    private boolean checkTableVisit(TableInfo tableInfo) {
        LastActivityInfo lastActivityInfo = new LastActivityInfo();
        lastActivityInfo.setRegion(tableInfo.getRegion());
        lastActivityInfo.setTableName(tableInfo.getName());
        lastActivityInfo.setDbName(tableInfo.getDbName());
        List<String> list = lastActivityMapper.search(lastActivityInfo);
        return CollectionUtils.isNotEmpty(list);
    }
}
