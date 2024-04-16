package com.lakecat.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
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
@Builder
public class AuthorityGovInfo extends BaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 字典类型
     */
    @TableField("table_name")
    private String tableName;
    /**
     * 操作发起用户
     */
    @TableField("operator")
    private String operator;


    /**
     * 权限操作
     */
    @TableField("operate")
    private String operate;


    /**
     * 被操作对象
     */
    @TableField("permission")
    private String permission;


    /**
     * 被操作角色
     */
    @TableField("operated_user")
    private String operatedUser;


    /**
     * 原因
     */
    @TableField("reason")
    private String reason;



    /**
     * 用户们
     */
    @TableField("user_name")
    private String userName;


    /**
     * 权限操作
     */
    @TableField("execute_status")
    private Integer executeStatus;

    /**
     * 权限操作
     */
    @TableField("cycle")
    private Integer cycle;



    /**
     * 原因
     */
    @TableField("create_time")
    private String createTime;


    /**
     * 原因
     */
    @TableField("update_time")
    private String updateTime;


}