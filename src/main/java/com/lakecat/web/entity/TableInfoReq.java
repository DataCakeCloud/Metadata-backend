package com.lakecat.web.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@ApiModel("table请求参数")
public class TableInfoReq {

    @ApiModelProperty(value = "所属区域", required = true)
    protected String region;
    @ApiModelProperty(value = "数据库名称", required = true)
    protected String databaseName;
    @ApiModelProperty(value = "表名称", required = true)
    protected String tableName;

    @ApiModelProperty(value = "租户信息", required = true)
    protected String tenantName;

    protected String dbName;

    protected String userGroup;

    protected Integer pageNum;

    protected Integer pageSize;

    public List<String> typeList;

    public String getDatabaseName() {
        if (StringUtils.isNotEmpty(dbName)) {
            return dbName;
        }
        return databaseName;
    }

}
