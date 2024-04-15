package com.lakecat.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lakecat.web.entity.DictInfo;
import com.lakecat.web.entity.SyncNameInfo;
import com.lakecat.web.entity.TableTagInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
public interface TableTagInfoMapper extends BaseMapper <TableTagInfo> {


    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     *
     * @return List <TableInfo>  查询列表
     */
    @Select("<script>" +
            "select id,`table_id`,`key`,`value` from table_tag_info where sole=#{sole}" +
            "</script>")
    List <TableTagInfo> searchBySole(@Param("sole") String sole);

    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     *
     * @return List <TableInfo>  查询列表
     */
    @Select("<script>" +
            "select id,`table_id`,`key`,`value` from table_tag_info where table_id=#{tableId}" +
            "</script>")
    List <TableTagInfo> search(@Param("tableId") Long tableId);


    void batchSaveForTags(@Param("list") List <TableTagInfo> list);

    /**
     * 通过唯一建查 catalogname.dbname.tablename
     *
     * @return List <TableInfo>  查询列表
     */
    @Select({"<script>" +
            "SELECT sole, GROUP_CONCAT(CONCAT(`key`, '=', `value`) SEPARATOR ',') AS tags" +
            " FROM table_tag_info  " +
            " where status = 1  " +
            "<if test='list!=null'>  and sole in  " +
            "   <foreach collection='list' item='key' open='(' separator=',' close=')'>" +
            "   #{key}" +
            "   </foreach>" +
            "</if>" +
            " group by sole" +
            "</script>"})
    List<TableTagInfo> searchBykeys(@Param("list") List<String> list);

}
