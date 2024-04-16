package com.lakecat.web.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.lakecat.web.utils.LakecatTableUtils;
import io.lakecat.catalog.common.model.Column;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.lakecat.web.entity.table.TableProfileInfo;
import com.lakecat.web.entity.table.TableOutputInfo;
import com.lakecat.web.entity.table.TablePrivilegeInfo;
import com.lakecat.web.entity.table.TableStorageInfo;
import io.lakecat.catalog.common.model.glossary.Category;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

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
public class TableInfo extends BaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 0 模版模式  1 sql模式
     */
    @TableField("create_type")
    private int createType;
    /**
     * 创建区域
     */
    @TableField("region")
    private String region;
    /**
     * 数据源类型
     */
    @TableField("`type`")
    private String type;
    /**
     * 库
     */
    @TableField("db_name")
    private String dbName;
    /**
     * 主题
     */
    @TableField("`subject`")
    private String subject;
    /**
     * 表类型
     */
    @TableField("`update_type`")
    private String updateType;
    /**
     * 更新频次
     */
    @TableField("`interval`")
    private String interval;
    /**
     * 中文名
     */
    @TableField("`cn_name`")
    private String cnName;
    /**
     * 描述
     */
    @TableField("`description`")
    private String description;
    /**
     * 1 分区表 0 非分区表
     */
    @TableField("`partition_type`")
    private int partitionType = 1;
    /**
     * 生命周期
     */
    @TableField("`lifecycle`")
    private int lifecycle;
    /**
     * 表名
     */
    @TableField("`name`")
    private String name;


    /**
     * 表名
     */
    @TableField("`last_activity_count`")
    private Long lastActivityCount;

    @TableField(exist = false)
    private Object columnsObj;

    @TableField("`columns`")
    private String columns;


    @TableField(exist = false)
    private String keyWordForSearch;


    @TableField(exist = false)
    private List<TableTagInfo> tagList;

    @TableField(exist = false)
    private String tags;

    @TableField(exist = false)
    private JSONArray columnsList;

    @TableField(exist = false)
    private Object partitionKeysObj;

    private String partitionKeys;

    @TableField(exist = false)
    private List<ColumnResponse> partitionKeyList;

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
    private Integer flag = 0;


    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String tenantName;


    @TableField(exist = false)
    private String catalogName;


    @TableField(exist = false)
    private String tableName;

    /**
     * 分层字段
     */
    @TableField("`hierarchical`")
    private String hierarchical;

    @TableField("`create_time`")
    private String createTime;
    @TableField("`update_time`")
    private String updateTime;
    /**
     * 保存路径
     */
    @TableField("`location`")
    private String location;
    /**
     * 英文名
     */
    @TableField("`application`")
    private String application;

    /**
     * 文件存储类型
     */
    @TableField("sd_file_format")
    private String sdFileFormat;


    /**
     * 表变更时间
     */
    @TableField("transient_lastDdlTime")
    private String transientLastDdlTime;
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
    @TableField("num_rows")
    private Integer numRows;
    /**
     * 存储
     */
    @TableField("byte_size")
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

    @TableField("`owner`")
    private String owner;

    @TableField(exist = false)
    private Integer dbCount;

    @TableField(exist = false)
    private String pageToken;

    @TableField(exist = false)
    private String nextMarker;

    @TableField(exist = false)
    private String previousMarker;

    @TableField(exist = false)
    //catalog.dbname.tablename
    private String key;

    @TableField(exist = false)
    private Double score;

    @TableField(exist = false)
    private Long recentVisitCount;

    @TableField(exist = false)
    private List<Column> columnList;

    @TableField(exist = false)
    private String lastAccessTime;

    @TableField(exist = false)
    private String tableType = "外部表";


    @TableField(exist = false)
    private String provider = "Hive";

    @TableField(exist = false)
    private Integer categoryId;

    @TableField(exist = false)
    private String secondaryKeywords;

    @TableField(exist = false)
    private String collectTime;

    @TableField(exist = false)
    private String outputFormat;

    @TableField(exist = false)
    private String inputFormat;

    @TableField(exist = false)
    private String fileFormat;

    @TableField(exist = false)
    private String securityLevel;

    @TableField(exist = false)
    private String useDirection;

    @TableField(exist = false)
    private List<ModelResponse> listModel;

    @TableField(exist = false)
    private String latestPartitionName;

    @TableField(exist = false)
    private Integer partitionCount;

    @TableField(exist = false)
    private Integer targetTagCategoryId;

    @TableField(exist = false)
    private String targetTagName;

    @TableField(exist = false)
    private Integer sourceTagCategoryId;

    @TableField(exist = false)
    private Integer targetModelCategoryId;

    @TableField(exist = false)
    private Integer sourceModelCategoryId;

    @TableField(exist = false)
    private List<Category> listTag;

    @TableField(exist = false)
    private List<Category> listCategory;

    @TableField(exist = false)
    private String storageType;

    @TableField(exist = false)
    private String createBy;

    @TableField(exist = false)
    private String userGroupUUid;

    @TableField(exist = false)
    private String dbTable;

    @TableField(exist = false)
    private Boolean clearCache;

    public String getColumns() {
        if (null != columnsObj) {
            return JSON.toJSONString(columnsObj);
        }

        if (StringUtils.isNotEmpty(columns)) {
            return columns;
        }

        if (this.columnList == null || this.columnList.isEmpty()) {
            return null;
        }
        try {
            List<JSONObject> Column = new ArrayList<>();
            for (Column output : this.columnList) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", output.getColumnName());
                jsonObject.put("type", LakecatTableUtils.lmsDataTypeTohmsDataType(output.getColType()));
                jsonObject.put("comment", output.getComment());
                Column.add(jsonObject);
            }
            return Column.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public String getTableName() {
        if (StringUtils.isNotEmpty(name)) {
            return name;
        }
        return tableName;
    }

    public String getPartitionKeys() {
        if (null != partitionKeysObj) {
            return JSON.toJSONString(partitionKeysObj);
        }
        if (partitionKeyList != null && !partitionKeyList.isEmpty()) {
            return JSON.toJSONString(partitionKeyList);
        }
        return partitionKeys;
    }

    public String getSole() {
        if (null != key) {
            return key;
        }
        StringBuilder stringBuilder = new StringBuilder("");
        if (StringUtils.isNotEmpty(region)) {
            stringBuilder.append(region).append(".");
        }
        if (StringUtils.isNotEmpty(dbName)) {
            stringBuilder.append(dbName).append(".");
        }
        if (StringUtils.isNotEmpty(name)) {
            stringBuilder.append(name);
        }
        return stringBuilder.toString();
    }

    public String title() {
        if (StringUtils.isBlank(cnName)) {
            return dbName + "." + name;
        } else {
            return cnName + "(" + dbName + "." + name + ")";
        }
    }
}