package com.lakecat.web.entity.owner;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TableUsageProfile {
    /*@ApiModelProperty("owner访问频次")
    private double ownerVisitFrequency;
    @ApiModelProperty("平台访问频次")
    private double platformVisitFrequency;
    @ApiModelProperty("占比")
    private double ownerVisitFrequencyRatio;*/
    @ApiModelProperty("owner访问频次活跃度")
    private double ownerVisitActivity;
    @ApiModelProperty("平台访问频次活跃度")
    private double platformVisitActivity;
}
