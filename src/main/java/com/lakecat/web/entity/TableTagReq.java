package com.lakecat.web.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

@Data
@Accessors(chain = true)
@ApiModel(description = "字典查询")
public class TableTagReq {

    private String region;

    private String dbName;

    private String tableNmae;

    private Long tableId;

    public String getSole() {
        StringBuilder stringBuilder = new StringBuilder("");
        if (StringUtils.isNotEmpty(region)) {
            stringBuilder.append(region).append(".");
        }
        if (StringUtils.isNotEmpty(dbName)) {
            stringBuilder.append(dbName).append(".");
        }
        if (StringUtils.isNotEmpty(tableNmae)) {
            stringBuilder.append(tableNmae);
        }
        return stringBuilder.toString();
    }


}
