package com.lakecat.web.vo.blood;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BloodRequest {
    private String region;
    private  String databaseName;
    private String tableName;//表名

    private int upDeep;//向上回溯几层
    private int downDeep;//向下回溯几层

    private String taskName;//任务名称

    private String taskId;

    private String auth;


    public void wrapDefault(){
        upDeep=upDeep==0?5:upDeep;
        downDeep=downDeep==0?5:downDeep;
    }
}
