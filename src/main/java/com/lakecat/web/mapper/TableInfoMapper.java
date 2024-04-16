package com.lakecat.web.mapper;

import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.entity.*;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lakecat.web.vo.blood.UserGroupRelation;
import com.lakecat.web.vo.blood.UserGroupVo;
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
public interface TableInfoMapper extends BaseMapper <TableInfo> {


    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     * 废弃
     * @param tableInfo 查询条件
     * @return List <TableInfo>  查询列表
     */
    @Select("<script>" +
            "select a.id,a.`name`," +
            "a.db_name as dbName," +
            "a.`owner`,a.byte_size as byteSize, " +
            "a.partition_type as Partition_type, " +
            "a.cn_name as cnName," +
            "a.lifecycle as lifecycle," +
            "a.update_time as updateTime," +
            "a.create_time as createTime," +
            "a.application,a.hierarchical," +
            "a.num_rows as numRows, " +
            "a.`subject`,a.`interval`," +
            "a.`description`,a.region," +
            "a.`location`,a.subject," +
            "a.update_type as updateType " +
            "from (select *,concat_ws('.', db_name, name) table_ws from table_info) a left join " +
            "(select table_info_name,count(*) as count from table_info_search_history group by table_info_name) b  " +
            "on a.table_ws = b.table_info_name " +
            "where 1 = 1 and a.status=0 " +
            "<if test = 'data.region != \"\"'> " +
            "and a.region = #{data.region} " +
            "</if>" +
            "<if test = 'data.dbName != null'> " +
            "and a.db_name = #{data.dbName} " +
            "</if>" +
            "<if test = 'data.subject != \"\"'> " +
            "and a.subject = #{data.subject} " +
            "</if>" +
            "<if test = 'data.keyWord != \"\"'> " +
            "and (a.owner like #{data.keyWord} or a.db_name like #{data.keyWord} or a.name like #{data.keyWord} or a.cn_name like #{data.keyWord} or a.region like #{data.keyWord} or a.description like  #{data.keyWord}) " +
            "</if>" +
            "order by b.count desc " +
            " limit #{data.page},#{data.limit}" +
            "</script>")
    List <TableInfo> search(@Param("data") TableInfo tableInfo);


    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     *废弃
     * @param tableInfo 查询条件
     * @return List <TableInfo>  查询列表
     */
    @Select("<script>" +
            "select\n" +
            "  a.id,\n" +
            "  a.`name`,\n" +
            "  a.db_name as dbName,\n" +
            "  a.`owner`,\n" +
            "  a.byte_size as byteSize,\n" +
            "  a.partition_type as Partition_type,\n" +
            "  a.cn_name as cnName,\n" +
            "  a.lifecycle as lifecycle,\n" +
            "  a.update_time as updateTime,\n" +
            "  a.create_time as createTime,\n" +
            "  a.application,\n" +
            "  a.hierarchical,\n" +
            "  a.num_rows as numRows,\n" +
            "  a.`subject`,\n" +
            "  a.`interval`,\n" +
            "  a.`description`,\n" +
            "  a.region,\n" +
            "  a.`location`,\n" +
            "  a.subject,\n" +
            "  a.update_type as updateType,\n" +
            "  b.userList,\n" +
            "  b.count,\n" +
            "  c.collect, \n" +
            "  d.tags, \n" +
            " case when c.userList like a.userName then 1 else 0 end as flag \n" +
            "from\n" +
            "  (select *,#{data.userName} as userName from table_info )  a left join \n" +
            " (select table_name,db_name,region,substring_index(group_concat(distinct user_id order by sum_count desc),',',3) as userList,sum(sum_count) as count from last_activity_info group by table_name,db_name,region) b \n" +
            " on a.name=b.table_name \n" +
            " and a.`db_name`=b.db_name \n" +
            " and a.region = b.region \n" +
            " left join (select table_id,count(*) as collect,group_concat(user_id) as userList from  collect_info where status = 1 and table_id is not null group by table_id ) c\n" +
            " on a.id = c.table_id \n" +
            " left join ( SELECT table_id, GROUP_CONCAT(CONCAT(`key`, '=', `value`) SEPARATOR ',') AS tags FROM table_tag_info GROUP BY table_id ) d\n" +
            " on a.id = d.table_id \n" +
            "where \n" +
            "  1 = 1\n" +
            "  and a.status = 0 " +
            "<if test = 'data.region != \"\"'> " +
            "and a.region = #{data.region} " +
            "</if>" +
            "<if test = 'data.dbName != null'> " +
            "and a.db_name = #{data.dbName} " +
            "</if>" +
            "<if test = 'data.subject != \"\"'> " +
            "and a.subject = #{data.subject} " +
            "</if>" +
            "<if test = 'data.keyWordForSearch != \"\"'> " +
            "and (a.name like #{data.keyWordForSearch} ) " +
//            "and (a.owner like #{data.keyWordForSearch} " +
//            "or a.db_name like #{data.keyWordForSearch} " +
//            "or a.name like #{data.keyWordForSearch} " +
//            "or a.cn_name like #{data.keyWordForSearch} " +
//            "or a.location like #{data.keyWordForSearch} " +
//            "or a.columns like #{data.keyWordForSearch} " +
//            "or a.description like  #{data.keyWordForSearch}) " +
            "</if>" +
            " limit #{data.page},#{data.limit}" +
            "</script>")
    List <TableInfo> searchNew(@Param("data") TableInfo tableInfo);


