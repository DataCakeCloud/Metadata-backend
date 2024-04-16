package com.lakecat.web.service.impl;

import java.util.Map;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lakecat.web.entity.GovJobInfo;
import com.lakecat.web.mapper.GovMapper;
import com.lakecat.web.service.IGovService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-11
 */
@Service
public class GovServiceImpl extends ServiceImpl <GovMapper, GovJobInfo> implements IGovService {

    @Autowired
    GovMapper govMapper;


    @Override
    public GovJobInfo getJobIdByName(String jobName) {

        return govMapper.getJobIdByName(jobName);

    }

    @Override
    public Map<String, Double> getOwnerSummaryStatistics(String owner) {
        return govMapper.getOwnerSummaryStatistics(owner);
    }
}
