package com.lakecat.web.entity.table;

import java.math.BigInteger;

import com.lakecat.web.entity.TableInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("表信息汇总描述")
public class TableSummaryDescInfo extends TableInfo {
    @ApiModelProperty("表存在时间，单位天")
    private Integer tableAge;
    @ApiModelProperty("最近被访问次数(默认近一个月)")
    private BigInteger recentlyVisitedTimes;

}
