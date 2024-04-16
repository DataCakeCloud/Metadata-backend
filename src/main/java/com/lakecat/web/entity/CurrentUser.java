package com.lakecat.web.entity;

import com.alibaba.fastjson.annotation.JSONField;

import com.lakecat.web.constant.CatalogNameEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * @author wuyan
 * @date 2019/8/7
 **/
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class CurrentUser {
    private int id;
    private String userId;
    private String userName;

    /**
     * 部门
     */
    @JSONField(name = "org")
    private String groupName;

    /**
     * 项目，梁又择使用
     */
    @JSONField(name = "group")
    private String group;

    private boolean isAdmin;
    private int tenantId;
    private String roles;
    private String tenantName;

    /**
     * 用户组id，可能存在多个，用逗号隔开
     */
    private String groupIds;

    private Boolean flag;


    private Map <String, CatalogNameEnum.CloudRegionCatalog> regionInfo;
}

