package com.lakecat.web.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lakecat.web.entity.GovTagEntity;
import com.lakecat.web.entity.OperateLogEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

@Mapper
public interface GovTagMapper extends BaseMapper <GovTagEntity> {


    /**
     * 获取数据源列表
     *
     * @return 数据源资源信息
     */
    @Select("<script>" +
            "select * " +
            "from governance_tags \n" +
            " where status=0 " +
            "</script>")
    List <OperateLogEntity> getAllTags();

    /**
     * tag_type = 0 代表为 job
     * @param jobName
     * @return
     */
    @Select("<script>" +
        "select * " +
        "from governance_tags \n" +
        " where status=0 and tag_type=0 and object_name=#{object_name} " +
        "</script>")
    List <GovTagEntity> getJobTags(@Param("object_name") String jobName);

    /**
     * tag_type = 0 代表为 job
     * @param tableQualifiedName
     * @return
     */
    @Select("<script>" +
        "select * " +
        "from governance_tags \n" +
        " where status=0 and tag_type=1 and object_name=#{object_name} " +
        "</script>")
    List <GovTagEntity> getTableTags(@Param("object_name") String tableQualifiedName);

    @Select("<script>" +
        "select * " +
        "from governance_tags \n" +
        " where status=0 and tag_name=#{tag_name} and object_name=#{object_name} limit 1" +
        "</script>")
    GovTagEntity getSingleTag(@Param("object_name")String qualifiedName, @Param("tag_name") String tagName);

    @Select("<script>" +
        "select * " +
        "from governance_tags \n" +
        " where status=0 and tag_name=#{tag_name} " +
        "</script>")
    List <GovTagEntity> getObjectByTagName(@Param("tag_name") String tagName);
}
