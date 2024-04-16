package com.lakecat.web.vo.blood;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * aws_us-east-1.default.datacake_school:linyang:id
 */
@Data
public class TableVo {

    public TableVo(String objectName){
        String[] a=objectName.split(":");
        this.owner=a[1];
        String[] b=a[0].split("\\.");
        this.region=b[0];
        this.dbName=b[1];
        this.tableName=b[2];
//        this.id=Long.valueOf(a[2]);
    }

    private String region;
    private String dbName;
    private String tableName;
    private String owner;
    private String sole;
//    private Long id;


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

}
