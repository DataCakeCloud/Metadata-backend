package com.lakecat.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("oa_auth")
public class OaAuth {

    @TableId(value = "id")
    private String id;

    @TableField("user")
    private String user;//申请人
    @TableField("user_group")
    private String userGroup;//申请人组名称；
    @TableField("uuid")
    private String uuid;
    @TableField("oa_request_json")
    private String oaRequestJson;
    @TableField("input_json")
    private String inputJson;
    @TableField("create_time")
    private String createTime;
    @TableField("oa_id")
    private String oaId;//返回id 有表示成功
    @TableField("status")
    private int status;//0 发送 1 审批成功 2失败
    @TableField("update_time")
    private String updateTime;
}
