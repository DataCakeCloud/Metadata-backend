package com.lakecat.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lakecat.web.config.GlobalConfig;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.entity.CurrentUser;
import com.lakecat.web.entity.InfTraceContextHolder;
import com.lakecat.web.entity.table.TableStorageInfo;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.mapper.TableStorageInfoMapper;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.service.impl.TableStorageInfoServicImpl;
import com.lakecat.web.utils.MD5Utill;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.exception.CatalogException;
import io.lakecat.catalog.common.plugin.request.ListTablePartitionsRequest;
import io.lakecat.catalog.common.plugin.request.input.PartitionFilterInput;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
//@IdentityAuth
@Api(tags = "表存储信息")
@RequestMapping("/metadata/tableStorageInfo")
public class TableStorageInfoController {


    @Autowired
    TableStorageInfoServicImpl tableStorageInfoServicImpl;

    @Autowired
    ILakeCatClientService iLakeCatClientService;

    @Autowired
    GlobalConfig globalConfig;

    @Autowired
    CatalogNameEnum CatalogNameEnum;


    @Autowired
    TableStorageInfoMapper tableStorageInfoMapper;


    @ApiOperation(value = "表存储信息详情", produces = "application/json;charset=UTF-8")
    @GetMapping(value = "/getAwsTableStorageDetails")
    public Response<List<TableStorageInfo>> getAwsTableStorageDetails() throws BusinessException, InterruptedException {
        ArrayList<TableStorageInfo> storageInfos = new ArrayList<>();
        String regionName = "ue1";
        String providerName = "AWS";
        String storageType = "S3";
        TableStorageInfo storageTableInfo = tableStorageInfoServicImpl.getStorageTableInfo(regionName, providerName, storageType);
        storageInfos.add(storageTableInfo);
        return Response.success(storageInfos);
    }

    @ApiOperation(value = "表存储信息详情", produces = "application/json;charset=UTF-8")
    @GetMapping(value = "/getHwTableStorageDetails")
    public Response<List<TableStorageInfo>> getHwTableStorageDetails() throws BusinessException, InterruptedException {
        ArrayList<TableStorageInfo> storageInfos = new ArrayList<>();
        String regionName = "sg2";
        String providerName = "HW";
        String storageType = "OBS";
        TableStorageInfo storageTableInfo = tableStorageInfoServicImpl.getStorageTableInfo(regionName, providerName, storageType);
        storageInfos.add(storageTableInfo);
        return Response.success(storageInfos);
    }

    @ApiOperation(value = "表存储分区信息详情", produces = "application/json;charset=UTF-8")
    @GetMapping(value = "/getTablePartitionSize")
    public Response<List<TableStorageInfo>> getTablePartitionSize(
            @ApiParam(name = "dt", value = "天", required = true)
            @RequestParam String dt
    ) {

        LakeCatClient lakeCatClient = iLakeCatClientService.get();
        ArrayList<TableStorageInfo> tableStorageInfoArrayList = new ArrayList<>();
        List<TableStorageInfo> allLatestRecord = tableStorageInfoMapper.getAllLatestRecord2(dt);
        int size1 = allLatestRecord.size();
        updateTableStorageInfoAddPartitonNameAndSize(dt, lakeCatClient, tableStorageInfoArrayList, allLatestRecord, size1);
        return Response.success(tableStorageInfoArrayList, tableStorageInfoArrayList.size());
    }

