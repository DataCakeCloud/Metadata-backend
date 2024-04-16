package com.lakecat.web.service.impl;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lakecat.web.config.GlobalConfig;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.entity.LakeCatParam;
import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.entity.table.TableStorageInfo;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.mapper.TableInfoMapper;
import com.lakecat.web.mapper.TableStorageInfoMapper;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.service.ITableStorageInfoService;
import com.obs.services.ObsClient;
import com.obs.services.model.ListObjectsRequest;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.exception.CatalogException;
import io.lakecat.catalog.common.plugin.request.ListTablePartitionsRequest;
import io.lakecat.catalog.common.plugin.request.input.PartitionFilterInput;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
@Service
@Component
public class TableStorageInfoServicImpl extends ServiceImpl<TableStorageInfoMapper, TableStorageInfo> implements ITableStorageInfoService {


    /**
     * 固定的线程池（当前线程池大小为5）
     */
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);
    @Autowired
    TableInfoMapper tableInfoMapper;
    @Autowired
    ITableStorageInfoService iTableStorageInfoService;
    @Autowired
    TableStorageInfoMapper tableStorageInfoMapper;
    @Autowired
    ILakeCatClientService iLakeCatClientService;
    @Autowired
    GlobalConfig globalConfig;
    @Autowired
    CatalogNameEnum CatalogNameEnum;

    @Value(value = "${hw.ak}")
    String hwAk;
    @Value(value = "${hw.sk}")
    String hwSk;
    @Value(value = "${hw.endPoint}")
    String hwEndPoint;
    @Value("${aw.accessKeyID}")
    String awAccessKeyID;
    @Value(value = "${aw.secretKey}")
    String awSecretKey;

    private HashMap getHwObjectStorageInfo(String bucketName, String keyName, String region) throws InterruptedException {
        HashMap partitionStorageMap = new HashMap();

        String endPoint = hwEndPoint
                ;
        String ak = hwAk;
        String sk = hwSk;
        // 创建ObsClient实例
        ObsClient obsClient = new ObsClient(ak, sk, endPoint);

        /** 固定的线程池（当前线程池大小为5） */
        ExecutorService executor = Executors.newFixedThreadPool(5);


        LongAdder currentPartitionObjectNum = new LongAdder();
        LongAdder currentPartitionSmallObjectNum = new LongAdder();
        LongAdder currentPartitionSize = new LongAdder();


        CountDownLatch cdl = new CountDownLatch(obsClient.listObjects(new ListObjectsRequest(bucketName, keyName, "0", "/", 1000000000)).getObjects().size());

        executor.submit(new Runnable() {
            @Override
            public void run() {
                obsClient.listObjects(new ListObjectsRequest(bucketName, keyName, "0", "/", 1000000000)).getObjects().forEach(line -> {
                    Long length = line.getMetadata().getContentLength();
                    currentPartitionObjectNum.increment();
                    currentPartitionSize.add(length);
                    if (length / 1024 / 1024 < 64) {
                        currentPartitionSmallObjectNum.increment();
                    }

                    cdl.countDown();

                });
            }
        });

        cdl.await();
        //关闭线程池
        executor.shutdown();


        partitionStorageMap.put("currentPartitionObjectNum", currentPartitionObjectNum);
        partitionStorageMap.put("currentPartitionSmallObjectNum", currentPartitionSmallObjectNum);
        partitionStorageMap.put("currentPartitionSize", currentPartitionSize);

        System.out.println("当前分区计算完成：" + keyName);

        return partitionStorageMap;
    }

    private HashMap getAwsObjectStorageInfo(String bucketName, String keyName, String region) throws InterruptedException, IOException {
        HashMap partitionStorageMap = new HashMap();
        if (region.equals("ue1")) {
            region = "us-east-1";
        }
        AmazonS3 awsS3Client = getAwsS3Client(region);

        /** 固定的线程池（当前线程池大小为5） */
        ExecutorService executor = Executors.newFixedThreadPool(5);

        LongAdder currentPartitionObjectNum = new LongAdder();
        LongAdder currentPartitionSmallObjectNum = new LongAdder();
        LongAdder currentPartitionSize = new LongAdder();

        CountDownLatch cdl = new CountDownLatch(awsS3Client.listObjects(bucketName, keyName).getObjectSummaries().size());

        executor.submit(new Runnable() {
            @Override
            public void run() {
                awsS3Client.listObjects(bucketName, keyName)
                        .getObjectSummaries().parallelStream().parallel().forEach(line -> {
                    long size = line.getSize();
                    currentPartitionObjectNum.increment();
                    currentPartitionSize.add(size);
                    if (size / 1024 / 1024 < 64) {
                        currentPartitionSmallObjectNum.increment();
                    }

                    cdl.countDown();

                });
            }
        });
        cdl.await();
        //关闭线程池
        executor.shutdown();


        partitionStorageMap.put("currentPartitionObjectNum", currentPartitionObjectNum);
        partitionStorageMap.put("currentPartitionSmallObjectNum", currentPartitionSmallObjectNum);
        partitionStorageMap.put("currentPartitionSize", currentPartitionSize);

        System.out.println("当前分区计算完成：" + keyName);

        return partitionStorageMap;

    }

    public AmazonS3 getAwsS3Client(String region) throws IllegalArgumentException {
        String accessKeyID = awAccessKeyID;
        String secretKey = awSecretKey;
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyID, secretKey);
        AmazonS3 s3 = null;
        try {
            s3 = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(region)
                    .build();
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
        }
        return s3;
    }

    @Override
    public TableStorageInfo getStorageTableInfo(String regionName, String providerName, String storageType) throws BusinessException, InterruptedException {
//        sg2
//        ue1
        //        List<Map<String, String>> allTables = tableInfoMapper.getAllTables(regionName);

        LakeCatParam lakeCatParam = LakeCatParam.builder().region(regionName).size(100).build();
        List<TableInfo> tableInfos = iLakeCatClientService.searchTable(lakeCatParam);
        ArrayList<TableStorageInfo> tableStorageInfos = new ArrayList<>();
        CountDownLatch cdl = new CountDownLatch(100);
        System.out.println("====== 线程开始 =====");
        LakeCatClient lakeCatClient = iLakeCatClientService.get();
        iTableStorageInfoService.deleteByDt("2022-08-10");
        for (TableInfo tableDetail : tableInfos) {
            executor.submit(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    try {
                        LongAdder currentTableObjectNum = new LongAdder();
                        LongAdder currentTableSmallObjectNum = new LongAdder();
                        LongAdder currentTableSize = new LongAdder();
//
//        TableInfo tableDetail = tableInfoMapper.getTableDetail(439257l);
//        TableInfo tableDetail = tableInfoMapper.getTableDetail(453096l);
//        TableInfo tableDetail = tableInfoMapper.getTableDetail(440805l);
                        String location = tableDetail.getLocation();
                        String region = tableDetail.getRegion();
//                        if("ue1".equals(region) && location.contains("ap-southeast-1")){
//                        }


                        String owner = tableDetail.getOwner();
                        String fileFormat = tableDetail.getSdFileFormat();
                        int partitionType = tableDetail.getPartitionType();
//                        1 分区表 0 非分区表
                        // TODO: 2023/1/12  修改租户id 此一处调用
                        String project = globalConfig.getProject(region);
                        String dbName = tableDetail.getDbName();
                        String catalogName = CatalogNameEnum.getCatalogName(region);
                        String name = tableDetail.getName();


                        ListTablePartitionsRequest request = new ListTablePartitionsRequest();
                        request.setProjectId(project);
                        request.setCatalogName(catalogName);
                        request.setDatabaseName(dbName);
                        request.setTableName(name);
                        PartitionFilterInput partitionFilterInput = new PartitionFilterInput();
                        partitionFilterInput.setMaxParts((short) -1);
                        request.setInput(partitionFilterInput);

                        String partitionPath;
                        String bucketName = null;
                        List<String> listPartitionNames = lakeCatClient.listPartitionNames(request);
                        if (partitionType == 1) {
                            listPartitionNames = lakeCatClient.listPartitionNames(request);
                            if (listPartitionNames.size() == 0) {
                                bucketName = location.split("\\//")[1].split("\\/")[0];
                                currentTableObjectNum.reset();
                                currentTableSmallObjectNum.reset();
                                currentTableSize.reset();


                            } else {
                                for (int j = 0; j < listPartitionNames.size(); j++) {
                                    String partitionName = listPartitionNames.get(j);
                                    partitionPath = location + "/" + partitionName;
                                    bucketName = partitionPath.split("\\//")[1].split("\\/")[0];
                                    int length = bucketName.length();
                                    HashMap storageInfo = null;
                                    if (region.equals("ue1")) {
                                        int begin = length + 6;
                                        String keyName = partitionPath.substring(begin);
                                        storageInfo = getAwsObjectStorageInfo(bucketName, keyName, region);
                                    } else if (region.equals("sg2")) {
                                        int begin = length + 7;
                                        String keyName = partitionPath.substring(begin);
                                        storageInfo = getHwObjectStorageInfo(bucketName, keyName, region);
                                    }

                                    LongAdder currentPartitionObjectNum = (LongAdder) storageInfo.get("currentPartitionObjectNum");
                                    LongAdder currentPartitionSmallObjectNum = (LongAdder) storageInfo.get("currentPartitionSmallObjectNum");
                                    LongAdder currentPartitionSize = (LongAdder) storageInfo.get("currentPartitionSize");

                                    currentTableObjectNum.add(currentPartitionObjectNum.longValue());
                                    currentTableSmallObjectNum.add(currentPartitionSmallObjectNum.longValue());
                                    currentTableSize.add(currentPartitionSize.longValue());

                                }
                            }

                        } else {
                            partitionPath = location;
                            bucketName = partitionPath.split("\\//")[1].split("\\/")[0];
                            int length = bucketName.length();
                            HashMap storageInfo = null;
                            if (region.equals("ue1")) {
                                int begin = length + 6;
                                String keyName = partitionPath.substring(begin);
                                storageInfo = getAwsObjectStorageInfo(bucketName, keyName, region);
                            } else if (region.equals("sg2")) {
                                int begin = length + 7;
                                String keyName = partitionPath.substring(begin);
                                storageInfo = getHwObjectStorageInfo(bucketName, keyName, region);
                            }

                            LongAdder currentPartitionObjectNum = (LongAdder) storageInfo.get("currentPartitionObjectNum");
                            LongAdder currentPartitionSmallObjectNum = (LongAdder) storageInfo.get("currentPartitionSmallObjectNum");
                            LongAdder currentPartitionSize = (LongAdder) storageInfo.get("currentPartitionSize");

                            currentTableObjectNum.add(currentPartitionObjectNum.longValue());
                            currentTableSmallObjectNum.add(currentPartitionSmallObjectNum.longValue());
                            currentTableSize.add(currentPartitionSize.longValue());
                        }

                        TableStorageInfo tableStorageInfo = TableStorageInfo.builder()
                                .dt("2022-08-10")
                                .key(tableDetail.getKey())
                                .owner(owner)
                                .dbName(dbName)
                                .tableName(name)
                                .tableObjectNum(currentTableObjectNum.longValue())
                                .tableSmallObjectNum(currentTableSmallObjectNum.longValue())
                                .tablePartitionNum(listPartitionNames.size())
                                .tableBucketName(bucketName)
                                .totalStorage(currentTableSize.longValue() + "")
                                .storageType(storageType)
                                .storageFileFormat(fileFormat)
                                .location(location)
                                .smallFileAdvice("")
                                .smallFileWorkbenchUrl("")
                                .region(region)
                                .provider(providerName)
                                .build();

                        tableStorageInfos.add(tableStorageInfo);
                        iTableStorageInfoService.save(tableStorageInfo);
                    } catch (CatalogException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        cdl.countDown();
                    }
                }
            });
        }
// 调用闭锁的await()方法，该线程会被挂起，它会等待直到count值为0才继续执行
// 这样我们就能确保上面多线程都执行完了才走后续代码
        cdl.await();
//关闭线程池
        executor.shutdown();
        System.out.println("====== 线程结束 =====");

        return null;

    }

    @Override
    public void deleteByDt(String dt) {
        tableStorageInfoMapper.deleteByDt(dt);
    }
}
