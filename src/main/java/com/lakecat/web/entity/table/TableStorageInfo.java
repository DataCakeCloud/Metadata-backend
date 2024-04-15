package com.lakecat.web.entity.table;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@ApiModel("表的存储信息")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableStorageInfo {
//  2022-08-10新增
    @ApiModelProperty("dt")
    private String dt;
    @ApiModelProperty("id")
    private Long id;
    @ApiModelProperty("owner")
    private String owner;
    @ApiModelProperty("表名称")
    private String tableName;
    @ApiModelProperty("库名称")
    private String dbName;
    @ApiModelProperty("表对象个数")
    private Long tableObjectNum;
    @ApiModelProperty("表小文件个数")
    private Long tableSmallObjectNum;
    @ApiModelProperty("表分区个数")
    private int tablePartitionNum;
    @ApiModelProperty("表所在桶")
    private String tableBucketName;
//
    @ApiModelProperty("存储总量")
    private String totalStorage;
    @ApiModelProperty("云商存储类型")
    private String storageType;
    @ApiModelProperty("存储文件格式")
    private String storageFileFormat;
    @ApiModelProperty("存储路径")
    private String location;
    @ApiModelProperty("小文件建议")
    private String smallFileAdvice;
    @ApiModelProperty("小文件合并跳转链接")
    private String smallFileWorkbenchUrl;
    @ApiModelProperty("region")
    private String region;
    @ApiModelProperty("provider")
    private String provider;
    @ApiModelProperty("标准存储量")
    private String tableStandardSize;
    @ApiModelProperty("智能分层存储量")
    private String tableIntelligentSize;
    @ApiModelProperty("冷冻存储量")
    private String tableDeepSize;
    @ApiModelProperty("归档存储量")
    private String tableArchiveSize;
    @TableField(exist = false)
    @ApiModelProperty("存储标签")
    private List<String> tags;
    @ApiModelProperty("小文件总大小")
    private String tableSmallObjectTotalSize;
    @ApiModelProperty("是否已经更新")
    private String isUpdate;
    @ApiModelProperty("分区字段名称")
    private String partitionNames;
    @ApiModelProperty("唯一建")
    private String key;

}
