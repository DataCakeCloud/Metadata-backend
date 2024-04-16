package com.lakecat.web;

/**
 * Created by slj on 2022/5/30.
 */
public class Demo {
    private Integer id;
    private String dbName;
    private String interval;
    private String hierarchical;
    private String subject;
    private String application;
    private String tableName;
    private String region;
    private String description;
    private String owner;
    private String role;
    private String flag;
    private String power;
    private String newOwner;
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNewOwner() {
        return newOwner;
    }

    public void setNewOwner(String newOwner) {
        this.newOwner = newOwner;
    }

    private String inputGuids;

    public String getInputGuids() {
        return inputGuids;
    }

    public void setInputGuids(String inputGuids) {
        this.inputGuids = inputGuids;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getHierarchical() {
        return hierarchical;
    }

    public void setHierarchical(String hierarchical) {
        this.hierarchical = hierarchical;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Demo{" +
                "id=" + id +
                ", dbName='" + dbName + '\'' +
                ", interval='" + interval + '\'' +
                ", hierarchical='" + hierarchical + '\'' +
                ", subject='" + subject + '\'' +
                ", application='" + application + '\'' +
                ", tableName='" + tableName + '\'' +
                ", region='" + region + '\'' +
                ", description='" + description + '\'' +
                ", owner='" + owner + '\'' +
                ", role='" + role + '\'' +
                ", flag='" + flag + '\'' +
                ", power='" + power + '\'' +
                '}';
    }
}
