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
public class JobAlertBloReqForUrl {

    //筛选条件
    /**
     * 评分 10-top10 20-top20 30-top30 如果为0 表示全选
     */

    @ApiModelProperty(value = "需要提醒的owner", required = true, position = 0)
    private String owner;

}
