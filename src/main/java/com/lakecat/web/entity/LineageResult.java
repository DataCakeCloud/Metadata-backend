package com.lakecat.web.entity;

import io.lakecat.catalog.common.model.LineageInfo;
import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.experimental.Accessors;

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
public class LineageResult {


    //根部rootid
    private Integer rootId;

    //所有的node
    private List<LineageNodeInfo> nodes;

    //所有的关系
    private List<NodeRelation> links;


}