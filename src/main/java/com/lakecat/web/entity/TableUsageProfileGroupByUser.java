package com.lakecat.web.entity;

import java.math.BigInteger;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ApiModel("根据用户分组的表使用统计")
public class TableUsageProfileGroupByUser {

    @ApiModelProperty("用户ID")
    private String userId;
    @ApiModelProperty("最近一次访问时间戳")
    private Long recentlyVisitedTimestamp;
    @ApiModelProperty("访问次数")
    private BigInteger sumCount;
    @ApiModelProperty("平均访问次数")
    private BigInteger avgCount;

}
