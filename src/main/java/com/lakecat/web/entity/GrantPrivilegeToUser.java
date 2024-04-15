package com.lakecat.web.entity;

import java.util.List;

public class GrantPrivilegeToUser {

    private RoleInputs roleInputs;
    private String userName;
    private String department;
    private String owner;
    private String cycle;
    private String reason;
    private String coApplicants;

    public String getCoApplicants() {
        return coApplicants;
    }

    public void setCoApplicants(String coApplicants) {
        this.coApplicants = coApplicants;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getDepartment() {
        return this.department;
    }

    public RoleInputs getRoleInputs() {
        return this.roleInputs;
    }

    public String getOwner() {
        return this.owner;
    }

    public String getReason() {
        return this.reason;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setRoleInputs(RoleInputs roleInputs) {
        this.roleInputs = roleInputs;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
