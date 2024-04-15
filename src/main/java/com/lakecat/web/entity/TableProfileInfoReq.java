package com.lakecat.web.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("表使用画像请求参数")
public class TableProfileInfoReq extends TableInfoReq {

    @ApiModelProperty(value = "用户ID", required = false)
    private String userId;
    @ApiModelProperty(value = "查询范围开始时间戳，到毫秒", required = false)
    private Long startTimestamp;
    @ApiModelProperty(value = "查询范围结束时间戳，到毫秒", required = false)
    private Long endTimestamp;

}
