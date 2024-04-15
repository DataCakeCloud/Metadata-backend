package com.lakecat.web.config;

/**
 * Created by slj on 2022/12/29.
 */

import com.lakecat.web.common.CommonParameters;
import com.lakecat.web.entity.InfTraceContextHolder;
import com.lakecat.web.utils.DSUtil;
import com.lakecat.web.utils.DSUtilForLakecat;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Aspect
@Component
//@Order(-1)
@Conditional(MybatisPlusCondition.class)
public class MultiTenantAspect {


    @Autowired
    DSUtilForLakecat dsUtil;


    @Pointcut("@annotation(com.lakecat.web.config.MultiTenant)")
    public void multi() {
    }
    @Around("multi() && @annotation(multiTenant)")
    public Object doAround(ProceedingJoinPoint joinPoint, MultiTenant multiTenant) throws Throwable {

        List <String> allTenantName = dsUtil.getAllTenantName();
        return allTenantName.stream().map(item -> {
            InfTraceContextHolder.get().setTenantName(item);
            MDC.put(CommonParameters.LOG_TRACE_ID, String.format("%s_%s", item, InfTraceContextHolder.get().getTraceId()));
            try {
                log.info("start execute for tenant {}", item);
                return joinPoint.proceed();
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                return null;
            } finally {
                log.info("finish execute for tenant {}", item);
                InfTraceContextHolder.get().setTenantName(null);
            }
        }).filter(Objects::nonNull).reduce((x, y) -> x).orElse(null);
    }
}