    private void updateTableStorageInfoAddPartitonNameAndSize(String dt, LakeCatClient lakeCatClient, ArrayList<TableStorageInfo> tableStorageInfoArrayList, List<TableStorageInfo> allLatestRecord, int size1) {
        // 开始时间
        long stime = System.currentTimeMillis();

        for (int i = 0; i < size1; i++) {
            TableStorageInfo line = allLatestRecord.get(i);
            String dbName = line.getDbName();

            String region = line.getRegion();
            System.out.println("region:" + region);
            String catalogName;

            switch (region) {
                case "ue1":
                    catalogName = CatalogNameEnum.getCatalogName("ue1");
                    break;
                case "sg1":
                    catalogName = CatalogNameEnum.getCatalogName("sg1");
                    break;
                case "sg2":
                    catalogName = CatalogNameEnum.getCatalogName("sg2");
                    break;
                case "MY":
                    catalogName = CatalogNameEnum.getCatalogName("sg2");
                    break;
                default:
                    throw new RuntimeException("unsupported region please recheck!");
            }
            String tbName = line.getTableName();
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            ListTablePartitionsRequest request = new ListTablePartitionsRequest();
            request.setProjectId(tenantName);
            request.setCatalogName(catalogName);
            request.setDatabaseName(dbName);
            request.setTableName(tbName);
            PartitionFilterInput partitionFilterInput = new PartitionFilterInput();
            partitionFilterInput.setMaxParts((short) -1);
            request.setInput(partitionFilterInput);


            List<String> listPartitionNames;
            try {
                listPartitionNames = lakeCatClient.listPartitionNames(request);
            } catch (CatalogException e) {
                continue;
            }
            int size = listPartitionNames.size();
            log.info("current database is {},table is {},partition size is {},num is {}", dbName, tbName, size, i);
            TableStorageInfo tableStorageInfo;
            if (size == 0) {
                tableStorageInfo = TableStorageInfo
                        .builder()
                        .dt(line.getDt())
                        .id(line.getId())
                        .tableName(line.getTableName())
                        .dbName(line.getDbName())
                        .tableObjectNum(line.getTableObjectNum())
                        .tableSmallObjectNum(line.getTableSmallObjectNum())
                        .tablePartitionNum(1)
                        .tableBucketName(line.getTableBucketName())
                        .totalStorage(line.getTotalStorage())
                        .storageType(line.getStorageType())
                        .storageFileFormat(line.getStorageFileFormat())
                        .location(line.getLocation())
                        .smallFileAdvice(line.getSmallFileAdvice())
                        .smallFileWorkbenchUrl(line.getSmallFileWorkbenchUrl())
                        .region(line.getRegion())
                        .provider(line.getProvider())
                        .owner(line.getOwner())
                        .tableStandardSize(line.getTableStandardSize())
                        .tableIntelligentSize(line.getTableIntelligentSize())
                        .tableDeepSize(line.getTableDeepSize())
                        .tableArchiveSize(line.getTableArchiveSize())
                        .tableSmallObjectTotalSize(line.getTableSmallObjectTotalSize())
                        .isUpdate("1")
                        .partitionNames("")
                        .build();
            } else {
                String partitonNames = MD5Utill.getPartitonNames(listPartitionNames.get(0));
                tableStorageInfo = TableStorageInfo
                        .builder()
                        .dt(line.getDt())
                        .id(line.getId())
                        .tableName(line.getTableName())
                        .dbName(line.getDbName())
                        .tableObjectNum(line.getTableObjectNum())
                        .tableSmallObjectNum(line.getTableSmallObjectNum())
                        .tablePartitionNum(size)
                        .tableBucketName(line.getTableBucketName())
                        .totalStorage(line.getTotalStorage())
                        .storageType(line.getStorageType())
                        .storageFileFormat(line.getStorageFileFormat())
                        .location(line.getLocation())
                        .smallFileAdvice(line.getSmallFileAdvice())
                        .smallFileWorkbenchUrl(line.getSmallFileWorkbenchUrl())
                        .region(line.getRegion())
                        .provider(line.getProvider())
                        .owner(line.getOwner())
                        .tableStandardSize(line.getTableStandardSize())
                        .tableIntelligentSize(line.getTableIntelligentSize())
                        .tableDeepSize(line.getTableDeepSize())
                        .tableArchiveSize(line.getTableArchiveSize())
                        .tableSmallObjectTotalSize(line.getTableSmallObjectTotalSize())
                        .isUpdate("1")
                        .partitionNames(partitonNames)
                        .build();
            }
            tableStorageInfoArrayList.add(tableStorageInfo);
            QueryWrapper<TableStorageInfo> queryWrapper = new QueryWrapper<TableStorageInfo>();

            queryWrapper.eq("dt", dt);
            queryWrapper.eq("id", line.getId());
            tableStorageInfoMapper.update(tableStorageInfo,queryWrapper);
        }
        // 结束时间
        long etime = System.currentTimeMillis();
        // 计算执行时间
        log.info("执行时长：{}分钟", (etime - stime)/1000/60);
    }

}
