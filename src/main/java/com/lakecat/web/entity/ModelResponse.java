package com.lakecat.web.entity;

import io.lakecat.catalog.common.model.glossary.Category;
import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.experimental.Accessors;

import javax.naming.Name;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xuebotao
 * @date 2023-12-17
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("表的模型相应")
public class ModelResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer glossaryId;

    //根部的id
    private Integer id;

    //根部的名称
    private String name;

    //带着根部的ids
    public List<Integer> idValues;

    //带着根部的values
    public List<String> nameValues;

    public Long createTime;

    private Boolean isExist = false;

    private Boolean effective = false;
}