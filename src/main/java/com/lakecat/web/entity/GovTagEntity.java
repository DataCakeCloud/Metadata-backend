package com.lakecat.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@ApiModel("治理标签数据")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("governance_tags")
public class GovTagEntity {
    @TableId
    @TableField(value = "id")
    @ApiModelProperty(value = "记录自增ID")
    private Long id;
    @ApiModelProperty(value = "标签名称")
    @TableField("tag_name")
    private String tagName;
    @ApiModelProperty(value = "标签值")
    @TableField("tag_value")
    private String tagValue;
    @ApiModelProperty(value = "标签类型")
    @TableField("tag_type")
    private Integer tagType;
    @ApiModelProperty(value = "标签所属对象ID")
    @TableField("object_id")
    private String objectId;
    @ApiModelProperty(value = "标签所属对象名称")
    @TableField("object_name")
    private String objectName;
    @ApiModelProperty(value = "标签状态：0启用， 1废弃")
    @TableField("status")
    private Integer status;
    @ApiModelProperty(value = "标签备注")
    @TableField("tag_comment")
    private String tagComment;
    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private String createTime;
    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    private String updateTime;
}
