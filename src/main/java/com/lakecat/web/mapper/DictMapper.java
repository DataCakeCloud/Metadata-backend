package com.lakecat.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lakecat.web.entity.DictInfo;
import com.lakecat.web.entity.InformInfo;
import com.lakecat.web.entity.SyncNameInfo;
import com.lakecat.web.entity.TableInfo;
import org.apache.ibatis.annotations.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-13
 */
@Mapper
public interface DictMapper extends BaseMapper <DictInfo> {


    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     * @return List <TableInfo>  查询列表
     */
    @Select("<script>" +
            "select * from dict_info where dict_type=#{data.dictType} and status =0" +
            "</script>")
    List <DictInfo> search(@Param("data") DictInfo dictInfo);



    @Select("<script>" +
            "select col_1 as dbNameTableName ,col_2 as owner,status from table_create where col_2 not in ('zhanshulin','niuqianxiong','zhaoqingfa','xuecj') and (col_1 !=null or col_1 !='') and status=0" +
            "</script>")
    List <SyncNameInfo> syncName();

    @Update("update table_create set " +
            "status=#{data.status} " +
            "where col_1=#{data.dbNameTableName}")
    void updateByName(@Param("data") SyncNameInfo syncNameInfo);

    @Insert("insert into dict_info" +
            "(dict_type," +
            " `key`," +
            "value)" +
            "VALUES(" +
            "#{data.dictType}," +
            "#{data.key}," +
            "#{data.value})")
    @Options(useGeneratedKeys = true, keyProperty = "data.id")
    @Transactional(rollbackFor = Exception.class)
    boolean insertDictKey(@Param("data") DictInfo dictInfo);
}
