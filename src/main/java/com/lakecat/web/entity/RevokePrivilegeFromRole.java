package com.lakecat.web.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RevokePrivilegeFromRole {
    private RoleInputs[] roleInputs;
    private String roleName;
    public Long permissionTableId;

    public RoleInputs[] getRoleInputs() { return this.roleInputs; }

    public String getRoleName() { return this.roleName; }

    public void setRoleInputs(RoleInputs[] roleInputs) { this.roleInputs = roleInputs; }

    public void setRoleName(String roleName) { this.roleName = roleName; }

}