    /**
     * 废弃
     * @param tableInfo
     * @return
     */
    @Select("<script>" +
            "select\n" +
            " * \n" +
            "from table_info \n" +
            "where \n" +
            "  1 = 1\n" +
            "  and status = 0 " +
            "<if test = 'data.region != \"\"'> " +
            "and region = #{data.region} " +
            "</if>" +
            "<if test = 'data.dbName != null'> " +
            "and db_name = #{data.dbName} " +
            "</if>" +
            "<if test = 'data.subject != \"\"'> " +
            "and subject = #{data.subject} " +
            "</if>" +
            "<if test = 'data.keyWordForSearch != \"\"'> " +
            "and (name like #{data.keyWordForSearch} )" +
//            "and (owner like #{data.keyWordForSearch} " +
//            "or name like #{data.keyWordForSearch} " +
//            "or cn_name like #{data.keyWordForSearch} " +
//            "or location like #{data.keyWordForSearch} " +
//            "or columns like #{data.keyWordForSearch} " +
//            "or description like  #{data.keyWordForSearch}) " +
            "</if>" +
            "</script>")
    List<TableInfo> searchTable(@Param("data") TableInfo tableInfo);



    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     *废弃
     * @param tableInfo 查询条件
     * @return List <TableInfo>  查询列表
     */
    @Select("<script>" +
            "select\n" +
            "  a.id,\n" +
            "  a.`name`,\n" +
            "  a.db_name as dbName,\n" +
            "  a.`owner`,\n" +
            "  a.byte_size as byteSize,\n" +
            "  a.partition_type as Partition_type,\n" +
            "  a.cn_name as cnName,\n" +
            "  a.lifecycle as lifecycle,\n" +
            "  a.update_time as updateTime,\n" +
            "  a.create_time as createTime,\n" +
            "  a.application,\n" +
            "  a.hierarchical,\n" +
            "  a.num_rows as numRows,\n" +
            "  a.`subject`,\n" +
            "  a.`interval`,\n" +
            "  a.`description`,\n" +
            "  a.region,\n" +
            "  a.`location`,\n" +
            "  a.subject,\n" +
            "  a.update_type as updateType,\n" +
            "  b.userList,\n" +
            "  b.count,\n" +
            "  c.collect, \n" +
            " case when c.userList like a.userName then 1 else 0 end as flag \n" +
            "from\n" +
            "  (select *,#{data.userName} as userName from table_info )  a left join \n" +
            " (select table_name,db_name,region,substring_index(group_concat(distinct user_id order by sum_count desc),',',3) as userList,sum(sum_count) as count from last_activity_info group by table_name,db_name,region) b \n" +
            " on a.name=b.table_name \n" +
            " and a.`db_name`=b.db_name \n" +
            " and a.region = b.region \n" +
            " left join (select table_id,count(*) as collect,group_concat(user_id) as userList from  collect_info where status = 1 and  table_id is not null group by table_id ) c\n" +
            " on a.id = c.table_id \n" +
            " where a.id = #{data.id}" +
            "</script>")
    TableInfo searchOne(@Param("data") TableInfo tableInfo);

