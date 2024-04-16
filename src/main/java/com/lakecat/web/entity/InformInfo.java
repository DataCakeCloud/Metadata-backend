package com.lakecat.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class InformInfo extends BaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 字典类型
     */
    @TableField("table_name")
    private String tableName;
    /**
     * 前端展示值
     */
    @TableField("`db_name`")
    private String dbName;
    /**
     * 数据源类型value
     */
    @TableField("`region`")
    private String region;

    /**
     * 数据源类型value
     */
    @TableField("operation")
    private String operation;

    @TableField("created_time")
    private String createdTime;


    @TableField("operation_user")
    private String operationUser;


    @TableField("inform_list")
    private String informList;

    @TableField("message")
    private String message;

    @TableField("args")
    private String args;


}