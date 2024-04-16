package com.lakecat.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lakecat.web.entity.DatabaseInfo;
import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.exception.BusinessException;

import java.util.List;

public interface ITableInfoByHMSService extends IService <TableInfo> {

    TableInfo createTable(TableInfo entity);

    void createDatabase(DatabaseInfo entity);

    boolean alterColumnsByHMS(TableInfo entity);

    List <String> getDBList(String region, String tenantName, String userName);

    List <String> getTableList(String region, String dbName, String tenantName, String userName);

    Boolean checkCreate(TableInfo entity);
}
