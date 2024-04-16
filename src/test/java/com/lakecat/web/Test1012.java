package com.lakecat.web;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by slj on 2022/5/30.
 */
public class Test1012 {


    static final Map <String, String> map = new HashMap <String, String>();
    static final Map <String, String> database = new HashMap <String, String>();
    static final Map <String, String> table = new HashMap <String, String>();

    static final Map <String, String> regions = new HashMap <String, String>();


    public static JSONArray tables = new JSONArray();

    public static JSONArray databaseList = new JSONArray();


    static {
        map.put("1", "shareit_ue1");
        map.put("2", "shareit_sg1");
        map.put("3", "shareit_sg2");
        map.put("4", "shareit_ue1,shareit_sg1,shareit_sg2");
        DSUtil.addOwnerNotForPowner("");

        /**
         * 1=修改库；2=删除库；3=描述库；4=创建表；5=全选
         */
        database.put("1", "修改库");
        database.put("2", "删除库");
        database.put("3", "描述库");
        database.put("4", "创建表");
        database.put("5", "修改库,删除库,描述库,创建表");


        /**
         * 数据表权限：1=查询数据；2=插入数据；3=删除表；4=描述表  5= 修改表  6=全选
         */
        table.put("1", "查询数据");
        table.put("2", "插入数据");
        table.put("3", "删除表");
        table.put("4", "描述表");
        table.put("5", "修改表");
        table.put("6", "查询数据,插入数据,删除表,描述表,修改表");

        regions.put("ue1", "1");
        regions.put("sg1", "2");
        regions.put("sg2", "3");


    }

    public static void main(String[] args) {
        File file = new File("src/test/resources/result_select1.xls");
        String authentication = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIzNDg0NTU2MzgyZTI0NjE4ODI2M2Q1Yzk4MGNjMmE4ZiIsImlhdCI6MTY2MTc0MDUzOCwic3ViIjoidG9rZW4gYnkgdXNoYXJlaXQiLCJleHAiOjE2NjE4MjY5Mzh9.RZmSP89_3PW2512kphMN27WqrsdjwqABqnMXRp5WeO0";
        List <Demo> list = ReadExcel.addUsersForRoleForTableSelectUser(file);
        for (Demo demo : list) {

            if (demo.getOwner() == null || demo.getOwner().equals("") || demo.getOwner().equals("null")) {
                continue;
            }
            String owner1 = demo.getOwner();
            String role = "privilege_single_user_" + owner1.replace("\"", "").replace("\"", "");
            String owner = owner1.replace("\"", "").replace("\"", "");

            JSONObject argName = new JSONObject();

            argName.put("operation", "查询数据".split(","));

            argName.put("objectType", "TABLE");
            argName.put("operations", new JSONObject());
            argName.put("ownerUser", "");
            argName.put("roleName", role);
            List <String> ownerList = new ArrayList <>();
            ownerList.add(owner);
            argName.put("userIds", ownerList);
//            argName.put("userIds", new ArrayList <>());
            System.out.println("打印的参数是");
            System.out.println(argName);


            System.out.println("数据已经准备好。。。。。。。。。。。。。。。开始创建角色");
            JSONObject json = new JSONObject();
            json.put("ownerUser", "shilidong");
            json.put("roleName", role);
            System.out.println("执行的角色是" + role);
            DSUtil.createRole(json, authentication);
            System.out.println("开始给角色增加权限000000000");
            DSUtil.addOwnerForRole(argName, authentication);
//            Response response = DSUtil.addOwnerForPowner(argName, authentication);
        }
    }
}
