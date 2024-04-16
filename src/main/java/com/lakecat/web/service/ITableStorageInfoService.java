package com.lakecat.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lakecat.web.entity.table.TableStorageInfo;
import com.lakecat.web.exception.BusinessException;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2022-08-09
 */
public interface ITableStorageInfoService extends IService <TableStorageInfo> {

    TableStorageInfo getStorageTableInfo(String regionName,String providerName, String storageType) throws BusinessException, InterruptedException;

    void deleteByDt(String dt);

}
