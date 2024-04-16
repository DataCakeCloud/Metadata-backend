package com.lakecat.web.entity;

import io.lakecat.catalog.common.model.glossary.Category;
import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xuebotao
 * @date 2023-12-07
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("湖仓模型")
public class Model extends BaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer glossaryId;

    private String glossaryName ="MODEL";

    private Integer id;

    private Integer parentId;

    //(类目)/模型名称
    private String name;

    private String description;

    private String createBy;

    private Long createTime;

    private Long updateTime;

    private boolean isEffective = false;

    //层级 目前模型分4层
    private Integer hierarchy;

    private Integer count;

    private String groupName;

    //子模型
    private List<Category> children = new ArrayList<>();

    private Boolean isExist = false;


}