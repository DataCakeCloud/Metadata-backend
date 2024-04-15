package com.lakecat.web.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(description = "批量更新数据等级对象")
public class BatchDataGradeReq {

    @ApiModelProperty(value = "表ID")
    private Long tableId;
    @ApiModelProperty(value = "列数据等级信息")
    private List<ColumnDataGrade> colsGrade;

    private String sole;

    private String region;

    private String dbName;

    private String tableName;

    public String getSole() {
        if (null != sole) {
            return sole;
        }
        StringBuilder stringBuilder = new StringBuilder("");
        if (StringUtils.isNotEmpty(region)) {
            stringBuilder.append(region).append(".");
        }
        if (StringUtils.isNotEmpty(dbName)) {
            stringBuilder.append(dbName).append(".");
        }
        if (StringUtils.isNotEmpty(tableName)) {
            stringBuilder.append(tableName);
        }
        return stringBuilder.toString();
    }


    @Data
    @Builder
    @Accessors(chain = true)
    @ApiModel(description = "列数据等级对象")
    public static class ColumnDataGrade {
        @ApiModelProperty(value = "列名")
        private String name;
        @ApiModelProperty(value = "维护人")
        private String maintainer;
        @ApiModelProperty(value = "列数据等级")
        private String dataGrade;

    }

    public BatchDataGradeReq() {
    }

    public BatchDataGradeReq(Long tableId, String colName, String dataGrade) {
        new BatchDataGradeReq(tableId, colName, dataGrade, null);
    }

    public BatchDataGradeReq(Long tableId, String colName, String dataGrade, String maintainer) {
        this.tableId = tableId;
        List<ColumnDataGrade> list = new ArrayList<>();
        list.add(ColumnDataGrade.builder().name(colName).dataGrade(dataGrade).maintainer(maintainer).build());
        this.colsGrade = list;
    }

    public BatchDataGradeReq add(BatchDataGradeReq batchDataGradeReq) {
        if (Objects.equals(this.tableId, batchDataGradeReq.getTableId())) {
            this.colsGrade.addAll(batchDataGradeReq.colsGrade);
        }
        return this;
    }

    public void addColumn(ColumnDataGrade columnDataGrade) {
        if (this.colsGrade == null) {
            this.colsGrade = new ArrayList<>();
        }
        this.colsGrade.add(columnDataGrade);
    }

}
