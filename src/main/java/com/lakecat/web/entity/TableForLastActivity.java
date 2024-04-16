package com.lakecat.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * Created by slj on 2022/12/8.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel("表缓存")
public class TableForLastActivity implements Serializable {


    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 0 模版模式  1 sql模式
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 0 模版模式  1 sql模式
     */
    @TableField("db_name")
    private String dbName;

    /**
     * 0 模版模式  1 sql模式
     */
    @TableField("region")
    private String region;


    /**
     * 0 模版模式  1 sql模式
     */
    @TableField("user_ids")
    private String userIds;


}
