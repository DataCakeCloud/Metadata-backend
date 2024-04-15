package com.lakecat.web.constant;

import java.text.MessageFormat;

/**
 * 公共域 服务响应码
 */
public enum BaseResponseCodeEnum {
    /**
     * 系统类响应
     */
    SYS_ERR("系统错误"),
    SYS_UNA("服务不可用"),
    SUCCESS(200,"成功"),
    SQLINJECTION("非法的SQL注入"),
    REQUEST_ILLEGAL("非法请求"),
    HTTP_ERR("http请求异常"),
    HTTP_CLOSE_ERR("http关闭异常"),
    CLI_PARAM_ILLEGAL("参数非法"),
    LINEAGE_GRAPH_GET_FAIL("血缘信息获取失败"),
    TABLE_AUTH_EXIST(30010001, "表已存在权限，不用重复申请"),
    EXTERNAL_OA_INVOKE_FAIL(30010002, "OA审批调用失败");


    private Integer code;
    private String message;
    private String suggest;
    private Boolean msgIgnore = false;

    BaseResponseCodeEnum(String message) {
        this.code = 500;
        this.message = message;
    }

    BaseResponseCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
        this.suggest = "";
        this.msgIgnore = true;
    }

    BaseResponseCodeEnum(Integer code, String message, String suggest) {
        this.code = code;
        this.message = message;
        this.suggest = suggest;
        this.msgIgnore = true;
    }

    BaseResponseCodeEnum(Integer code, String message, String suggest, Boolean msgIgnore) {
        this.code = code;
        this.message = message;
        this.suggest = suggest;
        this.msgIgnore = msgIgnore;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getSuggest() {
        return suggest;
    }

    public Boolean getMsgIgnore() {
        return msgIgnore;
    }

    @Override
    public String toString() {
        return MessageFormat.format("ResponseCode:{0},{1}.", this.name(), this.message);
    }

}
