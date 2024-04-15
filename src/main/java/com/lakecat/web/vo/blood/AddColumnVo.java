package com.lakecat.web.vo.blood;

import com.lakecat.web.entity.BatchDataGradeReq;
import lombok.Data;

import java.util.List;

@Data
public class AddColumnVo {
    private String catalog;
    private String dbName;
    private String name;
    List<BatchDataGradeReq.ColumnDataGrade> colsGrade;
}
