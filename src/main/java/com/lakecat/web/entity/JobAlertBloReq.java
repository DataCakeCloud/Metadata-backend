package com.lakecat.web.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author slj
 * @date 2022/5/17
 */
@ApiModel("创建提醒请求参数")
@Data
public class JobAlertBloReq {

    @ApiModelProperty(value = "需要提醒的ids数组", required = true, position = 0)
    private List <JobAlertBloReqForUrl> ids;
    @ApiModelProperty(value = "表名", required = true, position = 1)
    String tableName;

    @ApiModelProperty(value = "变更", required = true, position = 2)
    String message;

    @ApiModelProperty(value = "任务名", required = true, position = 3)
    String taskName;

    @ApiModelProperty(value = "任务链接", required = true, position = 3)
    String taskUrl;

    @ApiModelProperty(value = "表详情链接", required = true, position = 3)
    String tableUrl;


}
