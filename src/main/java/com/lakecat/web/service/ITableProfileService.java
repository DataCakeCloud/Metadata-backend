package com.lakecat.web.service;

import java.math.BigInteger;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.lakecat.catalog.common.model.TableAccessUsers;
import io.lakecat.catalog.common.model.TableUsageProfile;

import com.lakecat.web.entity.TableInfoReq;
import com.lakecat.web.entity.TableProfileInfoReq;
import com.lakecat.web.entity.TableUsageProfileGroupByUser;
import com.lakecat.web.exception.BusinessException;

public interface ITableProfileService {

    /**
     * 获取最近访问用户, 仅支持一个region范围内的查询
     *
     * @param tableInfoReqs
     * @return
     */
    List <TableAccessUsers> recentlyVisitedUsers(@NotNull List <TableInfoReq> tableInfoReqs, String tenantName);

    /**
     * 获取最近访问用户
     *
     * @param tableInfoReq
     * @return
     * @throws BusinessException
     */
    List <String> recentlyVisitedUsers(@NotNull TableInfoReq tableInfoReq) throws BusinessException;

    /**
     * 查询表的以用户分组使用情况
     *
     * @param tableProfileInfoReq
     * @param opTypes             ”WRITE;READ”
     * @return
     */
    List <TableUsageProfileGroupByUser> getUsageProfileGroupByUser(@NotNull TableProfileInfoReq tableProfileInfoReq, String opTypes);


    /**
     * 根据表查询被访问次数
     *
     * @param tableProfileInfoReq
     * @return
     * @throws BusinessException
     */
    BigInteger getUsageProfilesByTable(TableProfileInfoReq tableProfileInfoReq)
            throws BusinessException;

    /**
     * 根据表查询被访问总的次数
     *
     * @param region
     * @param databaseName
     * @param tableName
     * @return
     * @throws BusinessException
     */
    BigInteger getUsageProfilesByTable(String region, String databaseName, String tableName, String tenantName)
            throws BusinessException;

    /**
     * 根据表获取访问详情
     *
     * @param tableProfileInfoReq
     * @param size
     * @return
     * @throws BusinessException
     */
    List <TableUsageProfile> getUsageProfileDetails(TableProfileInfoReq tableProfileInfoReq, Integer size)
            throws BusinessException;
}
