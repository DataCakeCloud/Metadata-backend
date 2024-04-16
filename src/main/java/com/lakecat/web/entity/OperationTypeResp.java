package com.lakecat.web.entity;

import com.lakecat.web.constant.OperationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "操作类型")
public class OperationTypeResp {
    @ApiModelProperty(value = "操作类型")
    private String operationType;
    @ApiModelProperty(value = "操作类型中文名称")
    private String operationName;

    public OperationTypeResp(OperationType operationType) {
        this.operationType = operationType.typeValue;
        this.operationName = operationType.cnName;
    }

}
