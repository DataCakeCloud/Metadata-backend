package com.lakecat.web.common;

import com.alibaba.fastjson.JSON;
import com.lakecat.web.entity.CurrentUser;
import com.lakecat.web.entity.TableForCache;
import io.lakecat.catalog.common.Operation;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by slj on 2022/6/16.
 */
public class CommonParameters {

    public static final String LOG_TENANT_NAME = "tenantName";
    public static final String LOG_TRACE_ID = "traceId";
    public static final String SHARE_IT = "shareit";

    public static final String PER_TYPE = "1";

    public static String CURRENT_LOGIN_USER = "current_login_user";

    public static String CURRENTGROUP="CurrentGroup";


    public static Map <String,Operation> operation = new HashMap <>();

    public static Map <String, List <String>> mapForListTime = new HashMap <>();


    public static Map <String, Set <String>> SELECT_TABLE = new HashMap <>();
    public static Map <String, Set <String>> INSERT_TABLE = new HashMap <>();
    public static Map <String, Set <String>> DROP_TABLE = new HashMap <>();

    public static Map <String, String> map = new HashMap <>();
    public static Map <String, Integer> typeMap = new HashMap <>();
    public static Map <Integer, String> intcycleMap = new HashMap <>();
    public static Map <String, Integer> cycleIntMap = new HashMap <>();

    static {
        typeMap.put("个人",1);
        typeMap.put("角色",2);
        cycleIntMap.put("1天",1);
        cycleIntMap.put("1周",2);
        cycleIntMap.put("1个月",3);
        cycleIntMap.put("3个月",4);
        cycleIntMap.put("永久",5);

        intcycleMap.put(1,"1天");
        intcycleMap.put(2,"1周");
        intcycleMap.put(3,"1个月");
        intcycleMap.put(4,"3个月");
        intcycleMap.put(5,"永久");

    }


    public static List <String> admin = new ArrayList <>();
    public static List <String> white_list = new ArrayList <>();

    public static Map<String, String> operationConvert = new HashMap<>();

    static {
        operation.put("查询",Operation.SELECT_TABLE);
        operation.put("编辑",Operation.ALTER_TABLE);
        operation.put("描述",Operation.DESC_TABLE);
        operation.put("插入",Operation.INSERT_TABLE);
        operation.put("删除",Operation.DROP_TABLE);

        operation.put("查询数据",Operation.SELECT_TABLE);
        operation.put("修改表",Operation.ALTER_TABLE);
        operation.put("描述表",Operation.DESC_TABLE);
        operation.put("插入数据",Operation.INSERT_TABLE);
        operation.put("删除表",Operation.DROP_TABLE);

        //描述表,查询数据,修改表,插入数据,删除表
        operationConvert.put("查询", "查询数据");
        operationConvert.put("编辑", "修改表");
        operationConvert.put("描述", "描述表");
        operationConvert.put("插入", "插入数据");
        operationConvert.put("删除", "删除表");

        operationConvert.put("查询数据", "查询数据");
        operationConvert.put("修改表", "修改表");
        operationConvert.put("描述表", "描述表");
        operationConvert.put("插入数据", "插入数据");
        operationConvert.put("删除表", "删除表");





        //admin.add("ninebot");
        white_list.add("shareit");
        white_list.add("payment");
        white_list.add("bdp");
    }

}
