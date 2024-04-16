package com.lakecat.web.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author slj
 */
@Data
public class Response<T> implements Serializable {

    /**
     * 结果状态码, 0-success, 1-error
     */
    private Integer code = 20000;

    private String message;

    private T data;

    private long total;

    @ApiModelProperty(value = "page", required = true, position = 4)
    private long page;
    @ApiModelProperty(value = "limit", required = true, position = 5)
    private long limit;

    public static <T> Response success() {
        Response webResult = new Response();
        webResult.setCode(0);
        webResult.setMessage("success");
        return webResult;
    }

    public static <T> Response <T> success(T data, long total, long page, long limit) {
        Response <T> webResult = new Response <T>();
        webResult.setCode(0);
        webResult.setMessage("success");
        webResult.setData(data);
        webResult.setTotal(total);
        webResult.setPage(page);
        webResult.setLimit(limit);
        return webResult;
    }

    public static <T> Response success(T data) {
        if (data instanceof Collection) {
            Collection collection = (Collection) data;
            return success(collection, collection.size());
        }
        return success(data, 0);
    }

    public static <T> Response fail(T data, String message, int code) {
        Response webResult = new Response();
        webResult.setCode(code);
        webResult.setMessage(message);
        webResult.setData(data);
        if (data instanceof Collection) {
            Collection collection = (Collection) data;
            webResult.setTotal(collection.size());
        }
        return webResult;
    }

    public static <T> Response success(T data, long total) {
        Response webResult = new Response();
        webResult.setCode(0);
        webResult.setMessage("success");
        webResult.setData(data);
        webResult.setTotal(total);
        return webResult;
    }


    public static Response fail(String msg) {
        return fail(500, msg);
    }

    public static Response fail(int code, String msg) {
        Response webResult = new Response();
        webResult.setCode(code);
        webResult.setMessage(msg);
        return webResult;
    }
}
