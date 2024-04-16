package com.lakecat.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lakecat.web.constant.DataGradeTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;


/**
 * @author hongyg
 * Desc: 数据等级表, 合规
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(description = "数据等级")
@TableName("data_grade")
public class DataGrade extends BaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * FN: field name
     */
    public static final String FN_ID = "id";
    public static final String FN_TABLE_ID = "table_id";
    public static final String FN_GRADE_TYPE = "grade_type";
    public static final String FN_GRADE = "grade";
    public static final String FN_NAME = "name";
    public static final String FN_MAINTAINER = "maintainer";
    public static final String FN_STATUS = "status";
    public static final String FN_UPDATE_TIME = "update_time";
    public static final String FN_CREATE_TIME = "create_time";

    @ApiModelProperty(value = "自增ID")
    @TableId(value = FN_ID, type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "关联表table_info id")
    @TableField(value = FN_TABLE_ID)
    private Long tableId;
    /**
     * 数据等级类型，
     */
    @ApiModelProperty(value = "数据等级类型，0：database， 1： table, 2: field")
    @TableField(FN_GRADE_TYPE)
    private Integer gradeType = DataGradeTypeEnum.FIELD.ordinal();

    /**
     * 数据等级
     */
    @ApiModelProperty(value = "数据等级： 1级、2级、3级、4级")
    @TableField(FN_GRADE)
    private String grade;
    /**
     * 当 gradeType 为 TABLE 时无值
     */
    @ApiModelProperty(value = "字段名称")
    @TableField(FN_NAME)
    private String name;

    /**
     * 维护人
     */
    @ApiModelProperty(value = "维护人")
    @TableField(FN_MAINTAINER)
    private String maintainer;

    @ApiModelProperty(value = "状态： 0-有效1-无效")
    @TableField(FN_STATUS)
    private int status = 0;

    @ApiModelProperty(value = "更新时间")
    @TableField(FN_UPDATE_TIME)
    private String updateTime;

    @ApiModelProperty(value = "创建时间")
    @TableField(FN_CREATE_TIME)
    private String createTime;

    @TableField("sole")
    private String sole;
}

