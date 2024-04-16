package com.lakecat.web.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(description = "字典查询")
public class DictInfoTypeReq {
    @ApiModelProperty(value = "待查询的字典类型", example = "frequency_update", allowableValues = ""
        + "frequency_update: 更新频次, "
        + "department: 部门, "
        + "pu: 业务, "
        + "data_hierarchy: 数据分层, "
        + "subject_domain: 主题域, "
        + "SD_FileFormat: FileFormat 文件保存格式")
    private String dictType;

}
