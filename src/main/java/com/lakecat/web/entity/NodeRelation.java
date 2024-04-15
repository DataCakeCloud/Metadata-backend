package com.lakecat.web.entity;

import io.lakecat.catalog.common.model.glossary.Category;
import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xuebotao
 * @date 2024-01-02
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("node关系")
public class NodeRelation  {


    private Integer from;

    private Integer to;

    private Integer lineageType;

    private Integer jobStatus;

    private String executeUser;

    private String jobFactId;

    private Map<String, Object> params;


}