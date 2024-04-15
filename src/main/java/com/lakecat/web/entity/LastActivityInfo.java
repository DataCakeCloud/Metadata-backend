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
public class LastActivityInfo extends BaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 字典类型
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 字典类型
     */
    @TableField("db_name")
    private String dbName;


    /**
     * 字典类型
     */
    @TableField("region")
    private String region;


    /**
     * 前端展示值
     */
    @TableField("`user_id`")
    private String userId;
    /**
     * 数据源类型value
     */
    @TableField("`sum_count`")
    private BigInteger sumCount;


    /**
     * 数据源类型value
     */
    @TableField("`avg_count`")
    private BigInteger avgCount;


    /**
     * 数据源类型value
     */
    @TableField("`recently_visited_timestamp`")
    private String recentlyVisitedTimestamp;

    /**
     * 数据源类型value
     */
    @TableField("status")
    private String status;

    @TableField("sole")
    private String sole;

    @TableField(exist = false)
    private String userList;

    @TableField(exist = false)
    private Double count;

}