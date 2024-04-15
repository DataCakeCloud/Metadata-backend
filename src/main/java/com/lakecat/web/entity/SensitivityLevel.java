package com.lakecat.web.entity;

import lombok.Data;

/**
 *
 */
@Data
public class SensitivityLevel {
    private String dataLevel;//数据级别
    private String definingElements;//定义要素
    private String commonFields;//常用字段
}
