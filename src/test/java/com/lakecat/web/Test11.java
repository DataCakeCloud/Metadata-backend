package com.lakecat.web;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.*;

/**
 * Created by slj on 2022/5/30.
 */
public class Test11 {


    static final Map <String, String> map = new HashMap <String, String>();
    static final Map <String, String> database = new HashMap <String, String>();
    static final Map <String, String> table = new HashMap <String, String>();

    static final Map <String, String> regions = new HashMap <String, String>();

    static {
        map.put("1", "ue1");
        map.put("2", "sg1");
        map.put("3", "sg2");
        map.put("4", "ue1,sg1,sg2");
//        DSUtil.addOwnerNotForPowner("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdW5sb25namlhbmciLCJDT05URVhUX1VTRVJfSUQiOjk1MSwiQ09OVEVYVF9HUk9VUF9JRFMiOiI5MDA2MDUyOTkiLCJDT05URVhUX0VNQUlMIjoic3VubG9uZ2ppYW5nQHVzaGFyZWl0LmNvbSIsIkNPTlRFWFRfVEVOQU5UX0lEIjoxLCJDT05URVhUX1RFTkFOVF9OQU1FIjoic2hhcmVpdCIsIkNPTlRFWFRfVVNFUl9QQVNTV09SRCI6InVHSHNjR1lmZW5zY282T29xVmcrVEE9PSIsIkNPTlRFWFRfVVNFUl9URU5BTkNZX0NPREUiOiJTQ0FTLFNQUlMsQ0JTLEFEUyIsIkNPTlRFWFRfVVNFUl9PUkciOiJTUFJTLEJEUCIsImV4cCI6MTY3ODQ0MDgxMX0.FleBVU2d-3qOUoMVSaeukUmmg80ZkhnwcY8006svziA");

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

        regions.put("AWS美东", "1");
        regions.put("AWS新加坡", "2");
        regions.put("华为 新加坡", "3");


    }

    public static void main(String[] args) {
        File file = new File("src/test/resources/createRole_07.xls");
        String authentication = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdW5sb25namlhbmciLCJDT05URVhUX1VTRVJfSUQiOjk1MSwiQ09OVEVYVF9HUk9VUF9JRFMiOiI5MDA2MDUyOTkiLCJDT05URVhUX0VNQUlMIjoic3VubG9uZ2ppYW5nQHVzaGFyZWl0LmNvbSIsIkNPTlRFWFRfVEVOQU5UX0lEIjoxLCJDT05URVhUX1RFTkFOVF9OQU1FIjoic2hhcmVpdCIsIkNPTlRFWFRfVVNFUl9QQVNTV09SRCI6InVHSHNjR1lmZW5zY282T29xVmcrVEE9PSIsIkNPTlRFWFRfVVNFUl9URU5BTkNZX0NPREUiOiJTQ0FTLFNQUlMsQ0JTLEFEUyIsIkNPTlRFWFRfVVNFUl9PUkciOiJTUFJTLEJEUCIsImV4cCI6MTY3OTcxMjEzMH0.gbxHi1fUvkY1r7jzkB4T0bgCmlBguaDg5t2uF1HotqE";
        List <Demo> list = ReadExcel.addUsersForRoleForTable(file);
        for (Demo demo : list) {

            if (demo.getRole() == null || demo.getRole().equals("")) {
                continue;

            }
            if (demo.getOwner() == null || demo.getOwner().equals("")) {
                continue;

            }

            if (demo.getFlag() == null || demo.getFlag().equals("")) {
                continue;

            }

            if (demo.getRegion() == null || demo.getRegion().equals("")) {
                continue;

            }
            String role = demo.getRole();
            String owner = demo.getOwner();
            String region = demo.getRegion();

            String sharitName = map.get(regions.get(region));

            String flag = demo.getFlag();
            String dbName = demo.getDbName();
            String tableName = demo.getTableName();
            List <String> dbNameList = new ArrayList <String>();
            JSONObject argName = new JSONObject();

            if (dbName.equals("*")) {

                for (int i = 0; i <= Test08.databaseList.size() - 1; i++) {
                    String dbNameAll = Test08.databaseList.getString(i);
                    dbNameList.add(dbNameAll.trim());
                }

            } else {

                if (dbName.contains(",")) {
                    String[] split = dbName.split(",");
                    for (String s : split) {
                        dbNameList.add(s.trim());
                    }
                } else {
                    String[] split = dbName.split("、");
                    for (String s : split) {
                        dbNameList.add(s.trim());
                    }
                }
            }
            List <String> finaldbNameList = new ArrayList <>();
            for (String s : dbNameList) {

                if (tableName.equals("*")) {

                    finaldbNameList.add(sharitName + "." + s.trim() + "." + "*");
                } else {
                    String[] tableNameList = null;
                    if (tableName.contains(",")) {
                        tableNameList = tableName.split(",");
                    } else {
                        tableNameList = tableName.split("、");
                    }
                    for (String s1 : tableNameList) {
                        finaldbNameList.add(sharitName + "." + s.trim() + "." + s1);
                    }

                }
                argName.put("objectNames", finaldbNameList);
            }

            String[] split1 = flag.split("&");
            for (String s : split1) {
                String s1 = table.get(s);
                String[] split = s1.split(",");
                if (argName.getJSONArray("operation") != null) {
                    JSONArray operation = argName.getJSONArray("operation");
                    operation.addAll(Arrays.asList(split));
                    argName.put("operation", operation);
                    continue;
                }
                argName.put("operation", split);
            }

            String[] ownerList = null;
            if (owner.contains(",")) {
                ownerList = owner.split(",");
            } else {
                ownerList = owner.split("、");
            }

            argName.put("objectNames", finaldbNameList);
            argName.put("objectType", "TABLE");
            argName.put("operations", new JSONObject());
            argName.put("ownerUser", "");
            argName.put("roleName", role);
            argName.put("userIds", ownerList);
//            argName.put("userIds", new ArrayList <>());
            System.out.println(argName);

//            Map <String, String> mapDelete = new HashMap <String, String>();
//            mapDelete.put("roleName", role);
//            DSUtil.deleteRole(mapDelete, authentication);
//
            JSONObject json = new JSONObject();
            json.put("ownerUser", "系统");
            json.put("roleName", role);
            DSUtil.createRole(json, authentication);
            DSUtil.addOwnerForRole(argName, authentication);
            DSUtil.addOwnerForPowner(argName, authentication);

        }
    }
}
