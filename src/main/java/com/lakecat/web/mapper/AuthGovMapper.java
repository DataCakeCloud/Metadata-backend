package com.lakecat.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lakecat.web.entity.AuthorityGovInfo;
import com.lakecat.web.entity.SyncNameInfo;
import com.lakecat.web.entity.TableInfo;
import org.apache.ibatis.annotations.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-13
 */
@Mapper
public interface AuthGovMapper extends BaseMapper <AuthorityGovInfo> {


    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     *
     * @return List <TableInfo>  查询列表
     */
    @Select("<script>" +
            "select * from authority_gov_info where execute_status = 0 and operate='取消' " +
            "</script>")
    List <AuthorityGovInfo> search();


    @Update("update authority_gov_info set " +
            "execute_status=1 " +
            "where id=#{id}")
    void updateByIdForSync(@Param("id") Long id);


    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     *
     * @return List <TableInfo>  查询列表
     */
    @Delete("<script>" +
            "delete from table_info where region=#{data.region} and name=#{data.name} and db_name=#{data.dbName}" +
            "</script>")
    boolean deleteTableForBol(@Param("data") TableInfo tableInfo);


    @Insert("insert into authority_gov_info" +
            "(table_name," +
            "operator," +
            "operate," +
            "permission," +
            "operated_user," +
            "user_name," +
            "reason," +
            "cycle," +
            "execute_status) " +
            "VALUES(" +
            "#{data.tableName}," +
            "#{data.operator}," +
            "#{data.operate}," +
            "#{data.permission}," +
            "#{data.operatedUser}," +
            "#{data.userName}," +
            "#{data.reason}," +
            "#{data.cycle}," +
            "#{data.executeStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "data.id")
    @Transactional(rollbackFor = Exception.class)
    boolean insertForRecord(@Param("data") AuthorityGovInfo permission);
}
