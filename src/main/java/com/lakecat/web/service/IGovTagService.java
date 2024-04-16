package com.lakecat.web.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lakecat.web.entity.GovTagEntity;

public interface IGovTagService extends IService<GovTagEntity> {

    /**
     * 获取标签
     * @param jobName
     * @return
     */
    List<GovTagEntity> getJobTags(String jobName);

    /**
     * 获取表本身的tags
     * @param qualifiedName
     * @return
     */
    List<GovTagEntity> getTableTags(String qualifiedName);

    /**
     * 获取表访问频次标签
     * @param value
     * @return
     */
    String getTableVisitTimesTag(Double value) throws Exception;

    /**
     * 查找精确的标签
     * @param qualifiedName
     * @param tagName
     * @return
     * @throws Exception
     */
    GovTagEntity getSingleTag(String qualifiedName, String tagName) ;

    /**
     * 根据标签获取对象
     * @param tagName
     * @return
     * @throws Exception
     */
    List<GovTagEntity> getObjectByTagName(String tagName) ;

}
