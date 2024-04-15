package com.lakecat.web.config;

/**
 * Created by slj on 2022/12/29.
 */
import java.lang.annotation.*;

/**
 * 支持多租户
 *
 * @author fengxiao
 * @date 2022/12/27
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MultiTenant {
}