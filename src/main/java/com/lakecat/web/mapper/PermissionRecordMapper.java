package com.lakecat.web.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lakecat.web.entity.CollectInfo;
import com.lakecat.web.entity.PermissionRecordInfo;
import org.apache.ibatis.annotations.*;
import org.springframework.transaction.annotation.Transactional;


/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-13
 */
@Mapper
public interface PermissionRecordMapper extends BaseMapper <PermissionRecordInfo> {


    @Insert("insert into permission_record_info" +
            "(order_id," +
            "table_list," +
            "permission," +
            "reason," +
            "cycle," +
            "apply_user," +
            "flag," +
            "grant_type," +
            "status," +
            "grant_user," +
            "type," +
            "table_recovery_state, " +
            "proposer, " +
            "certigier) " +
            "VALUES(" +
            "#{data.orderId}," +
            "#{data.tableList}," +
            "#{data.permission}," +
            "#{data.reason}," +
            "#{data.cycle}," +
            "#{data.applyUser}," +
            "#{data.flag}," +
            "#{data.grantType}," +
            "#{data.status}," +
            "#{data.grantUser}," +
            "#{data.type}," +
            "#{data.tableRecoveryState}," +
            "#{data.proposer}," +
            "#{data.certigier})")

    @Options(useGeneratedKeys = true, keyProperty = "data.id")
    @Transactional(rollbackFor = Exception.class)
    boolean insertForOrder(@Param("data") PermissionRecordInfo permission);


    @Select("select * from permission_record_info " +
            " where table_list like CONCAT('%', #{tableInfo}, '%')  and status=0 " +
            " order by update_time desc")
    //@Select("select * from permission_record_info where table_list like #{tableInfo} and (apply_user=#{userId} or grant_user like '%${userId}%')")
    List<PermissionRecordInfo> findUserByTableInfo(@Param("tableInfo") String tableInfo);


    @Update("update permission_record_info set " +
            "table_recovery_state=#{tableRecoveryStateRes}," +
            "update_time = CURRENT_TIMESTAMP " +
            "where id=#{id}")
    void updateRecoveryState(@Param("tableRecoveryStateRes") String tableRecoveryStateRes,
                             @Param("id") Long id );
}
