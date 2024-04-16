package com.lakecat.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigInteger;

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
public class RoleOwnerRelevance extends BaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 字典类型
     */
    @TableField("project_id")
    private String projectId;

    /**
     * 字典类型
     */
    @TableField("role_name")
    private String roleName;


    /**
     * 字典类型
     */
    @TableField("role_id")
    private String roleId;


    /**
     * 前端展示值
     */
    @TableField("`comment`")
    private String comment;
    /**
     * 数据源类型value
     */
    @TableField("`created_time`")
    private String createdTime;


    /**
     * 数据源类型value
     */
    @TableField("`user_name`")
    private String userName;

}