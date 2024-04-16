package com.lakecat.web.entity.owner;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DsOwnerStatVo {
    @ApiModelProperty("活跃表")
    private ActiveTable activeTable;
    @ApiModelProperty("存储数据")
    private TableStorage tableStorage;
    @ApiModelProperty("资源使用量")
    private ResourceUsage resourceUsage;
    @ApiModelProperty("访问热度")
    private TableUsageProfile tableUsageProfile;
    @ApiModelProperty("数据完善度")
    private MetadataIntegrity metadataIntegrity;
}
