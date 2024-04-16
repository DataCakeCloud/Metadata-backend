package com.lakecat.web.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lakecat.web.entity.BillOwnerDepartment;
import com.lakecat.web.entity.GovJobInfo;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-11
 */
public interface IGovService extends IService<GovJobInfo> {

    GovJobInfo getJobIdByName(String jobName);

    /**
     * 获取 owner 汇总统计数据
     * @param owner
     * @return
     */
    Map<String, Double> getOwnerSummaryStatistics(String owner);
}
