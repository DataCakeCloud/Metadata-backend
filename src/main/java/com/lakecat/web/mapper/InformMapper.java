package com.lakecat.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lakecat.web.entity.*;
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
public interface InformMapper extends BaseMapper <InformInfo> {


    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     *
     * @return List <TableInfo>  查询列表
     */
    @Select("<script>" +
            "select * from table_info where region=#{data.region} and name=#{data.name} and db_name=#{data.dbName}" +
            "</script>")
    TableInfo search(@Param("data") TableInfo tableInfo);


    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     *
     * @return List <TableInfo>  查询列表
     */
    @Delete("<script>" +
            "delete from table_info where region=#{data.region} and name=#{data.name} and db_name=#{data.dbName}" +
            "</script>")
    boolean deleteTableForBol(@Param("data") TableInfo tableInfo);




    @Insert("insert into table_change_record" +
            "(table_name," +
            "db_name," +
            "region," +
            "operation," +
            "created_time," +
            "operation_user," +
            "message," +
            "args," +
            "inform_list) " +
            "VALUES(" +
            "#{data.tableName}," +
            "#{data.dbName}," +
            "#{data.region}," +
            "#{data.operation}," +
            "#{data.createdTime}," +
            "#{data.operationUser}," +
            "#{data.message}," +
            "#{data.args}," +
            "#{data.informList})")
    @Options(useGeneratedKeys = true, keyProperty = "data.id")
    @Transactional(rollbackFor = Exception.class)
    boolean insertForTableChange(@Param("data") InformInfo permission);
}
