package com.lakecat.web.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.constant.BaseResponseCodeEnum;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;


@Data
public class BaseResponse<T> {

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


    public BaseResponse() {
    }

    private BaseResponse(T data) {
        this.data = data;
    }

    private BaseResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> BaseResponse <T> success() {
        return new BaseResponse <>();
    }

    public static <T> BaseResponse <T> success(T data) {
        return new BaseResponse <>(data);
    }

    public static <T> BaseResponse <T> success(BaseResponseCodeEnum responseCodeEnum, T data) {
        return getInstance(responseCodeEnum.name(), responseCodeEnum.getMessage(), data);
    }

    public static <T> BaseResponse <T> error(BaseResponseCodeEnum responseCodeEnum) {
        return getInstance(responseCodeEnum.name(), responseCodeEnum.getMessage());
    }

    public static <T> BaseResponse <T> error(BaseResponseCodeEnum responseCodeEnum, T data) {
        return getInstance(responseCodeEnum.name(), responseCodeEnum.getMessage(), data);
    }

    public static <T> BaseResponse <T> error(String code, String message) {
        return getInstance(code, message);
    }

    public static <T> BaseResponse <T> error(String code, String message, T data) {
        return getInstance(code, message, data);
    }

    public static <T> BaseResponse <T> getInstance(String code, String message) {
        return getInstance(code, message, null);
    }

    public static <T> BaseResponse <T> getInstance(String code, String message, T data) {
        return new BaseResponse <>(code, message, data);
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