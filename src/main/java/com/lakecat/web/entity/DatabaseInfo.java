package com.lakecat.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.lakecat.web.constant.FormatEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.Map;


/**
 * @author slj
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DatabaseInfo implements Serializable {


    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String region;

    private String catalogName;

    private String databaseId;

    private String databaseName;

    private String description;

    private String locationUri;

    private Map <String, String> parameters;

    private String projectId;

    private String userId;

    private String uuid;
    private String userGroupName;

    private Integer pageNum;

    private Integer pageSize;

    private String owner;

    private Long startTime;

    private Long endTime;
}