    /**
     * 通过owner、name、cn_name、description字段进行模式匹配进行查询
     *废弃
     * @param tableInfo 查询条件
     * @return List <TableInfo>  查询列表
     */
    @Select("<script>" +
            "select count(*) from table_info   where 1 = 1 and status=0" +
            "<if test = 'data.region != \"\"'> " +
            "and region = #{data.region} " +
            "</if>" +
            "<if test = 'data.dbName != null'> " +
            "and db_name = #{data.dbName} " +
            "</if>" +
            "<if test = 'data.subject != \"\"'> " +
            "and subject = #{data.subject} " +
            "</if>" +
            "<if test = 'data.keyWordForSearch != \"\"'> " +
            "and (name like #{data.keyWordForSearch} )" +
//            "and (owner like #{data.keyWordForSearch} " +
//            "or db_name like #{data.keyWordForSearch} " +
//            "or name like #{data.keyWordForSearch} " +
//            "or cn_name like #{data.keyWordForSearch} " +
//            "or columns like #{data.keyWordForSearch} " +
//            "or description like  #{data.keyWordForSearch}) " +
            "</if>" +
            "</script>")
    int count(@Param("data") TableInfo tableInfo);


    /**
     * 通过表id 获取表详情
     *已废弃
     * @param id 表id
     * @return TableInfo 表详情
     */
    @Select("select * from table_info where id = #{id}")
    TableInfo getTableDetail(@Param("id") Long id);


    /**
     * 通过表库名 表名 区域名 获取表id
     *
     * @param data 表id
     * @return TableInfo 表详情
     */
    @Select("select * from table_info where region =#{data.region} and db_name =#{data.dbName} and name =#{data.name}")
    List<TableInfo> getTableIdByName(@Param("data") TableInfo data);

    @Update("update table_info set" +
            " columns=#{data.columns} " +
            "  where db_name=#{data.dbName} and name=#{data.name} and region=#{data.region}")
    Boolean updateTableByKey(@Param("data") TableInfo data);


    /**
     * 通过表id 获取表详情
     *
     * @param id 表id
     * @return TableInfo 表详情
     */
    @Select("select id,columns,description,subject from table_info where id = #{id}")
    TableInfo column(@Param("id") Long id);


    /**
     * 组装库.表名并返回 且不为空
     *
     * @return List <String> 库.表列表
     */
    //同步暂时不用管
    @Select("select id,concat_ws('.',region,concat_ws('.', db_name, name)) name from table_info where name is not null")
    List <TableInfo> getNameAndIdMapByRegion();

    /**
     * 按region查询bug修复
     *
     * @return List <String> 库.表列表
     */
    //同步暂时不用管
    @Select("select create_type, region, `type`, db_name, description, partition_type, lifecycle, `name`,\n" +
            "        `columns`, partition_keys, location, sd_file_format, `status`,\n" +
            "        owner from table_info where name is not null and region=#{region}")
    List <TableInfo> getNameAndIdMapByRegion2(@Param("region") String region);



    /**
     * 按region查询bug修复
     *
     * @return List <String> 库.表列表
     */
    //同步暂时不用管
    @Select("select id,create_type, region, `type`, db_name, description, partition_type, lifecycle, `name`,\n" +
            "        `columns`, partition_keys, location, sd_file_format, `status`,\n" +
            "        owner from table_info where name is not null  ")
    List <TableInfo> getTableInfoList() ;


    /**
     * 组装库.表名并返回 且不为空
     * 已废弃
     * @return List <String> 库.表列表
     */
    @Select("<script>" +
            "  select " +
            "<if test = 'countFlag'> count(*) cnt </if>" +
            "<if test = '!countFlag'> region, db_name, name </if>"
            + "from table_info "
            + "where name is not null " +
            "<if test = 'keyword != \"\"'> " +
            "and ( concat_ws('.', region, db_name, name) like #{keyword} ) " +
            "</if>" +
            "<if test = '!countFlag'> order by region, db_name, name limit #{pageNum},#{pageSize} </if>" +
            "</script>")
    List <Map <String, Object>> getTableObjects(Boolean countFlag, String keyword, int pageSize, int pageNum);


    @Select("<script>" +
            "  select " +
            "<if test = 'countFlag'> count(*) cnt </if>" +
            "<if test = '!countFlag'> region, db_name </if>"
            + "from (select region, db_name from table_info where name is not null group by region, db_name) t where 1=1 " +
            "<if test = 'keyword != \"\"'> " +
            "and ( concat_ws('.', region, db_name) like #{keyword} ) " +
            "</if>" +
            "<if test = '!countFlag'> order by region,db_name limit #{pageNum},#{pageSize} </if>" +
            "</script>")
    List <Map <String, Object>> getDatabaseObjects(Boolean countFlag, String keyword, int pageSize, int pageNum);

