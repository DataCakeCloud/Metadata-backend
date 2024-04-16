package com.lakecat.web.entity.table;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("表的产出信息")
public class TableOutputInfo {

    @ApiModelProperty("任务名称")
    private String taskName;
    @ApiModelProperty("任务ID, DS任务ID")
    private String taskId;
    @ApiModelProperty("任务治理ID")
    private String govJobId;
    @ApiModelProperty("是否展示跳转链接")
    private boolean flag;
    @ApiModelProperty("计算治理标签")
    private List <String> computingGovTags;

}
