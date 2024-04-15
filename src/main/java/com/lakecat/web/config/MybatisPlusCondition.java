package com.lakecat.web.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author lcg
 */
public class MybatisPlusCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean b = Boolean.parseBoolean(context.getEnvironment().getProperty("enable.dbshard"));
        return b;
    }
}
