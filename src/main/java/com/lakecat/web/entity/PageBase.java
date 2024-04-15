package com.lakecat.web.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "分页")
public class PageBase {
    @ApiModelProperty(value = "页数")
    private int pageNum;
    @ApiModelProperty(value = "每页数量")
    private int pageSize;
    @ApiModelProperty(value = "总量")
    private long total;

}
