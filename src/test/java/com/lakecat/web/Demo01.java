package com.lakecat.web;

import com.alibaba.fastjson.JSONArray;

/**
 * Created by slj on 2022/5/30.
 */
public class Demo01 {
    private Integer id;
    private String region;
    private String dbName;
    private String tableName;
    private String name;
    private String comment;
    private String type;
    private JSONArray columns;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JSONArray getColumns() {
        return columns;
    }

    public void setColumns(JSONArray columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "Demo01{" +
                "id=" + id +
                ", region='" + region + '\'' +
                ", dbName='" + dbName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", name='" + name + '\'' +
                ", comment='" + comment + '\'' +
                ", type='" + type + '\'' +
                ", columns='" + columns + '\'' +
                '}';
    }
}
