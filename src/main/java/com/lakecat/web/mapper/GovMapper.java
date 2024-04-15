package com.lakecat.web.mapper;

import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lakecat.web.entity.BillOwnerDepartment;
import com.lakecat.web.entity.GovJobInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-13
 */
@Mapper
public interface GovMapper extends BaseMapper <GovJobInfo> {


    @Select("select * from gov_job_info where job_name=#{jobName}")
    GovJobInfo getJobIdByName(@Param("jobName") String jobName);

    /**
     * 获取 owner 汇总统计数据
     * @param owner
     * @return
     */
    @Select("select sum(1) task_count, avg(cpu_use_ratio) cpu_use_ratio, avg(mem_use_ratio) mem_use_ratio from gov_job_info where owner=#{owner}")
    Map<String, Double> getOwnerSummaryStatistics(@Param("owner") String owner);
}
