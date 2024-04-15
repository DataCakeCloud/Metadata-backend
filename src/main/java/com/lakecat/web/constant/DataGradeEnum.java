package com.lakecat.web.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum DataGradeEnum {

    /**
     * 1级、2级、3级、4级  ===>> 机密、秘密、内部、公开
     */
    L1("1级", "机密"),
    L2("2级", "秘密"),
    L3("3级", "内部"),
    L4("4级", "公开");

    private String name;

    private String desc;

    public static Set<String> nameSet = Arrays.asList(DataGradeEnum.values()).stream().map(x -> x.name).collect(Collectors.toSet());

    DataGradeEnum(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public static String checkName(String name) {
        if (nameSet.contains(name)) {
            return name;
        }
        throw new IllegalArgumentException("数据等级:" + name + ", 输入不正确， 应该为: " + nameSet);
    }

}
