package com.lakecat.web.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.constant.BaseResponseCodeEnum;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;


@Data
public class BaseOAResponse<T> {

    /**
     * 错误码
     */
    private String code;
    /**
     * 消息
     */
    private String message;
    /**
     * 响应内容
     */
    private T data;


    public BaseOAResponse() {
    }

    private BaseOAResponse(T data) {
        this.data = data;
    }

    private BaseOAResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> BaseOAResponse <T> success() {
        return new BaseOAResponse <>();
    }

    public static <T> BaseOAResponse <T> success(T data) {
        return new BaseOAResponse <>(data);
    }

    public static <T> BaseOAResponse <T> success(BaseResponseCodeEnum responseCodeEnum, T data) {
        return getInstance(responseCodeEnum.name(), responseCodeEnum.getMessage(), data);
    }

    public static <T> BaseOAResponse <T> error(BaseResponseCodeEnum responseCodeEnum) {
        return getInstance(responseCodeEnum.name(), responseCodeEnum.getMessage());
    }

    public static <T> BaseOAResponse <T> error(BaseResponseCodeEnum responseCodeEnum, T data) {
        return getInstance(responseCodeEnum.name(), responseCodeEnum.getMessage(), data);
    }

    public static <T> BaseOAResponse <T> error(String code, String message) {
        return getInstance(code, message);
    }

    public static <T> BaseOAResponse <T> error(String code, String message, T data) {
        return getInstance(code, message, data);
    }

    public static <T> BaseOAResponse <T> getInstance(String code, String message) {
        return getInstance(code, message, null);
    }

    public static <T> BaseOAResponse <T> getInstance(String code, String message, T data) {
        return new BaseOAResponse <>(code, message, data);
    }


    /**
     * 解决cannot evaluate BaseResponse.toString()的exception
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);

    }

    public JSONObject get() {
        return JSON.parseObject(data.toString());
    }
}