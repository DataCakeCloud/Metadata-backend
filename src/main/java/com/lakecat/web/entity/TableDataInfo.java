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
public class TableDataInfo extends BaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;


    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 查询表
     */
    @TableField("table_name")
    private String tableName;


    /**
     * 区域
     */
    @TableField("region")
    private String region;
    /**
     * 执行sql
     */
    @TableField("sql")
    private String sql;
    /**
     * 展示数据
     */
    @TableField("data")
    private String data;

    /**
     * 查询条数
     */
    @TableField("size")
    private Integer size;


}