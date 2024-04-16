package com.lakecat.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author slj
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(description = "操作日志表")
@TableName("operate_log")
public class OperateLogEntity extends BaseInfo implements Serializable {

    public static final long serialVersionUID = 1L;

    /**
     * 操作日志id
     */
    @ApiModelProperty(value = "操作日志id")
    @TableId(value = "id", type = IdType.NONE)
    private String id;

    /**
     * 用户名称
     */
    @ApiModelProperty(value = "用户名称")
    @TableField("`user_name`")
    private String userName;


    /**
     * 来源
     */
    @ApiModelProperty(value = "来源")
    @TableField("source")
    private String source;
    /**
     * 日志id
     */
    @ApiModelProperty(value = "日志id")
    @TableField("trace_id")
    private String traceId;

    /**
     * 操作类型
     */
    @ApiModelProperty(value = "操作类型")
    @TableField("type")
    private String type;



    /**
     * 请求路径
     */
    @ApiModelProperty(value = "请求路径")
    @TableField("uri")
    private String uri;


    /**
     * 请求参数
     */
    @ApiModelProperty(value = "请求参数")
    @TableField("params")
    private String params;


    /**
     * result_code 响应状态码
     */
    @ApiModelProperty(value = "响应状态码")
    @TableField("result_code")
    private String resultCode;


    /**
     *响应码不为0时的错误信息
     */
    @ApiModelProperty(value = "响应码不为0时的错误信息")
    @TableField("result_message")
    private String resultMessage;



    /**
     * result_data 响应码为0时，返回数据
     */
    @ApiModelProperty(value = "响应码为0时，返回数据")
    @TableField("result_data")
    private String resultData;

    /**
     * 耗时，单位毫秒
     */
    @ApiModelProperty(value = "耗时，单位毫秒")
    @TableField("cost_time")
    private Integer costTime;


    /**
     * request_time
     */
    @ApiModelProperty(value = "请求时间")
    @TableField("request_time")
    private String requestTime;


    /**
     * 返回日期
     */
    @ApiModelProperty(value = "返回日期")
    @TableField("response_time")
    private String responseTime;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

}
