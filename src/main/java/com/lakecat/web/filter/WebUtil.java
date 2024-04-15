package com.lakecat.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author zhaopan
 * @date 2018/11/14
 **/
@Slf4j
public class WebUtil {

    public static String UNKNOWN = "unknown";

    /**
     * 判断请求是否任意match条件
     *
     * @param request       request
     * @param matchMethods  match method
     * @param matchContents match content
     * @param mathPaths     match path
     *
     * @return
     */
    public static boolean requestAnyIsMatch(HttpServletRequest request, String[] matchMethods, String[] matchContents,
                                            String[] mathPaths) {

        return methodIsMatch(request, matchMethods)
                || contentIsMatch(request, matchContents)
                || pathIsMatch(request, mathPaths);
    }

    /**
     * 判断请求是否全部match条件
     *
     * @param request       request
     * @param matchMethods  match method
     * @param matchContents match content
     * @param mathPaths     match path
     *
     * @return
     */
    public static boolean requestAllIsMatch(HttpServletRequest request, String[] matchMethods, String[] matchContents,
                                            String[] mathPaths) {

        return methodIsMatch(request, matchMethods)
                && contentIsMatch(request, matchContents)
                && pathIsMatch(request, mathPaths);
    }

    /**
     * 判断请求method是否match条件方法
     *
     * @param request      request
     * @param matchMethods matchMethods
     *
     * @return
     */
    public static boolean methodIsMatch(HttpServletRequest request, String[] matchMethods) {

        if (!ArrayUtils.isEmpty(matchMethods)) {
            for (String method : matchMethods) {
                if (Objects.equals(method, request.getMethod())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断请求content是否match条件方法
     *
     * @param request       request
     * @param matchContents matchContents
     *
     * @return
     */
    public static boolean contentIsMatch(HttpServletRequest request, String[] matchContents) {
        if (!ArrayUtils.isEmpty(matchContents)) {
            for (String content : matchContents) {
                if (request.getContentType() != null &&
                        request.getContentType().toLowerCase().contains(content.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断请求path是否match条件方法
     *
     * @param request   request
     * @param mathPaths mathPaths
     *
     * @return
     */
    public static boolean pathIsMatch(HttpServletRequest request, String[] mathPaths) {
        PathMatcher pathMatcherToUse = new AntPathMatcher();
        String requestPath =
                StringUtils.isBlank(request.getServletPath()) ? request.getRequestURI() : request.getServletPath();
        if (!ArrayUtils.isEmpty(mathPaths)) {
            for (String pattern : mathPaths) {
                if (pathMatcherToUse.match(pattern, requestPath)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 复制输入流
     *
     * @param request request
     * @return InputStream
     */
    public static InputStream cloneInputStream(HttpServletRequest request) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[60];
        int len = 0;
        InputStream inputStream = request.getInputStream();
        while (len > -1) {
            try{
                len = inputStream.read(buffer);
            }catch (Exception exception){
                log.error("clone input stream error",exception);
            }
            if(len >= 0 ){
                byteArrayOutputStream.write(buffer, 0, len);
            }

        }
        byteArrayOutputStream.flush();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR" };

    public static String getClientIpAddress(HttpServletRequest request) {
        String ip = null;
        for (String header : IP_HEADER_CANDIDATES) {
            ip = request.getHeader(header);
            if (StringUtils.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
                break;
            }
        }

        if(StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();
        }

        if(StringUtils.isNotBlank(ip)){
            String[] ipArray = ip.split(", ");
            if (ipArray.length >= 1) {
                ip = ipArray[ipArray.length - 1];
            }
        }

        return ip;
    }

}
