package com.lakecat.web.vo.blood;

import com.google.common.collect.Sets;
import com.lakecat.web.entity.TableBloodInfo;
import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.mapper.TableBloodInfoMapper;
import com.lakecat.web.service.BloodService;
import com.lakecat.web.utils.DateUtil;
import com.lakecat.web.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class BloodSyncRunnable implements Runnable{

    private BloodService bloodService;

    private TableInfo tableInfo;

    private TableBloodInfoMapper tableBloodInfoMapper;

    private CountDownLatch countDownLatch;


    @Override
    public void run() {
        TableBloodInfo tableBloodInfo=TableBloodInfo.builder().createTime(DateUtil.getCurrentDateStr()).updateTime(DateUtil.getCurrentDateStr())
                .dbName(tableInfo.getDbName()).name(tableInfo.getName()).region(tableInfo.getRegion()).build();
        try {
            BloodRequest bloodRequest =BloodRequest.builder().region(tableInfo.getRegion()).databaseName(tableInfo.getDbName())
                    .tableName(tableInfo.getName()).upDeep(5).downDeep(5).build();
            Response response=bloodService.blood(bloodRequest);
            tableBloodInfo.setTaskId(bloodRequest.getTaskId());
            tableBloodInfo.setTaskName(bloodRequest.getTaskName());
            if (!StringUtils.isEmpty(response)){
                tableBloodInfo.setBloodStr(JsonUtil.toJson(response,false));
                Set<String> sets= Sets.newHashSet();
                for (Response.Rdata.Instance instance:response.getData().getInstance()){
                    if (!StringUtils.isEmpty(instance.getOwner())){
                        sets.addAll(Arrays.stream(instance.getOwner().split(",")).collect(Collectors.toSet()));
                    }
                }
                if (!CollectionUtils.isEmpty(sets)){
                    tableBloodInfo.setOwners(JsonUtil.toJson(sets,false));
                }
            }
            tableBloodInfoMapper.insert(tableBloodInfo);
        }catch (Exception e){
            log.error("",e);
            tableBloodInfo.setException(true);
        }finally {
            countDownLatch.countDown();
        }
    }
}
