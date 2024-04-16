package com.lakecat.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

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
public class TableTagInfo extends BaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 字典类型
     */
    @TableField("table_id")
    private Long tableId;
    /**
     * 前端展示值
     */
    @TableField("`key`")
    private String key;
    /**
     * 数据源类型value
     */
    @TableField("`value`")
    private String value;

    /**
     * 数据源类型value
     */
    @TableField("status")
    private String status;

    @TableField("sole")
    private String sole;

    @TableField(exist = false)
    private String tags;

    @TableField(exist = false)
    private String region;

    @TableField(exist = false)
    private String dbName;

    @TableField(exist = false)
    private String tableName;


    public String getSoleKey() {
        StringBuilder stringBuilder = new StringBuilder("");
        if (StringUtils.isNotEmpty(region)) {
            stringBuilder.append(region).append(".");
        }
        if (StringUtils.isNotEmpty(dbName)) {
            stringBuilder.append(dbName).append(".");
        }
        if (StringUtils.isNotEmpty(tableName)) {
            stringBuilder.append(tableName);
        }
        return stringBuilder.toString();
    }


}