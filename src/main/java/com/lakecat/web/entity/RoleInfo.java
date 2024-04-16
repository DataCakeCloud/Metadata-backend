package com.lakecat.web.entity;

import io.swagger.annotations.ApiModelProperty;

public class RoleInfo {
    @ApiModelProperty(value = "角色owner")
    private String ownerUser;

    @ApiModelProperty(value = "角色名")
    private String roleName;

    @ApiModelProperty(value = "创建信息")
    private String comment;

    @ApiModelProperty(value = "旧角色名")
    private String oldName;

    @ApiModelProperty(value = "新角色名")
    private String newName;

    public String getRoleName() {
        return this.roleName;
    }

    public String getOwnerUser(){ return this.ownerUser; }

    public String getComment(){ return this.comment; }

    public String getOldName(){ return this.oldName; }

    public String getNewName(){ return this.newName; }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setOwnerUser(String ownerUser){ this.ownerUser = ownerUser; }

    public void setComment(String comment){ this.comment = comment; }

    public void setOldName(String oldName){ this.oldName = oldName; }

    public void setNewName(String newName){ this.newName = newName; }
}
