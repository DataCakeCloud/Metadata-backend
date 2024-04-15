package com.lakecat.web.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author slj
 * @date 2022/5/17
 */
@ApiModel("创建提醒请求参数")
@Data
public class JobAlertReq {

    @ApiModelProperty(value = "已经赋权的用户", required = true, position = 0)
    private String userName;
    @ApiModelProperty(value = "表owner", required = true, position = 1)
    private String owner;
    @ApiModelProperty(value = "表名", required = true, position = 2)
    String tableName;

    @ApiModelProperty(value = "权限", required = true, position = 3)
    String permission;

    @ApiModelProperty(value = "url", required = true, position = 4)
    String url;

}
