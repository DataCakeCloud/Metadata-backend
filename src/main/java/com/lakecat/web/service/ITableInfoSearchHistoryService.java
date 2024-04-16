package com.lakecat.web.service;

import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.entity.TableInfoSearchHistory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-15
 */
public interface ITableInfoSearchHistoryService extends IService<TableInfoSearchHistory> {

    void addSearchHistory(Long id, String userId);

    void addSearchHistory(TableInfo tableIno, String userId);
}