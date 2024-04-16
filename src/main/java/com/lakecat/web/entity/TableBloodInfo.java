package com.lakecat.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableBloodInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String region;
    @TableField("db_name")
    private String dbName;

    private String name;
    @TableField("blood_str")
    private String bloodStr;
    @TableField("create_time")
    private String createTime;
    @TableField("update_time")
    private String updateTime;

    private String owners;//血缘关联的人员

    @TableField("task_id")
    private String taskId;

    @TableField("task_name")
    private String taskName;

    private boolean exception;//查询异常


}
