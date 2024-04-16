package com.lakecat.web.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author wuyan
 * @date 2022/6/13
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("组类")
public class AccessGroup {
    @NotBlank(message = "名字不能为空")
    @ApiModelProperty(value = "名字")
    private String name;


    private String label;

    private Integer id;

    private String eName;

    private Integer tenantId;

    private Integer parentId;

    private Integer type = 0;

    private Integer userId;

    private Integer hierarchy;

    // 0:是 ，1：不是
    private Integer isLeader;

    private String userName;

    private Integer userNum;

    private List <AccessGroup> children = new ArrayList <>();

    private List <TableForCache> tables;

    private Integer userSize;

    private Boolean hasChildren = true;

    private Integer groupId;

    private String isLeaderFlag;

    private List <Integer> ids;

    private Set <String> userSet;

    private Boolean isHasChildrenDir = false;


}
