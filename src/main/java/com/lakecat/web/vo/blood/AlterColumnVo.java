package com.lakecat.web.vo.blood;

import lombok.Data;

import java.util.List;

@Data
public class AlterColumnVo {
    private List<ColumnVo> renameList;

    private List<ColumnVo> alterList;

    private Long id;

    private String region;

    private String dbName;

    private String name;

    private String columns;;

}
