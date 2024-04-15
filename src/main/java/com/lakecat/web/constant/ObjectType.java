package com.lakecat.web.constant;

import java.util.HashMap;
import java.util.Map;

import com.lakecat.web.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

public enum ObjectType {

    /**
     * 数据源
     */
    DATASOURCE("(datasource@[\\w]+)", "DATASOURCE", "数据源"),
    REGION("^[a-zA-Z0-9_-]+$", "REGION", "区域"),
    TABLE("([^0-9][\\w]+)\\.([^0-9][\\w]+)\\.([^0-9][\\w]+|\\*)", "TABLE", "数据表"),
    CATALOG("","CATALOG","数据源"),
    DATABASE("([^0-9][\\w]+)\\.([^0-9][\\w]+|\\*)", "DATABASE", "数据库"),
    ;

    public final String regexFormat;
    public final String printName;
    public final String cnName;
    public static final Map<String, ObjectType> ANY_NAME_MAP = new HashMap<>();

    static {
        for (ObjectType e: ObjectType.values()){
            ANY_NAME_MAP.put(e.printName, e);
            ANY_NAME_MAP.put(e.cnName, e);
        }
    }

    ObjectType(String regexFormat, String printName, String cnName) {
        this.regexFormat = regexFormat;
        this.printName = printName;
        this.cnName = cnName;
    }

    /**
     * 根据任何名称获取枚举值
     * @param anyName
     * @return
     */
    public static ObjectType get(String anyName) throws BusinessException {
        if (ANY_NAME_MAP.containsKey(anyName)){
            return ANY_NAME_MAP.get(anyName);
        }
        throw new BusinessException("传入参数有误", -1);
    }

    public static ObjectType getType(String text) {
        if (StringUtils.countMatches(text,".")==1) {
            return DATABASE;
        }
        if (StringUtils.countMatches(text,".")==2) {
            return TABLE;
        }
        if (StringUtils.countMatches(text,".")==0) {
            return REGION;
        }
        if (text.matches(DATASOURCE.regexFormat)) {
            return DATASOURCE;
        }

        throw new IllegalArgumentException();
    }

}
