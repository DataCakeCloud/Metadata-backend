package com.lakecat.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.List;


@JsonIgnoreProperties
public class TreeData implements Serializable {


    /**
     * 属性名
     */
    private String parameterName;
    /**
     * 值
     */
    private Object parameterValue;;

    /**
     * 类型
     */
    private String parameterType;

    /**
     * 子节点
     */
    private List<TreeData> children;


    public Object getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(Object parameterValue) {
        this.parameterValue = parameterValue;
    }


    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public List<TreeData> getChildren() {
        return children;
    }

    public void setChildren(List<TreeData> children) {
        this.children = children;
    }

}