    /**
     * 组装库.表名并返回 且不为空
     * 已废弃
     * @return List <String> 库.表列表
     */
    @Select("select id, db_name, name, region from table_info where name is not null and region=#{region} ")
    List <Map <String, String>> getAllTables(String region);

    // 已废弃
    @Select("select id from table_info where name is not null and columns is not null ")
    List <Long> getAllTableIds();

    // 已废弃
    @Select("select id from table_info where name is not null and columns is not null and owner=#{owner}")
    List <Long> getOwnerAllTableIds(String owner);

    // 已废弃
    @Select("select * from table_info where region=#{region} and db_name=#{dbName} and name=#{tableName}")
    List <TableInfo> selectTables(@Param("region") String region, @Param("dbName") String dbName, @Param("tableName") String tableName);

    /**
     * 更新收集上来的元数据信息
     *
     * @param tableInfo 元数据信息
     */
    @Update("update table_info set " +
            "type=#{data.type}, " +
            "columns=#{data.columns}, " +
            "location=#{data.location}, " +
            "owner=#{data.owner}, " +
            "partition_keys=#{data.partitionKeys}, " +
            "partition_type=#{data.partitionType}, " +
            "sd_file_format=#{data.sdFileFormat} " +
            "where db_name=#{data.dbName} and name=#{data.name} and region=#{data.region}")
    void updateTableInfo(@Param("data") TableInfo tableInfo);


    /**
     * 更新收集上来的元数据信息
     *
     * @param tableInfo 元数据信息
     */
    @Update("update table_info set " +
            "region=#{data.region}, " +
            "type=#{data.type}, " +
            "columns=#{data.columns}, " +
            "location=#{data.location}, " +
            "partition_keys=#{data.partitionKeys}, " +
            "partition_type=#{data.partitionType}, " +
            "sd_file_format=#{data.sdFileFormat} " +
            "where db_name=#{data.dbName} and name=#{data.name}")
    void updateTableInfoList(@Param("data") TableInfo tableInfo);


    /**
     * 通过表id 更新元数据所属用户
     *
     * @param id    表id
     * @param owner 用户名
     * @return boolean 是否更新成功
     */
    @Update("update table_info set owner=#{owner} where id = #{id}")
    boolean updateOwner(@Param("id") Long id, @Param("owner") String owner);

    @Update("update table_info set owner=#{data.owner} where region =#{data.region} and db_name =#{data.dbName} and name =#{data.name}")
    boolean updateOwnerByName(@Param("data") TableInfo data);

    /**
     * 通过库名 表名 删除对应数据
     *
     * @param dbName 库名
     * @param name   表名
     */
    @Delete("delete from table_info where db_name=#{dbName} and name=#{name} and region=#{region}")
    void deleteTable(@Param("dbName") String dbName, @Param("name") String name, @Param("region") String region);


    @Delete("delete from table_info where id=#{id}")
    void deleteTableById(@Param("id") Long id);


    @Select("select * from department_region_route_info where department=#{department} and region=#{region}")
    DepartmentRegionRouteInfo getBucketName(@Param("department") String department, @Param("region") String region);


    @Select("select * from table_data_info where table_name=#{tableName} and region=#{region} and size=#{size} ")
    TableDataInfo getSqlData(@Param("tableName") String tableName, @Param("region") String region, @Param("size") Integer size);

    @Insert("REPLACE INTO table_data_info" +
            "(table_name," +
            "region," +
            "sql_text," +
            "data," +
            "size) " +
            "VALUES(" +
            "#{data.tableName}," +
            "#{data.region}," +
            "#{data.sql}," +
            "#{data.data}," +
            "#{data.size})")
    @Transactional(rollbackFor = Exception.class)
    boolean insertForData(@Param("data") TableDataInfo tableDataInfo);


