package com.lakecat.web.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.lakecat.web.common.CommonParameters;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.entity.CurrentUser;
import com.lakecat.web.entity.InfTraceContextHolder;
import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.utils.GsonUtil;
import io.lakecat.catalog.client.LakeCatClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.lakecat.web.common.CommonMethods.*;

/**
 * 过滤器记录请求日志
 *
 * @author wuyan
 * @date 2018/10/26
 **/
@Slf4j
@Component
public class HttpRequestTraceFilter extends OncePerRequestFilter {


    private static Cache <String, Map <String, CatalogNameEnum.CloudRegionCatalog>> cacheForRegion = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(3, TimeUnit.MINUTES)
            .build();

    @Autowired
    CatalogNameEnum catalogNameEnum;


    @Autowired
    ILakeCatClientService iLakeCatClientService;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                 FilterChain chain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        log.info("requestUri={}", requestUri);
        boolean match = WebUtil.methodIsMatch(request, RequestConstant.IGNORE_METHOD)
                || WebUtil.pathIsMatch(request, RequestConstant.IGNORE_INTERCEPT_PATHS);

        if (match) {
            chain.doFilter(request, response);
            return;
        }
        String traceId = request.getHeader(CommonParameters.LOG_TRACE_ID);
        if (StringUtils.isBlank(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        }
        try {
            String authentication = request.getHeader("Authentication");
            /*CurrentUser currentUser=new CurrentUser();
            currentUser.setTenantName("shareit");
            currentUser.setUserName("ext.huangkai");
            currentUser.setUserId("ext.huangkai");*/
            String user = request.getHeader(CommonParameters.CURRENT_LOGIN_USER);
            String currentGroup = request.getHeader(CommonParameters.CURRENTGROUP);
            CurrentUser currentUser = JSON.parseObject(user, CurrentUser.class);
            String tenantName = currentUser.getTenantName();
            setAdmin(currentUser, tenantName);
            Map <String, CatalogNameEnum.CloudRegionCatalog> map = catalogNameEnum.initCloudRegionCatalog(tenantName, authentication);/*cacheForRegion.getIfPresent(tenantName);
            if (map == null||map.size()==0) {
                map = catalogNameEnum.initCloudRegionCatalog(tenantName, authentication);
                cacheForRegion.put(tenantName, map);
            }*/
            log.error("region-->{}",map);
            currentUser.setRegionInfo(map);

            InfTraceContextHolder.get().setUserInfo(currentUser);
            InfTraceContextHolder.get().setUserName(currentUser.getUserId());
            InfTraceContextHolder.get().setNewCode(currentUser.getGroupName());
            InfTraceContextHolder.get().setTenantName(currentUser.getTenantName());
            LakeCatClient lakeCatClient = iLakeCatClientService.get();
            InfTraceContextHolder.get().setLakeCatClient(lakeCatClient);
            InfTraceContextHolder.get().setAuthentication(authentication);
            InfTraceContextHolder.get().setUuid(request.getHeader("Uuid"));
            InfTraceContextHolder.get().setCurrentGroup(currentGroup);
            InfTraceContextHolder.get().setTraceId(traceId);
            MDC.put(CommonParameters.LOG_TRACE_ID, traceId);
            MDC.put(CommonParameters.LOG_TENANT_NAME, currentUser.getTenantName());
            chain.doFilter(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
