package com.lakecat.web.service.impl;

import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.entity.TableInfoSearchHistory;
import com.lakecat.web.mapper.TableInfoSearchHistoryMapper;
import com.lakecat.web.service.ITableInfoSearchHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-15
 */
@Service
public class TableInfoSearchHistoryServiceImpl extends
    ServiceImpl<TableInfoSearchHistoryMapper, TableInfoSearchHistory> implements ITableInfoSearchHistoryService {

    @Autowired
    TableInfoServiceImpl tableInfoService;

    @Override
    public void addSearchHistory(Long id, String userId) {
        TableInfo tableDetail = tableInfoService.getTableDetail(id);
        String tableInfoName = tableDetail.getDbName() + "." + tableDetail.getName();
        TableInfoSearchHistory tableInfoSearchHistory = new TableInfoSearchHistory();
        tableInfoSearchHistory.setUserId(userId);
        tableInfoSearchHistory.setTableInfoName(tableInfoName);
        tableInfoSearchHistory.setTableInfoId(id);
        this.save(tableInfoSearchHistory);

    }

    @Override
    public void addSearchHistory(TableInfo tableIno, String userId) {
//        TableInfo tableDetail = tableInfoService.getTableDetail(id);
        String tableInfoName = tableIno.getDbName() + "." + tableIno.getTableName();
        TableInfoSearchHistory tableInfoSearchHistory = new TableInfoSearchHistory();
        tableInfoSearchHistory.setUserId(userId);
        tableInfoSearchHistory.setTableInfoName(tableInfoName);
//        tableInfoSearchHistory.setTableInfoId(id);
        tableInfoSearchHistory.setSole(tableIno.getRegion() + "." + tableInfoName);
        this.save(tableInfoSearchHistory);

    }


}
