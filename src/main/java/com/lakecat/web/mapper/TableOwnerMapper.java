package com.lakecat.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lakecat.web.entity.*;
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
public interface TableOwnerMapper extends BaseMapper <TableOwnerInfo> {


    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     *
     * @return List <TableInfo>  查询列表
     */
    @Select("<script>" +
            "select count(*) from table_info where owner=#{owner}" +
            "</script>")
    int searchTableNum(@Param("owner") String owner);


    @Select("<script>" +
            "select count(*) from table_info where 1=1" +
            "</script>")
    int searchTableNumAll();


    @Select("<script>" +
            "select count(*) from role_owner_relevance where role_name in (\n" +
            "select role_name from role_table_relevance where name in (select concat_ws('.',concat_ws('_', 'shareit', region),concat_ws('.', db_name, name)) table_ws from table_info where owner=#{owner}))" +
            "</script>")
    int privilegeUser(@Param("owner") String owner);


    @Select("<script>" +
            "select count(*) from role_owner_relevance where 1=1" +
            "</script>")
    int privilegeUserAll();


    @Select("<script>" +
            "select sum(total_storage) from table_storage_info where owner=#{owner}\n" +
            "</script>")
    Long storageSize(@Param("owner") String owner);


    @Select("<script>" +
            "select sum(total_storage) from table_storage_info where 1=1" +
            "</script>")
    Long storageSizeAll();


    @Select("<script>" +
            "select\n" +
            "          count(DISTINCT name) \n" +
            "        from\n" +
            "          role_table_relevance\n" +
            "        where\n" +
            "          role_name in (\n" +
            "            select\n" +
            "              role_name\n" +
            "            from\n" +
            "              role_owner_relevance\n" +
            "            where\n" +
            "              user_name=#{owner}\n" +
            "          ) " +
            "</script>")
    int privilegeNum(@Param("owner") String owner);


    @Select("<script>" +
            "select\n" +
            "          count(DISTINCT name) \n" +
            "        from\n" +
            "          role_table_relevance\n" +
            "        where 1=1" +
            "</script>")
    int privilegeNumAll();


    /**
     * 1、该用户经常访问的表
     *
     * @param owner
     * @return
     */
    @Select("<script>" +
            "\n" +
            "select b.sum_count as num,a.* from last_activity_info b join table_info a \n" +
            "on a.region=b.region \n" +
            "and a.db_name=b.db_name \n" +
            "and a.name=b.table_name \n" +
            "where b.user_id =#{owner} order by b.sum_count desc limit 10" +
            "</script>")
    List <TableInfoForOwner> ownerTableTopN(@Param("owner") String owner);

    /**
     * 3、该用户创建的表 被经常访问的
     *
     * @param owner
     * @return
     */
    /*@Select("<script>" +
            "select region,db_name,name,sum(num) num from (select * from (\n" +
            "select a.*,IF(b.sum_count is null,'0',b.sum_count) as num from table_info a \n" +
            "left join last_activity_info b \n" +
            "on a.region=b.region \n" +
            "and a.db_name=b.db_name \n" +
            "and a.name=b.table_name \n" +
            "where owner=#{owner}) a ) t group by region,db_name,name order by num desc limit 10" +
            "</script>")*/

    @Select("<script>" +"select a.*,num from (select * from table_info where owner=#{owner} ) a "
        +
        " join (select region,db_name,table_name,sum(sum_count) num from last_activity_info group by region,db_name,table_name) b \n" +
        "on a.region=b.region \n" +
        "and a.db_name=b.db_name \n" +
        "and a.name=b.table_name \n" +
        "  order by num desc limit 10" +
        "</script>")
    List <TableInfoForOwner> tableOwnerTopN(@Param("owner") String owner);


    /**
     * 2、平台高频访问的表
     *
     * @param owner
     * @return
     */
    /*@Select("<script>" +
            "select region,db_name,name,sum(a.num) num from (\n" +
            "select a.*,IF(b.sum_count is null,'0',b.sum_count) as num from table_info a \n" +
            "left join last_activity_info b \n" +
            "on a.region=b.region \n" +
            "and a.db_name=b.db_name \n" +
            "and a.name=b.table_name \n" +
            "where 1=1) a group by region,db_name,name order by a.num desc limit 10" +
            "</script>")*/

    @Select("select *, last_activity_count as num from table_info order by last_activity_count desc limit 10")
    List <TableInfoForOwner> tableAllTopN(@Param("owner") String owner);
}
