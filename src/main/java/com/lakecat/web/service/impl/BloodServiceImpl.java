package com.lakecat.web.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lakecat.web.entity.*;
import com.lakecat.web.entity.table.TableOutputInfo;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.mapper.TableBloodInfoMapper;
import com.lakecat.web.mapper.TableInfoMapper;
import com.lakecat.web.service.BloodService;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.service.ITableInfoService;
import com.lakecat.web.thread.SyncThread;
import com.lakecat.web.utils.DateUtil;
import com.lakecat.web.utils.JsonUtil;
import com.lakecat.web.vo.blood.*;
import io.prestosql.jdbc.$internal.guava.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BloodServiceImpl implements BloodService {

    @Autowired
    ITableInfoService tableInfoService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${pipeline.lineage.url}")
    private String pipelineLineageUrl;

    @Resource
    private TableInfoMapper tableInfoMapper;

    @Resource
    private TableBloodInfoMapper tableBloodInfoMapper;

    @Autowired
    private ITableInfoService iTableInfoService;

    @Autowired
    private ILakeCatClientService iLakeCatClientService;

    /**
     * 并发调用上游和下游的血缘然后合并
     *
     * @param bloodRequest
     * @return
     */
    @Override
    public Response blood(BloodRequest bloodRequest) {
        if (StringUtils.isEmpty(bloodRequest.getTaskName())) {
            TableInfoReq tableInfoReq = new TableInfoReq();
            BeanUtils.copyProperties(bloodRequest, tableInfoReq);
            try {
                TableOutputInfo tableOutputInfo = tableInfoService.tableOutputInfo(tableInfoReq);
                if (tableOutputInfo != null && tableOutputInfo.getTaskName() != null) {
                    bloodRequest.setTaskName(tableOutputInfo.getTaskName());
                    bloodRequest.setTaskId(tableOutputInfo.getTaskId());
                }else {
                    return null;
                }
            } catch (BusinessException e) {
                return null;
            }
        }
        List<Response> responseList = Lists.newArrayList();
        int i = 0;
        List<BloodRunnable> bloodRunnableList = Lists.newArrayList();
        if (bloodRequest.getUpDeep() > 0) {
            i++;
            BloodRunnable bloodRunnable = new BloodRunnable(restTemplate, null, pipelineLineageUrl, bloodRequest, 1, responseList);
            bloodRunnableList.add(bloodRunnable);
        }
        if (bloodRequest.getDownDeep() > 0) {
            i++;
            BloodRunnable bloodRunnable = new BloodRunnable(restTemplate, null, pipelineLineageUrl, bloodRequest, 2, responseList);
            bloodRunnableList.add(bloodRunnable);
        }
        if (i > 0) {
            CountDownLatch countDownLatch = new CountDownLatch(i);
            bloodRunnableList.forEach(bloodRunnable -> {
                bloodRunnable.setCountDownLatch(countDownLatch);
                SyncThread.pool.execute(bloodRunnable);
            });
            try {
                countDownLatch.await(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("", e);
            }
        }
        if (CollectionUtils.isNotEmpty(responseList)) {
            if (responseList.size() > 1) {
                for (Iterator<Response> iterator = responseList.iterator(); iterator.hasNext(); ) {//删除只有单边的数据
                    Response response = iterator.next();
                    if (response.getData().getInstance().size() == 1) {
                        iterator.remove();
                        break;
                    }
                }
            }
            if (responseList.size() == 1) {
                return responseList.get(0);
            }
            Response up = null;
            Response down = null;
            for (Response response : responseList) {
                if (response.isUp()) {
                    up = response;
                } else {
                    down = response;
                }
            }
            List<Response.Rdata.Instance> replaceInstance = Lists.newArrayList();
            for (Iterator<Response.Rdata.Instance> iterator = up.getData().getInstance().iterator(); iterator.hasNext(); ) {//保留下游的起始阶段 去掉上游的终止节点
                Response.Rdata.Instance instance = iterator.next();
                for (Response.Rdata.Instance dInstance : down.getData().getInstance()) {
                    if (!StringUtils.isEmpty(dInstance.getNodeId()) && !StringUtils.isEmpty(instance.getNodeId()) && dInstance.getNodeId().equals(instance.getNodeId())) {
                        iterator.remove();
                        break;
                        //replaceInstance.add(dInstance);
                    }
                }
            }
            up.getData().getInstance().addAll(down.getData().getInstance());
            up.getData().getRelation().addAll(down.getData().getRelation());
            return up;
        }
        return null;
    }

    /**
     * 封装返回的信息
     *
     * @param response
     */
    public void wrapBloodResponse(Response response) {
        if (response != null && response.getData() != null && CollectionUtils.isNotEmpty(response.getData().getInstance())){
            for (Response.Rdata.Instance instance:response.getData().getInstance()){
                if (instance.getData()==null){
                    Response.Rdata.Instance.TableDetail tableDetail=new Response.Rdata.Instance.TableDetail();
                    instance.setData(tableDetail);
                    tableDetail.setOwner(instance.getOwner());
                    tableDetail.setTaskId(instance.getTask_id());
                    if (!StringUtils.isEmpty(instance.getMetadataId())){
                        String metadataId=instance.getMetadataId();
                        if (metadataId.indexOf("@")>-1){
                            String[] metaDatas=instance.getMetadataId().split("\\.");
                            tableDetail.setDatabaseName(metaDatas[0]);
                            if (metaDatas.length>1){
                                String[] tableAndRegion=metaDatas[1].split("@");
                                tableDetail.setTableName(tableAndRegion[0]);
                                if (tableAndRegion.length>1){
                                    tableDetail.setRegion(tableAndRegion[1]);
                                }
                            }
                            instance.setMetadataId(metadataId.substring(0,metadataId.indexOf("@")));
                        }
                    }
                    if (!StringUtils.isEmpty(instance.getNodeId())){
                        tableDetail.setTaskName(instance.getNodeId().split(",")[0]);
                    }
                    if (!StringUtils.isEmpty(tableDetail.getTableName()) && !StringUtils.isEmpty(tableDetail.getDatabaseName()) && !StringUtils.isEmpty(tableDetail.getRegion())) {
                        LakeCatParam lakeCatParam = LakeCatParam.builder().region(tableDetail.getRegion())
                                .dbName(tableDetail.getDatabaseName()).tableName(tableDetail.getTableName()).build();
                        TableInfo tableInfo = iLakeCatClientService.getTable(lakeCatParam);
//                        List<TableInfo> ids=tableInfoMapper.selectTables(tableDetail.getRegion(),tableDetail.getDatabaseName(),tableDetail.getTableName());
//                        if (CollectionUtils.isNotEmpty(ids)){
//                            tableDetail.setTableId(ids.get(0).getId());
//                            tableDetail.setTableOwner(ids.get(0).getOwner());
//                        }
                        if (tableInfo != null) {
                            tableDetail.setTableOwner(tableInfo.getOwner());
                        }
                        TableInfoReq tableInfoReq = new TableInfoReq();
                        BeanUtils.copyProperties(tableDetail, tableInfoReq);
                        try {
                            List<TableUsageProfileGroupByUser> tableUsageProfileGroupByUsers=iTableInfoService.tableProfileInfo(tableInfoReq,null);
                            tableDetail.setUsageProfileGroupByUsers(tableUsageProfileGroupByUsers);
                        } catch (Exception e) {
                            //log.error("",e);
                        }
                    }
                }
            }
        }
    }

    /**
     * upDeep downDeep传0的时候 默认为5，获取血缘owner
     *
     * @return
     */
    public List<String> bloodOwners(BloodRequest bloodRequest) {
        bloodRequest.wrapDefault();
        Set<String> sets = Sets.newHashSet();
        Response response = blood(bloodRequest);
        if (response != null) {
            for (Response.Rdata.Instance instance : response.getData().getInstance()) {
                if (!StringUtils.isEmpty(instance.getOwner())) {
                    sets.addAll(Arrays.stream(instance.getOwner().split(",")).collect(Collectors.toSet()));
                }
            }
        }
        return Lists.newArrayList(sets);
    }

    public void sync() {
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        List<TableInfo> tableInfos = tableInfoMapper.selectList(null);
        tableInfos = tableInfos.subList(0, 1);
        if (CollectionUtils.isNotEmpty(tableInfos)) {
            CountDownLatch countDownLatch = new CountDownLatch(tableInfos.size());
            for (TableInfo tableInfo : tableInfos) {
                BloodSyncRunnable bloodSyncRunnable = new BloodSyncRunnable(this, tableInfo, tableBloodInfoMapper, countDownLatch);
                executorService.execute(bloodSyncRunnable);
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static Response tree(TableBloodInfo tableBloodInfo, int depth) {
        if (!StringUtils.isEmpty(tableBloodInfo.getBloodStr())) {
            Response response = JsonUtil.parse(tableBloodInfo.getBloodStr(), Response.class);
            if (response.getData() != null && CollectionUtils.isNotEmpty(response.getData().getRelation()) && response.getData().getRelation().size() > 1) {
                BloodNode root = BloodNode.builder().nodeName(response.getData().getCoreTaskId()).depth(0).allNodes(Maps.newHashMap()).leftNodes(Lists.newArrayList())
                        .rightNodes(Lists.newArrayList()).count(1).build();
                root.getAllNodes().put(root.getNodeName(), root);
                for (; ; ) {
                    for (Response.Rdata.Relation relation : response.getData().getRelation()) {
                        if (root.getAllNodes().containsKey(relation.getTarget())) {
                            BloodNode bloodNode = root.getAllNodes().get(relation.getTarget());
                            BloodNode left = BloodNode.builder().nodeName(relation.getTarget()).depth(0).allNodes(Maps.newHashMap()).leftNodes(Lists.newArrayList())
                                    .rightNodes(Lists.newArrayList()).count(1).build();
                            bloodNode.getLeftNodes().add(left);
                            root.setCount(bloodNode.getCount() + 1);
                            root.getAllNodes().put(bloodNode.getNodeName(), bloodNode);
                        }
                        if (root.getAllNodes().containsKey(relation.getSource())) {
                            BloodNode bloodNode = root.getAllNodes().get(relation.getTarget());
                            BloodNode right = BloodNode.builder().nodeName(relation.getTarget()).depth(0).allNodes(Maps.newHashMap()).leftNodes(Lists.newArrayList())
                                    .rightNodes(Lists.newArrayList()).count(1).build();
                            bloodNode.getRightNodes().add(right);
                            root.setCount(bloodNode.getCount() + 1);
                            root.getAllNodes().put(bloodNode.getNodeName(), bloodNode);
                        }
                    }
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        TableBloodInfo tableBloodInfo = new TableBloodInfo();

    }

}
