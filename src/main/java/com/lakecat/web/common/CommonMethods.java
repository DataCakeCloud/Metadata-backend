package com.lakecat.web.common;

import com.lakecat.web.entity.CurrentUser;

import java.util.regex.Pattern;

import static com.lakecat.web.common.CommonParameters.SHARE_IT;

/**
 * @author slj
 * @date 2022/6/16
 */
public class CommonMethods {


    public static CurrentUser administrator(String tenantName) {
        CurrentUser userInfo = new CurrentUser();
        setAdmin(userInfo, tenantName);
        return userInfo;
    }


    public static void setAdmin(CurrentUser userInfo, String tenantName) {
        if (CommonParameters.admin.contains(tenantName.toLowerCase())) {
            userInfo.setTenantName("bdp");
        } else {
            userInfo.setTenantName(tenantName);
        }
    }


    public static boolean isAdmin(String tenantName) {
        return CommonParameters.admin.contains(tenantName.toLowerCase());
    }


}
