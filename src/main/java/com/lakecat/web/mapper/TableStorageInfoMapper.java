package com.lakecat.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lakecat.web.entity.DepartmentRegionRouteInfo;
import com.lakecat.web.entity.LastActivityInfo;
import com.lakecat.web.entity.OwnerNotDefaultBucketRecord;
import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.entity.table.TableStorageInfo;
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
public interface TableStorageInfoMapper extends BaseMapper <TableStorageInfo> {

    @Select("<script>" +
            "delete from table_storage_info where  dt = '${dt}'" +
            "</script>")
    void deleteByDt(@Param("dt") String dt);

    @Select("<script>" +
        "select * from table_storage_info where region=#{region} and db_name=#{databaseName} and table_name=#{tableName} order by dt desc limit 1" +
        "</script>")
    List<TableStorageInfo> getLatestRecord(String region, String databaseName, String tableName);

    /**
     * 获取当前人下的僵尸数据量
     * @param owner
     * @param currDate
     * @param interval
     * @return
     */
    @Select("<script>" +
        "select sum(total_storage) total_storage, "
        + "  sum(if(t2.region is null, total_storage, null)) zombie_size, "
        + "  count(if(t2.region is null, 1, null)) zombie_count "
        + "from ("
        + "select * from (\n"
        + "select \n"
        + "  if(@region = a.region and @db_name = a.db_name and @table_name = a.table_name, @n := @n + 1, @n := 1) as row_cnt, a.* ,\n"
        + "  @region:=a.region,\n"
        + "  @db_name:=a.db_name,\n"
        + "  @table_name:=a.table_name\n"
        + "from ( select * from table_storage_info where owner=#{owner} order by region, db_name, table_name, dt desc ) a,(select @n:=0, @region:=null,@db_name:=null,@table_name:=null) b \n"
        + ") t where row_cnt=1 "
        + ") t1 left join ("
            + "select region, db_name, table_name from last_activity_info group by region, db_name, table_name"
        + ") t2 on t1.region=t2.region and t1.db_name=t2.db_name and t1.table_name=t2.table_name " +
        "</script>")
    Map<String, Double> getZombieTables(String owner, String currDate, Integer interval);

    /**
     * 获取当前人下的僵尸数据量
     * @param owner
     * @return
     */
    @Select("<script>" +
        "select sum(total_storage) total_storage, "
        + "  sum(if(t2.region is null, total_storage, null)) zombie_size, "
        + "  count(if(t2.region is null, 1, null)) zombie_count "
        + "from ("
        + "select * from (\n"
        + "select \n"
        + "  if(@region = a.region and @db_name = a.db_name and @table_name = a.table_name, @n := @n + 1, @n := 1) as row_cnt, a.* ,\n"
        + "  @region:=a.region,\n"
        + "  @db_name:=a.db_name,\n"
        + "  @table_name:=a.table_name\n"
        + "from ( select * from table_storage_info where owner=#{owner} order by region, db_name, table_name, dt desc ) a,(select @n:=0, @region:=null,@db_name:=null,@table_name:=null) b \n"
        + ") t where row_cnt=1 "
        + ") t1 left join (" +
        "select "
        + "region, db_name, table_name "
        + "from ("
        + "select region, db_name, table_name from last_activity_info where sole in (select concat_ws('.',t2.region,concat_ws('.',t2.db_name, t2.name)) from table_info " +
        " where owner=#{owner}  and  name is not null and region is not null and  db_name is not null) group by region, db_name, table_name"
        + ") t"
        + ") t2 on t1.region=t2.region and t1.db_name=t2.db_name and t1.table_name=t2.table_name " +
        "</script>")
    Map<String, Double> getOwnerZombieTables(String owner);


    /**
     * 获取最新的刷新记录
     * @return
     */
    @Select({"" +
        "select * from (\n"
        + "select \n"
        + "  if(@region = a.region and @db_name = a.db_name and @table_name = a.table_name, @n := @n + 1, @n := 1) as row_cnt, a.* ,\n"
        + "  @region:=a.region,\n"
        + "  @db_name:=a.db_name,\n"
        + "  @table_name:=a.table_name\n"
        + "from ( select * from table_storage_info order by region, db_name, table_name, dt desc ) a,(select @n:=0, @region:=null,@db_name:=null,@table_name:=null) b \n"
        + ") t where row_cnt=1 "})
    List<TableStorageInfo> getAllLatestRecord();


    /**
     * 获取最新天的刷新记录
     * @return
     */
    @Select({"select * from table_storage_info where  dt = '${dt}'"})
    List<TableStorageInfo> getAllLatestRecord2(@Param("dt") String dt);

}
