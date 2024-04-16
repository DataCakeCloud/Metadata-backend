package com.lakecat.web.service.impl;

import java.util.List;
import javax.annotation.Resource;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lakecat.web.entity.GovTagEntity;
import com.lakecat.web.mapper.GovTagMapper;
import com.lakecat.web.service.IGovTagService;
import com.lakecat.web.utils.CacheUtils;
import com.lakecat.web.utils.TagUtils;
import net.jodah.expiringmap.ExpiringMap;
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
public class GovTagServiceImpl extends ServiceImpl <GovTagMapper, GovTagEntity> implements IGovTagService {

    private static ExpiringMap<Object, Object> tagsCacheMap = CacheUtils.getCacheMap(20000, 12 * 60 * 60, Object.class, Object.class);

    @Resource
    GovTagMapper govTagMapper;


    @Override
    public List<GovTagEntity> getJobTags(String jobName) {
        if (tagsCacheMap.containsKey(jobName)) {
            return (List<GovTagEntity>)tagsCacheMap.get(jobName);
        }
        List<GovTagEntity> tags = govTagMapper.getJobTags(jobName);
        tagsCacheMap.put(jobName, tags);
        return tags;
    }

    @Override
    public List<GovTagEntity> getTableTags(String qualifiedName) {
        if (tagsCacheMap.containsKey(qualifiedName)) {
            return (List<GovTagEntity>)tagsCacheMap.get(qualifiedName);
        }
        List<GovTagEntity> tableTags = govTagMapper.getTableTags(qualifiedName);
        tagsCacheMap.put(qualifiedName, tableTags);
        return tableTags;
    }

    @Override
    public String getTableVisitTimesTag(Double value) throws Exception {
        return TagUtils.assertTagValue(value, "访问频次", new double[]{
            0, 3, 28
        }, TagUtils.SIZE_GROUP_DESC1);
    }

    @Override
    public GovTagEntity getSingleTag(String qualifiedName, String tagName) {
        String tagQualifiedName = String.format("%s.%s", qualifiedName, tagName);
        if (tagsCacheMap.containsKey(tagQualifiedName)) {
            return (GovTagEntity)tagsCacheMap.get(tagQualifiedName);
        }
        GovTagEntity govTag = govTagMapper.getSingleTag(qualifiedName, tagName);
        tagsCacheMap.put(tagQualifiedName, govTag);
        return govTag;
    }

    @Override
    public List<GovTagEntity> getObjectByTagName(String tagName) {
        return govTagMapper.getTableTags(tagName);
    }
}
