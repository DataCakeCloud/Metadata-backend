package com.lakecat.web.entity;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "对象类型")
public class ObjectTypeInfo {
    @ApiModelProperty(value = "对象英文类型")
    private String objectType;
    @ApiModelProperty(value = "对象中文名称")
    private String objectName;
    @ApiModelProperty(value = "对象对应的操作类型")
    private List<OperationTypeResp> operations;


    public String getObjectType() {
        return this.objectType;
    }

    public String getObjectName() {
        return this.objectName;
    }

    public List<OperationTypeResp> getOperations() {
        return this.operations;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public void setOperations(List<OperationTypeResp> operations) {
        this.operations = operations;
    }
}
