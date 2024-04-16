package com.lakecat.web.entity;

import lombok.Data;

import java.util.Map;

@Data
public class RoleInputs {
    private String roleName;
    private String ownerUser;
    private String[] operation;
    private String objectType;
    private String[] objectName;
    private String[] objectNames;
    private String[] userIds;
    private String groupIds;//用户组的id，用逗号分割
    private Map operations;
    private String[] roleNameList;
    private String type;
    private String[] scope;

}