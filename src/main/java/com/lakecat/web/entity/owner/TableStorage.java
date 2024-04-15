package com.lakecat.web.entity.owner;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TableStorage {
    @ApiModelProperty("僵尸数据量")
    private double ownerZombieDataSize;
    @ApiModelProperty("总存储量")
    private double tableStorageSize;
    @ApiModelProperty("占比")
    private double ownerZombieDataSizeRatio;
}
