package com.lakecat.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author slj
 * @date 2022/4/27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BaseInfo {


    @TableField(exist = false)
    private String userId;

    @TableField(exist = false)
    private String keyWord;

    @TableField(exist = false)
    private int page;

    @TableField(exist = false)
    private int limit;

    @TableField(exist = false)
    private String sort;

}

