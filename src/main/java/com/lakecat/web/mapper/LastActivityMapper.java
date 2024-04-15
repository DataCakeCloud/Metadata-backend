package com.lakecat.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lakecat.web.entity.CollectInfo;
import com.lakecat.web.entity.LastActivityInfo;
import com.lakecat.web.entity.TableForLastActivity;
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
public interface LastActivityMapper extends BaseMapper <LastActivityInfo> {


    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     *
     * @return List <TableInfo>  查询列表
     */
    @Select("<script>" +
            "select user_id from last_activity_info where table_name=#{data.tableName} and db_name=#{data.dbName} and region=#{data.region}" +
            " and user_id !='root' order by sum_count desc limit 3" +
            "</script>")
    List <String> search(@Param("data") LastActivityInfo lastActivityInfo);

    @Select("<script>" +
        "select "
            + "sum(sum_count) table_times, sum(1) table_count, avg(sum_count) table_times_avg "
        + "from ("
            + "select sum(sum_count) sum_count, table_id from last_activity_info where table_id in (select id from table_info where owner=#{owner}) group by table_id"
        + ") t" +
        "</script>")
    Map<String, Double> ownerActiveTableStat(@Param("owner") String owner);

    @Select("<script>" +
            "select "
            + "sum(sum_count) table_times, sum(1) table_count, avg(sum_count) table_times_avg "
            + "from ("
            + "select sum(sum_count) sum_count, table_id from last_activity_info where sole in (" +
            " select concat_ws('.',region,concat_ws('.',db_name, name)) from table_info where owner=#{owner} " +
            " name is not null and region is not null and  db_name is not null ) group by table_id"
            + ") t" +
            "</script>")
    Map<String, Double> ownerActiveTableStatNew(@Param("owner") String owner);

    @Select("<script>" +
        "select sum(sum_count) table_times, count(1) table_count from (select sum(sum_count) sum_count, table_id from last_activity_info group by table_id) t " +
        "</script>")
    Map<String, Double> sumActiveTableStat();

    @Select("<script>" +
            "select sum(sum_count) table_times, count(1) table_count from (select sum(sum_count) sum_count, sole from last_activity_info group by sole) t " +
            "</script>")
    Map<String, Double> sumActiveTableStatNew();



    @Select("<script>" +
            " select a.table_ws from (\n" +
            "            select id,\n" +
            "            concat_ws('_','shareit',concat_ws('.',region,concat_ws('.', db_name, table_name))) table_ws,\n" +
            "            group_concat(user_id) from last_activity_info group by table_id   \n" +
            ") a where a.table_ws in ${tableName}" +
            "</script>")
    List <String> searchLastActivity(@Param("tableName") String tableName);

    @Select("<script>" +
            " select a.table_ws from (\n" +
            "            select id,\n" +
            "            concat_ws('_','shareit',concat_ws('.',region,concat_ws('.', db_name, table_name))) table_ws,\n" +
            "            group_concat(user_id) from last_activity_info group by sole   \n" +
            ") a where a.table_ws in ${tableName}" +
            "</script>")
    List <String> searchLastActivityNew(@Param("tableName") String tableName);


    /**
     * 通过唯一建查 catalogname.dbname.tablename
     *
     * @return List <TableInfo>  查询列表
     */
    @Select({"<script>" +
            "select sole,substring_index(group_concat(distinct user_id order by sum_count desc),',',3) " +
            " as userList,sum(sum_count) as count from last_activity_info " +
            " where status = 1  " +
            "<if test='list!=null'>  and sole in  " +
            "   <foreach collection='list' item='key' open='(' separator=',' close=')'>" +
            "   #{key}" +
            "   </foreach>" +
            "</if>" +
            " group by sole" +
            "</script>"})
    List<LastActivityInfo> searchBykeys(@Param("list") List<String> list);

}
