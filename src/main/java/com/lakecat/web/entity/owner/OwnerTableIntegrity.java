package com.lakecat.web.entity.owner;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OwnerTableIntegrity {
    @ApiModelProperty("表数")
    private double count;
    @ApiModelProperty("汇总完善度")
    private double sumIntegrity;
    @ApiModelProperty("完善度")
    private double integrityRatio;

    @ApiModelProperty("拥有者")
    private String owner;

}
