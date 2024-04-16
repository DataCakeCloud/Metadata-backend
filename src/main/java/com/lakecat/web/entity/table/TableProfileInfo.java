package com.lakecat.web.entity.table;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("表的访问信息")
public class TableProfileInfo {
    @ApiModelProperty("表存在时间，单位天")
    private Integer tableAge;
    @ApiModelProperty("最近被访问次数(默认近一个月)")
    private Integer recentlyVisitedTimes;
    @ApiModelProperty("最近访问用户")
    private List<String> recentlyVisitedUsers;

}
