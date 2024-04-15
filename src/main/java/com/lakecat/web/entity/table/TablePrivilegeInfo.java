package com.lakecat.web.entity.table;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("表的权限信息")
public class TablePrivilegeInfo {

    @ApiModelProperty("用户ID")
    private String userId;
    @ApiModelProperty("申请时间")
    private String requestTime;
    @ApiModelProperty("原因")
    private String reason;
    @ApiModelProperty("保留周期")
    private String cycle;
    @ApiModelProperty("申请权限列表")
    private List<String> privilegeList;

    @ApiModelProperty("保留周期")
    private String userGroup;

    @ApiModelProperty("回收状态")
    private Integer recoveryState = 0;

    private Long permissionTableId;

    /**
     * 申请人
     */
    private String proposer;

    /**
     * 授权人
     */
    private String certigier;



}
