package com.lakecat.web.filter;

/**
 * @author fengxiao
 * @date 2020/6/2
 */
public class RequestConstant {

    /**
     * 忽略拦截或过滤的请求path
     */
    public static final String[] IGNORE_INTERCEPT_PATHS = {"/", "/static/**", "/error/**", "/logout", "/code", "/login",
            "/inf-druid/**", "/druid/**", "/index", "/index.html", "/version", "/favicon.ico","/metadata/auth/doAuthByGroup",
            "/swagger-resources/**", "/swagger-ui.html", "/doc.html/**","/metadata/table/hookTrigger","/metadata/role/grantPrivilegeToUserCallback","/webjars/bycdao-ui/ace/ace.min.js",
            "/swagger-ui.html","/v2/api-docs","/swagger-resources/**","/swagger/**","/**/v2/api-docs","/**/*.js"
    ,"/**/*.css","/**/*.png","/**/*.ico","/webjars/springfox-swagger-ui/**","/actuator/**","/druid/**","/webjars/**","/metadata/health","/metadata/auth/excel","/metadata/role/oaCallback"};

    /**
     * 忽略的CONTENT_TYPE
     * modelstatistics/report
     * modelerror/report
     **/
    public static final String[] IGNORE_CONTENT = {"multipart/form-data"};

    /**
     * 忽略拦截或过滤的请求方法
     **/
    public static final String[] IGNORE_METHOD = {"HEAD", "OPTIONS", "TRACE", "CONNECT"};

    public static final String CURRENT_LOGIN_USER = "current_login_user";

    public static final String AUTHENTICATION_HEADER = "Authentication";
    public static final String DINGTALK_TOKEN_HEADER = "Token";
    public static final String DINGTALK_ACCESS_PATH = "/access";
    public static final String DINGTALK_ACCESS_METHOD = "POST";

    public static final String AUTH_EXCEPTION = "auth_exception";
}
