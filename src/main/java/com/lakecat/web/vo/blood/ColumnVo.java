package com.lakecat.web.vo.blood;

import lombok.Data;

@Data
public class ColumnVo {
    private String beforeColumnName;//之前的名字

    private String columnName;

    private String colType;

    private String comment;

}
