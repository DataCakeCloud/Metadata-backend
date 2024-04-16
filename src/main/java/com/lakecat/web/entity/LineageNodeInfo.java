package com.lakecat.web.entity;

import io.lakecat.catalog.common.model.LineageInfo;
import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

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
public class LineageNodeInfo  {

    private Integer id;
    private String qn;
    private Integer ot;
    private Integer st;
    private Integer dt;
    public boolean tf;


    private List<LineageInfo.LineageRs> beforeRelations;
    private List<LineageInfo.LineageRs> relations;



}