    @Insert("insert into owner_not_default_bucket_record" +
            "(owner," +
            "department," +
            "region," +
            "bucket_name," +
            "status) " +
            "VALUES(" +
            "#{data.owner}," +
            "#{data.department}," +
            "#{data.region}," +
            "#{data.bucketName}," +
            "#{data.status})")
    @Options(useGeneratedKeys = true, keyProperty = "data.id")
    @Transactional(rollbackFor = Exception.class)
    boolean insertForRecord(@Param("data") OwnerNotDefaultBucketRecord ownerForRecord);


    @Update("UPDATE table_info SET  `subject`=#{data.subject}, `interval`=#{data.interval}, cn_name=#{data.cnName}," +
            " description=#{data.description}, hierarchical=#{data.hierarchical}, update_time=#{data.updateTime}, " +
            "application=#{data.application},owner=#{data.owner} where id = #{data.id}")
    boolean updateDetailById(@Param("data") TableInfo entity);

    @Update("UPDATE table_info SET  `subject`=#{data.subject}, `interval`=#{data.interval}, cn_name=#{data.cnName}," +
            " description=#{data.description}, hierarchical=#{data.hierarchical}, update_time=#{data.updateTime}, " +
            "application=#{data.application},owner=#{data.owner} where   db_name=#{data.dbName} and name=#{data.name} and region=#{data.region}")
    boolean updateDetailByKey(@Param("data") TableInfo entity);


    // 废弃
//    @Update("UPDATE table_info SET  `subject`=#{subject}   where id = #{id}")
//    boolean updateSubjectById(@Param("subject") String subject,Long id);

    boolean batchUpdate(@Param("list") List <TableInfo> entity);


    boolean batchSave(@Param("list") List <TableInfo> entity);

    @Insert("INSERT INTO table_info (" +
            "create_type, " +
            "region," +
            "`type`," +
            "db_name," +
            "description," +
            "partition_type," +
            "lifecycle," +
            "`name`," +
            "`columns`," +
            "partition_keys," +
            "create_time," +
            "location," +
            "sd_file_format," +
            "`status`," +
            "owner," +
            "last_activity_count," +
            "transient_lastDdlTime)" +
            "VALUES(" +
            "#{data.createType}, " +
            "#{data.region}, " +
            "#{data.type}, " +
            "#{data.dbName}, " +
            "#{data.description}," +
            "#{data.partitionType}, " +
            "#{data.lifecycle}," +
            "#{data.name}," +
            "#{data.columns}," +
            "#{data.partitionKeys}," +
            "#{data.createTime}," +
            "#{data.location}," +
            "#{data.sdFileFormat}," +
            "#{data.status}," +
            "#{data.owner}," +
            "#{data.lastActivityCount}," +
            "#{data.transientLastDdlTime})")
    @Options(useGeneratedKeys = true, keyProperty = "data.id")
    @Transactional(rollbackFor = Exception.class)
    boolean insertForTableInfo(@Param("data") TableInfo entity);


    boolean batchSaveForUsers(@Param("list") List <LastActivityInfo> entity);

    boolean batchSaveForRoles(@Param("list") List <RoleOwnerRelevance> entity);

    @Select("select DISTINCT role_name from role_owner_relevance")
    List <RoleOwnerRelevance> selectRoles();


    boolean batchSaveForTables(@Param("list") List <RoleTableRelevance> entity);

    List<String> showDatabases();


    @Select("select * from (\n" +
            "select role_name,group_concat(privilege) as privilege \n" +
            "from role_table_relevance \n" +
            "where name=#{name} \n" +
            "group by role_name\n" +
            ") a left join (\n" +
            "select role_name,group_concat(user_name) as user_name from role_owner_relevance group by role_name \n" +
            ") b \n" +
            "on a.role_name=b.role_name ")
    List <RoleTableRelevance> getRoleByTableName(@Param("name") String name);


    @Select("select name from (\n" +
            "            select  role_name,name,privilege privilege \n" +
            "            from role_table_relevance where privilege=#{privilege} and name in ${name}\n" +
            "            ) a left join (\n" +
            "            select role_name,group_concat(user_name) as user_name from role_owner_relevance group by role_name \n" +
            "            ) b \n" +
            "            on a.role_name=b.role_name")
    List <String> getRoleByTableSelect(@Param("privilege") String privilege,@Param("name") String name);


    @Select("select role_name  \n" +
            "from role_table_relevance \n" +
            "where name=#{name} \n" +
            "group by role_name\n")
    List <String> getRoleList(@Param("name") String name);


