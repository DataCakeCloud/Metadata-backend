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
@ApiModel("表详情")
public class TableInfoForOwner extends BaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 0 模版模式  1 sql模式
     */
    private int createType;
    /**
     * 创建区域
     */
    private String region;
    /**
     * 数据源类型
     */
    @TableField("`type`")
    private String type;
    /**
     * 库
     */
    private String dbName;
    /**
     * 主题
     */
    @TableField("`subject`")
    private String subject;
    /**
     * 表类型
     */
    private String updateType;
    /**
     * 更新频次
     */
    @TableField("`interval`")
    private String interval;
    /**
     * 中文名
     */
    private String cnName;
    /**
     * 描述
     */
    private String description;
    /**
     * 1 分区表 0 非分区表
     */
    private int partitionType;
    /**
     * 生命周期
     */
    private int lifecycle;
    /**
     * 表名
     */
    @TableField("`name`")
    private String name;



    @TableField(exist = false)
    private String keyWordForSearch;


    @TableField(exist = false)
    private Object partitionKeysObj;

    private String partitionKeys;

    @TableField(exist = false)
    private String title;


    @TableField(exist = false)
    private String userList;


    @TableField(exist = false)
    private Integer collect;

    @TableField(exist = false)
    private Double count;

    @TableField(exist = false)
    private Integer num;


    @TableField(exist = false)
    private Integer flag;


    @TableField(exist = false)
    private String userName;
    /**
     * 分层字段
     */
    private String hierarchical;

    private String createTime;

    private String updateTime;
    /**
     * 保存路径
     */
    private String location;
    /**
     * 英文名
     */
    private String application;

    /**
     * 文件存储类型
     */
    @TableField("sd_file_format")
    private String sdFileFormat;
    /**
     * 数据文件分隔符
     */
    @TableField(exist = false)
    private String fileDelimiter;

    @TableField(exist = false)
    private Long size;


    /**
     * 行数
     */
    private Integer numRows;
    /**
     * 存储
     */
    private Integer byteSize;
    ///**
    // * 表存储格式
    // */
    //@TableField(exist = false)
    //private String fileFormat;
    /**
     * 0-有效1-无效
     */
    @TableField("`status`")
    private int status;

    @TableField(exist = false)
    private String sql;

    private String owner;


    public String getPartitionKeys() {
        if (null != partitionKeysObj) {
            return JSON.toJSONString(partitionKeysObj);
        }
        return partitionKeys;
    }

    public String title() {
        if (StringUtils.isBlank(cnName)) {
            return dbName + "." + name;
        } else {
            return cnName + "(" + dbName + "." + name + ")";
        }
    }
}