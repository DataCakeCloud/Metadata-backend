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
public class OwnerNotDefaultBucketRecord extends BaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;


    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 部门
     */
    @TableField("department")
    private String department;


    /**
     * 区域
     */
    @TableField("owner")
    private String owner;
    /**
     * 区域
     */
    @TableField("region")
    private String region;
    /**
     * 桶
     */
    @TableField("bucket_name")
    private String bucketName;


    /**
     * 状态
     */
    @TableField("status")
    private String status;

}