    @Select("select * from (\n" +
            "select role_name,group_concat(privilege) as privilege \n" +
            "from role_table_relevance \n" +
            "where name = #{name} \n" +
            "group by role_name\n" +
            ") a left join (\n" +
            "select role_name,group_concat(user_name) as user_name from role_owner_relevance group by role_name \n" +
            ") b \n" +
            "on a.role_name=b.role_name ")
    List <RoleTableRelevance> getRoleListForTables(@Param("name") String name);


    @Select("select * from (\n" +
            "select role_name,group_concat(privilege) as privilege \n" +
            "from role_table_relevance \n" +
            "where name = #{name} \n" +
            "group by role_name\n" +
            ") a left join (\n" +
            "select role_name,group_concat(user_name) as user_name from role_owner_relevance group by role_name \n" +
            ") b \n" +
            "on a.role_name=b.role_name ")
    List <RoleTableRelevance> getRoleListForAllTables(@Param("name") String name);


    @Select("select * from table_data_info where table_name=#{tableName} and region=#{region} and size=#{size} ")
    TableDataInfo getOwnerByRoleName(@Param("tableName") String tableName, @Param("region") String region, @Param("size") Integer size);


    @Select("select * from table_data_info where table_name=#{tableName} and region=#{region} and size=#{size} ")
    TableDataInfo removeRelevance(@Param("tableName") String tableName, @Param("region") String region, @Param("size") Integer size);


    @Select("<script>" +
            "select * from table_info where  `owner` is not null and `owner` !='test' and `owner` !='bdp'" +
            "</script>")
    List <TableInfo> searchList();



    @Select("<script>" +
            "select * from table_info where  `owner` = #{owner}" +
            "</script>")
    List <TableInfo> searchListByOwner(@Param("owner") String owner);


    @Select("<script>" +
            "select data_level dataLevel, defining_elements definingElements,common_fields commonFields from sensitivity_level " +
            "</script>")
    List <SensitivityLevel> selectSensitivityLevel();

//    @Select("<script>" +
//            "select a.director from user_group u, ds_task.access_group a where u.parent_id is not null and a.delete_status=0 and u.parent_id=a.id and u.uuid=#{uuid} " +
//            "</script>")
//    List <String> selectUserGroupOrgLeader(@Param("uuid") String uuid);

    @Select("<script>" +
            "select a.director from user_group u, access_group a where u.parent_id is not null and a.delete_status=0 and u.parent_id=a.id and u.id=#{id} " +
            "</script>")
    List <String> selectUserGroupOrgLeaderByUserGroupId(@Param("id") Integer id);

//    @Select("<script>" +
//            "select email from ds_task.access_user where id=#{id}" +
//            "</script>")
//    String selectEmail(@Param("id") Integer id);

    @Select("<script>" +
            "select name from user_group where uuid=#{uuid}" +
            "</script>")
    String selectUserGroupName(@Param("uuid") String uuid);

    @Select("<script>" +
            "select name , uuid from user_group " +
            "</script>")
    List<UserGroupVo> selectAllUserGroupName();

    @Select("<script>" +
            "SELECT user_name userName ,user_group_id userGroupId  from user_group_relation where `owner`=0 and user_group_id in " +
            "(SELECT groups FROM actor where id in (select CONVERT(json_extract(runtime_config,'$.sourceId'),SIGNED) from task  where delete_status=0 and  " +
            "json_extract(json_extract(output_dataset,'$[0].metadata'),'$.table')=#{name}))" +
            "</script>")
    List<UserGroupRelation> actorLeader(@Param("name") String tableName);


    @Select("<script>" +
            "select b.user_name userName ,b.user_group_id userGroupId from user_group a, user_group_relation b where a.uuid=#{uuid} and a.id=b.user_group_id and b.owner=0 and a.delete_status=0" +
            "</script>")
    List<UserGroupRelation>  selectUserGroupLeader(@Param("uuid") String uuid);

    @Select("<script>" +
            "select b.user_name userName ,b.user_group_id userGroupId from user_group a, user_group_relation b where a.name=#{name} and a.id=b.user_group_id and b.owner=0 and a.delete_status=0" +
            "</script>")
    List<UserGroupRelation>  selectUserGroupLeaderByUserGroupName(@Param("name") String name);

    @Select("<script>" +
            "select uuid from user_group where name=#{name} and delete_status=0" +
            "</script>")
    String selectUserGroupUuid(@Param("name") String name);

}
