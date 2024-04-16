package com.lakecat.web.mapper;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lakecat.web.entity.CollectInfo;
import com.lakecat.web.entity.DictInfo;
import com.lakecat.web.entity.OwnerNotDefaultBucketRecord;
import com.lakecat.web.entity.SyncNameInfo;
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
public interface CollectMapper extends BaseMapper <CollectInfo> {


    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     *
     * @return List <TableInfo>  查询列表
     */
    @Select("<script>" +
            "select * from collect_info where table_id=#{data.tableId} and user_id=#{data.userId}" +
            "</script>")
    List <CollectInfo> search(@Param("data") CollectInfo collectInfo);

    @Select("<script>" +
            "select * from collect_info where sole=#{data.sole} and user_id=#{data.userId}  and status=1  " +
            "</script>")
    List <CollectInfo> searchNew(@Param("data") CollectInfo collectInfo);


    @Select("<script>" +
            "select count(*) from collect_info where table_id=#{tableId} and status=1" +
            "</script>")
    Long count(@Param("tableId") Long tableId);


    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     *
     * @return List <TableInfo>  查询列表
     */
    @Select("<script>" +
            "select * from collect_info where status=1 and table_id=#{data.tableId} and user_id=#{data.userId}" +
            "</script>")
    CollectInfo searchOne(@Param("data") CollectInfo collectInfo);


    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     *
     * @return List <TableInfo>  查询列表
     */
    @Select("<script>" +
            "select table_id from collect_info where status=1 and user_id=#{data.userId}" +
            "</script>")
    List <Long> searchListByUserName(@Param("data") CollectInfo collectInfo);




    @Select("<script>" +
            "select user_id as userId,group_concat(table_id) as tableList from  collect_info where table_id is not null group by `user_id`" +
            "</script>")
    List <JSONObject> searchList();


    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     *
     * @return List <TableInfo>  查询列表
     */
    @Select("<script>" +
            "select sole as tableName ,create_time  from  collect_info " +
            " where status=1 and user_id=#{data.userId} and sole is not null order by update_time desc " +
//            "<if test = 'data.pageNum != -1'> " +
//            "  limit #{data.pageNum},#{data.pageSize}" +
//            "</if>" +
            "</script>")
    List <CollectInfo> searchInfoByUserName(@Param("data") CollectInfo collectInfo);



    @Select("<script>" +
            "select a.table_id,b.table_ws as tableName from collect_info a left join (select id,concat_ws('.', db_name, name) table_ws from table_info where name is not null) b" +
            " on a.table_id=b.id" +
            " where status=1 and user_id=#{data.userId} and b.table_ws is not null order by update_time desc" +
            "<if test = 'data.size != -1'> " +
            " limit #{data.size} " +
            "</if>" +
            "</script>")
    List <CollectInfo> searchInfoByUserNameSize(@Param("data") CollectInfo collectInfo);


    @Update("update collect_info set " +
            "status=#{data.status},update_time=#{data.updateTime} " +
            "where table_id=#{data.tableId} and user_id=#{data.userId}")
    void updateByName(@Param("data") CollectInfo collectInfo);

    @Update("update collect_info set " +
            "status=#{data.status},update_time=#{data.updateTime} " +
            "where sole=#{data.sole} and user_id=#{data.userId}")
    void updateByNameNew(@Param("data") CollectInfo collectInfo);

    @Insert("insert into collect_info" +
            "(sole," +
            "user_id," +
            "status) " +
            "VALUES(" +
            "#{data.sole}," +
            "#{data.userId}," +
            "#{data.status})")
    @Options(useGeneratedKeys = true, keyProperty = "data.id")
    @Transactional(rollbackFor = Exception.class)
    boolean insertForCollect(@Param("data") CollectInfo collectInfo);

    /**
     * 通过唯一建查 catalogname.dbname.tablename
     *
     * @return List <TableInfo>  查询列表
     */
    @Select({"<script>" +
            "select sole,count(*) as collect,group_concat(user_id) as userList from collect_info " +
            "where status = 1  " +
            "<if test='list!=null'>  and sole in  " +
            "   <foreach collection='list' item='key' open='(' separator=',' close=')'>" +
            "   #{key}" +
            "   </foreach>" +
            "</if>" +
            " group by sole" +
            "</script>"})
    List<CollectInfo> searchBykeys(@Param("list") List<String> list);
}
