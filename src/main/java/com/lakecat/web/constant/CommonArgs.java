package com.lakecat.web.constant;

import com.lakecat.web.exception.BusinessException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by slj on 2022/7/20.
 */
public class CommonArgs {

    public static Map <String, String> map1 = new HashMap <String, String>();
    public static Map <String, String> map2 = new HashMap <String, String>();
    public static Map <String, String> map3 = new HashMap <String, String>();

    static {
        map1.put("1", "个人");
        map1.put("2", "角色");
        map2.put("1", "否");
        map2.put("2", "是");
        map3.put("1", "权限申请");
        map3.put("2", "权限授予");

    }
}
