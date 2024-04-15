package com.lakecat.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("权限记录表")
public class PermissionRecordInfo extends BaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


    /**
     * 工单类型
     */
    @TableField("order_id")
    private String orderId;


    /**
     * 工单类型
     */
    @TableField("type")
    private String type;
    /**
     * 申请列表
     */
    @TableField("table_list")
    private String tableList;

    /**
     * 权限类型
     */
    @TableField("permission")
    private String permission;

    /**
     * 申请原因
     */
    @TableField("reason")
    private String reason;


    /**
     * 申请用户 用户/用户组
     */
    @TableField("apply_user")
    private String applyUser;

    /**
     * 申请人
     */
    @TableField("proposer")
    private String proposer;


    /**
     * 授权到谁  用户/用户组
     */
    @TableField("grant_user")
    private String grantUser;

    /**
     * 授权人
     */
    @TableField("certigier")
    private String certigier;


    /**
     * 是否钉钉通知
     */
    @TableField("flag")
    private String flag;


    /**
     * 授权类型 个人 或者 角色
     */
    @TableField("grant_type")
    private Integer grantType;

    /**
     * 保留周期
     */
    @TableField("cycle")
    private String cycle;


    /**
     * 授权类型 个人 或者 角色
     */
    @TableField("status")
    private int status;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;

    //0-未回收1-已经回收  table1:1,table2:0
    @TableField("table_recovery_state")
    private String tableRecoveryState;


    @TableField(exist = false)
    private String objectName;


    @TableField(exist = false)
    private String recoveryState;


}