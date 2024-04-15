package com.lakecat.web.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

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
@ApiModel("收集详情")
public class CollectInfo extends BaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 0 模版模式  1 sql模式
     */
    @TableField("table_id")
    private Long tableId;
    /**
     * 创建区域
     */
    @TableField("user_id")
    private String userId;


    @TableField(exist = false)
    private String tableName;


    @TableField(exist = false)
    private Long size;

    /**
     * 创建区域
     */
    @TableField("status")
    private int status;

    /**
     * 创建区域
     */
    @TableField("create_time")
    private String createTime;


    /**
     * 创建区域
     */
    @TableField("update_time")
    private String updateTime;

    @TableField("sole")
    private String sole;

    @TableField(exist = false)
    private Integer collect;

    @TableField(exist = false)
    private String userList;

    @TableField(exist = false)
    private Integer pageNum = -1;

    @TableField(exist = false)
    private Integer pageSize;


}