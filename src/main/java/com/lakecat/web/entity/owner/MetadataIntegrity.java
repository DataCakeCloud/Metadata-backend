package com.lakecat.web.entity.owner;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MetadataIntegrity {
    @ApiModelProperty("平台完善度")
    private double platformIntegrity;
    @ApiModelProperty("用户表完善度")
    private double ownerIntegrity;
    @ApiModelProperty("表完善度明细")
    private List<TableIntegrity> tableDetail;

}
