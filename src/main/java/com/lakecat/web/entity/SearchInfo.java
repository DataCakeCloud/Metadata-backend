package com.lakecat.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SearchInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户Id
     */
    private String userId;

    /**
     * 输入内容
     */
    private String keyWord;
    /**
     * 创建区域
     */
    private String region;

    /**
     * 主题
     */
    private String subject;
    /**
     * 页面
     */
    private Integer pageNum;

    /**
     * 页面大小
     */
    private Integer pageSize;


}
