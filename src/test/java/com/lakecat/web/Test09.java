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
public class Test09 {


    static final Map <String, String> map = new HashMap <String, String>();
    static final Map <String, String> database = new HashMap <String, String>();

    static final Map <String, String> regions = new HashMap <String, String>();


    public static JSONArray tables = new JSONArray();

    public static JSONArray databaseList = new JSONArray();


    static {
        map.put("1", "shareit_ue1");
        map.put("2", "shareit_sg1");
        map.put("3", "shareit_sg2");
        map.put("4", "shareit_ue1,shareit_sg1,shareit_sg2");


        /**
         * 1=修改库；2=删除库；3=描述库；4=创建表；5=全选
         */
        database.put("1", "修改库");
        database.put("2", "删除库");
        database.put("3", "描述库");
        database.put("4", "创建表");
        database.put("5", "修改库,删除库,描述库,创建表");


        regions.put("AWS美东", "1");
        regions.put("AWS新加坡", "2");
        regions.put("华为 新加坡", "3");


    }

    public static void main(String[] args) {


        DSUtil.addOwnerNotForPowner("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdW5sb25namlhbmciLCJDT05URVhUX1VTRVJfSUQiOjk1MSwiQ09OVEVYVF9HUk9VUF9JRFMiOiI5MDA2MDUyOTkiLCJDT05URVhUX0VNQUlMIjoic3VubG9uZ2ppYW5nQHVzaGFyZWl0LmNvbSIsIkNPTlRFWFRfVEVOQU5UX0lEIjoxLCJDT05URVhUX1RFTkFOVF9OQU1FIjoic2hhcmVpdCIsIkNPTlRFWFRfVVNFUl9QQVNTV09SRCI6InVHSHNjR1lmZW5zY282T29xVmcrVEE9PSIsIkNPTlRFWFRfVVNFUl9URU5BTkNZX0NPREUiOiJTQ0FTLFNQUlMsQ0JTLEFEUyIsIkNPTlRFWFRfVVNFUl9PUkciOiJTUFJTLEJEUCIsImV4cCI6MTY3ODE1ODUwOX0.lyH0mvxQ6olXf71Iouah1Q9PWo_ZM80LpXJeF-UVV9A");

        int size1 = Test08.databaseList.size();
        int size2 = Test08.tables.size();

        System.out.println(size1);
        System.out.println(size2);


    }
}
