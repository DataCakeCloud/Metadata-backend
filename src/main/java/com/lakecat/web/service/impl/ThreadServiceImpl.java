package com.lakecat.web.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lakecat.web.common.CommonParameters;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.entity.CurrentUser;
import com.lakecat.web.entity.InfTraceContextHolder;
import com.lakecat.web.entity.RoleTableRelevance;
import com.lakecat.web.entity.TableForLastActivity;
import com.lakecat.web.mapper.LastActivityMapper;
import com.lakecat.web.mapper.TableInfoMapper;
import com.lakecat.web.service.IAdminRoleService;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.service.ITableInfoService;
import com.lakecat.web.service.ThreadService;
import com.lakecat.web.thread.SyncThread;
import com.lakecat.web.utils.DSUtilForLakecat;
import io.lakecat.catalog.client.LakeCatClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static com.lakecat.web.common.CommonMethods.administrator;
import static com.lakecat.web.constant.CatalogNameEnum.regionCatalogMapping;
import static java.util.stream.Collectors.toList;

/**
 * Created by slj on 2023/3/7.
 */
@Slf4j
@Service
@Lazy(false)
public class ThreadServiceImpl implements ThreadService {

    @Value("${default.tenantName}")
    private String defaultTenantName;

    @Value("${default.region}")
    private String defaultRegion;

    @Autowired
    ITableInfoService tableInfoService;

    @Autowired
    private IAdminRoleService adminRoleService;


    @Resource
    private TableInfoMapper tableInfoMapper;

    @Resource
    private LastActivityMapper lastActivityMapper;


    @Autowired
    DSUtilForLakecat dsUtilForLakecat;


    @Autowired
    private com.lakecat.web.service.IAuthGovernService IAuthGovernService;


    @Autowired
    ILakeCatClientService iLakeCatClientService;


    @Autowired
    com.lakecat.web.constant.CatalogNameEnum catalogNameEnum;

    @Autowired
    SyncThread alertSendThread;

    @Async("prodAsync")  //指定线程池
    @Override
    public void initTable() {
        List <String> allTenantName = Lists.newArrayList(defaultTenantName);

        List <String> list = tableInfoMapper.showDatabases();
        List <String> collect = list.stream().filter(x -> x.startsWith("gov_")).map(x -> {
            return x.substring(4);
        }).collect(toList());
        allTenantName.retainAll(collect);
        for (String tenantName : allTenantName) {

            if (!tenantName.equals("shareit") && !tenantName.equals("payment")) {
                continue;
            }
            log.info("##############################" + tenantName + "################################################");
            try {
                CurrentUser userInfo = administrator(tenantName);
                Map <String, CatalogNameEnum.CloudRegionCatalog> ANY_NAME_MAP = new HashMap <>();
                catalogNameEnum.setAnyNameMap(ANY_NAME_MAP, regionCatalogMapping);
                userInfo.setRegionInfo(ANY_NAME_MAP);
                InfTraceContextHolder.get().setUserInfo(userInfo);
                LakeCatClient lakeCatClient = iLakeCatClientService.get();
                InfTraceContextHolder.get().setLakeCatClient(lakeCatClient);
                Set <String> regionList = catalogNameEnum.getRegionList();
                for (String region : regionList) {
                    try {
                        tableInfoService.synchronization(tenantName, region, false,false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(tenantName + "执行完成");
        }
    }


    @Async("prodAsync")  //指定线程池
    @Override
    public void initTableOhter(boolean update) {
        List <String> allTenantName = Lists.newArrayList(defaultTenantName);

    /*    List <String> list = tableInfoMapper.showDatabases();
        List <String> collect = list.stream().filter(x -> x.startsWith("gov_")).map(x -> {
            return x.substring(4);
        }).collect(toList());
        allTenantName.retainAll(collect);*/
        for (String tenantName : allTenantName) {
            log.info("##############################" + tenantName + "################################################");
            try {
                CurrentUser userInfo = administrator(tenantName);
                Map <String, CatalogNameEnum.CloudRegionCatalog> ANY_NAME_MAP = new HashMap <>();
                catalogNameEnum.setAnyNameMap(ANY_NAME_MAP, regionCatalogMapping);
                userInfo.setRegionInfo(ANY_NAME_MAP);
                InfTraceContextHolder.get().setUserInfo(userInfo);
                LakeCatClient lakeCatClient = iLakeCatClientService.get();
                InfTraceContextHolder.get().setLakeCatClient(lakeCatClient);
                Set <String> regionList = catalogNameEnum.getRegionList();
                for (String region : regionList) {
                    try {
                        tableInfoService.synchronization(tenantName, region, false,update);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Async("prodAsync")  //指定线程池
    @Override
    public void syncForRole() {
        try {
            List <String> allTenantName = Lists.newArrayList(defaultTenantName);//dsUtilForLakecat.getAllTenantName();
           /* List <String> list = tableInfoMapper.showDatabases();
            List <String> collect = list.stream().filter(x -> x.startsWith("gov_")).map(x -> {
                return x.substring(4);
            }).collect(toList());
            allTenantName.retainAll(collect);*/
            for (String tenantName : allTenantName) {
                CurrentUser userInfo = administrator(tenantName);
                InfTraceContextHolder.get().setUserInfo(userInfo);
                LakeCatClient lakeCatClient = iLakeCatClientService.get();
                InfTraceContextHolder.get().setLakeCatClient(lakeCatClient);
                adminRoleService.syncGrantPrivilege(tenantName);
            }

        } catch (Exception e) {
            log.info("",e);
        }
    }


    @Async("prodAsync")  //指定线程池
    @Override
    public void syncForPvc() {
        try {
            List <String> allTenantName = dsUtilForLakecat.getAllTenantName();
            for (String tenantName : allTenantName) {
                CurrentUser userInfo = administrator(tenantName);
                InfTraceContextHolder.get().setUserInfo(userInfo);
                LakeCatClient lakeCatClient = iLakeCatClientService.get();
                InfTraceContextHolder.get().setLakeCatClient(lakeCatClient);
                IAuthGovernService.sync();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Async("prodAsync")  //指定线程池
    @Override
    public void syncForLast() {
        log.info("开始同步最近访问数据");
        try {
            List <String> allTenantName = Lists.newArrayList(defaultTenantName);
            for (String tenantName : allTenantName) {
                CurrentUser userInfo = administrator(tenantName);
                Map <String, CatalogNameEnum.CloudRegionCatalog> ANY_NAME_MAP = new HashMap <>();
                catalogNameEnum.setAnyNameMap(ANY_NAME_MAP, regionCatalogMapping);
                userInfo.setRegionInfo(ANY_NAME_MAP);
                InfTraceContextHolder.get().setUserInfo(userInfo);
                LakeCatClient lakeCatClient = iLakeCatClientService.get();
                InfTraceContextHolder.get().setLakeCatClient(lakeCatClient);
                tableInfoService.syncLast();
            }
        } catch (Exception e) {
            log.info("",e);
        }
    }
}
