package com.lakecat.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
public class GovJobInfo extends BaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 用户
     */
    @TableField("id")
    private String id;
    /**
     * cluster
     */
    @TableField("job_name")
    private String jobName;

}