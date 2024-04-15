package com.lakecat.web.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.lakecat.web.entity.InfTraceContextHolder;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.model.PagedList;
import io.lakecat.catalog.common.model.TableAccessUsers;
import io.lakecat.catalog.common.model.TableSource;
import io.lakecat.catalog.common.model.TableUsageProfile;
import io.lakecat.catalog.common.plugin.request.GetTableAccessUsersRequest;
import io.lakecat.catalog.common.plugin.request.GetTableUsageProfileRequest;
import io.lakecat.catalog.common.plugin.request.GetUsageProfileDetailsRequest;
import io.lakecat.catalog.common.plugin.request.GetUsageProfilesGroupByUserRequest;

import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.entity.TableInfoReq;
import com.lakecat.web.entity.TableProfileInfoReq;
import com.lakecat.web.entity.TableUsageProfileGroupByUser;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.service.ITableProfileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TableProfileServiceImpl implements ITableProfileService {

    @Autowired
    ILakeCatClientService lakeCatClientService;
    @Autowired
    CatalogNameEnum CatalogNameEnum;

    @Override
    public List <TableAccessUsers> recentlyVisitedUsers(List <TableInfoReq> tableInfoReqs, String tenantName) {
        if (tableInfoReqs.size() > 0) {
            String region = tableInfoReqs.get(0).getRegion();
            String catalogName = CatalogNameEnum.getCatalogName(region);
            LakeCatClient lakeCatClient = lakeCatClientService.get();
            ArrayList <TableSource> tableSources = new ArrayList <>();
            TableSource tableSource;
            for (TableInfoReq tableInfoReq : tableInfoReqs) {
                tableSource = new TableSource(tenantName,
                        catalogName, tableInfoReq.getDatabaseName(),
                        tableInfoReq.getTableName());
                tableSources.add(tableSource);
            }
            GetTableAccessUsersRequest accessUsersRequest = new GetTableAccessUsersRequest(
                    lakeCatClient.getProjectId(), tableSources);
            List <TableAccessUsers> tableAccessUsers = lakeCatClient.getTableAccessUsers(accessUsersRequest);
            if (CollectionUtils.isNotEmpty(tableAccessUsers)) {
                return tableAccessUsers;
            }
        }
        return null;
    }

    @Override
    public List <String> recentlyVisitedUsers(TableInfoReq tableInfoReq) throws BusinessException {
        List <TableInfoReq> tableInfoReqs = new ArrayList <>();
        tableInfoReqs.add(tableInfoReq);
        List <TableAccessUsers> tableAccessUsersVos = recentlyVisitedUsers(tableInfoReqs, tableInfoReq.getTableName());
        if (CollectionUtils.isNotEmpty(tableAccessUsersVos)) {
            return tableAccessUsersVos.get(0).getUsers();
        }
        return null;
    }

    @Override
    public List <TableUsageProfileGroupByUser> getUsageProfileGroupByUser(TableProfileInfoReq tableProfileInfoReq, String opTypes) {
        LakeCatClient lakeCatClient = getClient();
        String tenantName = tableProfileInfoReq.getTenantName();
        String projectId;
        if (tenantName != null && !"".equals(tenantName)) {
            projectId = tenantName;
        } else {
            projectId = lakeCatClient.getProjectId();
        }
        TableSource tableSource = new TableSource(projectId,
                CatalogNameEnum.getCatalogName(tableProfileInfoReq.getRegion()), tableProfileInfoReq.getDatabaseName(),
                tableProfileInfoReq.getTableName());
        GetUsageProfilesGroupByUserRequest byUserRequest = new GetUsageProfilesGroupByUserRequest(projectId, tableSource);
        if (opTypes == null) {
            opTypes = "WRITE;READ";
        }
        byUserRequest.setOpType(opTypes);
        if (tableProfileInfoReq.getStartTimestamp() != null) {
            byUserRequest.setStartTimestamp(tableProfileInfoReq.getStartTimestamp());
        }
        if (tableProfileInfoReq.getEndTimestamp() != null) {
            byUserRequest.setEndTimestamp(tableProfileInfoReq.getEndTimestamp());
        }
        List <TableUsageProfile> usageProfileGroupByUser = lakeCatClient.getUsageProfileGroupByUser(byUserRequest);
        if (CollectionUtils.isNotEmpty(usageProfileGroupByUser)) {
            return usageProfileGroupByUser.stream()
                    .map(x -> new TableUsageProfileGroupByUser(x.getUserId(), x.getCreateTimestamp(), x.getSumCount(), null))
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public BigInteger getUsageProfilesByTable(TableProfileInfoReq tableProfileInfoReq) throws BusinessException {
        LakeCatClient lakeCatClient = getClient();
        GetTableUsageProfileRequest request = new GetTableUsageProfileRequest();
        request.setProjectId( InfTraceContextHolder.get().getTenantName());
        request.setCatalogName(CatalogNameEnum.getCatalogName(tableProfileInfoReq.getRegion()));
        request.setDatabaseName(tableProfileInfoReq.getDatabaseName());
        request.setTableName(tableProfileInfoReq.getTableName());
        if (tableProfileInfoReq.getStartTimestamp() != null) {
            request.setStartTimestamp(tableProfileInfoReq.getStartTimestamp());
        }
        if (tableProfileInfoReq.getEndTimestamp() != null) {
            request.setEndTimestamp(tableProfileInfoReq.getEndTimestamp());
        }
        request.setUserId(tableProfileInfoReq.getUserId());
        PagedList <TableUsageProfile> tableUsageProfile = lakeCatClient.getTableUsageProfile(request);
        if (tableUsageProfile != null && tableUsageProfile.getObjects() != null && tableUsageProfile.getObjects().length > 0) {
            return tableUsageProfile.getObjects()[0].getSumCount();
        }
        return BigInteger.valueOf(0);
    }

    @Override
    public BigInteger getUsageProfilesByTable(String region, String databaseName, String tableName, String tenantName)
            throws BusinessException {
        TableProfileInfoReq tableInfoReq = new TableProfileInfoReq();
        tableInfoReq.setRegion(region);
        tableInfoReq.setDatabaseName(databaseName);
        tableInfoReq.setTableName(tableName);
        tableInfoReq.setTenantName(tenantName);
        tableInfoReq.setStartTimestamp(0L);
        tableInfoReq.setEndTimestamp(System.currentTimeMillis());
        return getUsageProfilesByTable(tableInfoReq);
    }

    @Override
    public List <TableUsageProfile> getUsageProfileDetails(TableProfileInfoReq tableProfileInfoReq, Integer size)
            throws BusinessException {
        LakeCatClient lakeCatClient = getClient();
        TableSource tableSource = new TableSource(lakeCatClient.getProjectId(),
                CatalogNameEnum.getCatalogName(tableProfileInfoReq.getRegion()), tableProfileInfoReq.getDatabaseName(),
                tableProfileInfoReq.getTableName());
        GetUsageProfileDetailsRequest request = new GetUsageProfileDetailsRequest(lakeCatClient.getProjectId(), tableSource);
        request.setUserId(tableProfileInfoReq.getUserId());
        request.setRowCount(size);
        return Arrays.asList(lakeCatClient.getUsageProfileDetails(request).getObjects());
    }

    private LakeCatClient getClient() {
        LakeCatClient lakeCatClient = InfTraceContextHolder.get().getLakeCatClient();
        if (lakeCatClient == null) {
            return lakeCatClientService.get();
        }
        return lakeCatClient;
    }

}
