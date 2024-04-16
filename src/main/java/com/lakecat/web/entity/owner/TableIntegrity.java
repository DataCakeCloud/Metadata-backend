package com.lakecat.web.entity.owner;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TableIntegrity {
    @ApiModelProperty("表全限定名称")
    private String tableQualifiedName;
    @ApiModelProperty("完善度")
    private double integrityRatio;

    @ApiModelProperty("拥有者")
    private String owner;

}
