package com.lakecat.web.entity;

import io.swagger.annotations.ApiModel;
import lombok.*;

import java.io.Serializable;

/**
 * @author xuebotao
 * @since 2023-10-30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("查询lakecat参数")
public class LakeCatParam implements Serializable {

    private static final long serialVersionUID = 1L;

    private String region;

    private String catalogName;

    private String dbName;

    private String tableName;

    private String keywords;

    private Integer size;

    private String pageToken;

    private String nextMarker;

    private String previousMarker;

    private String owner;

    private Integer categoryId;

    private String secondaryKeywords;

    private String objectType;

    private String lineageType;

    private Integer depth = 0;

    private String direction = "BOTH";

    private Integer beforeDepth = 1;

    private Integer afterDepth = 1;

    private String jobFactId;

}