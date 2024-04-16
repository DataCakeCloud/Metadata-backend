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
public class TableOwnerInfo extends BaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 字典类型
     */
    @TableField("tableNum")
    private String tableNum;
    /**
     * 前端展示值
     */
    @TableField("`storageSize`")
    private String storageSize;
    /**
     * 数据源类型value
     */
    @TableField("`privilegeUser`")
    private String privilegeUser;

    /**
     * 数据源类型value
     */
    @TableField("privilegeNum")
    private String privilegeNum;

}