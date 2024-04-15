package com.lakecat.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-10
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TableInfoUserInput implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String userId;

    private String input;

    private LocalDateTime createTime;


}


