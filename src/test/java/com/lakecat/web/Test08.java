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
public class Test08 {


    static final Map <String, String> map = new HashMap <String, String>();
    static final Map <String, String> database = new HashMap <String, String>();

    static final Map <String, String> regions = new HashMap <String, String>();


    public static JSONArray tables = new JSONArray();

    public static JSONArray databaseList = new JSONArray();


    static {
        map.put("1", "ue1");
        map.put("2", "sg1");
        map.put("3", "sg2");
        map.put("4", "ue1,sg1,sg2");
        DSUtil.addOwnerNotForPowner("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdW5sb25namlhbmciLCJDT05URVhUX1VTRVJfSUQiOjk1MSwiQ09OVEVYVF9HUk9VUF9JRFMiOiI5MDA2MDUyOTkiLCJDT05URVhUX0VNQUlMIjoic3VubG9uZ2ppYW5nQHVzaGFyZWl0LmNvbSIsIkNPTlRFWFRfVEVOQU5UX0lEIjoxLCJDT05URVhUX1RFTkFOVF9OQU1FIjoic2hhcmVpdCIsIkNPTlRFWFRfVVNFUl9QQVNTV09SRCI6InVHSHNjR1lmZW5zY282T29xVmcrVEE9PSIsIkNPTlRFWFRfVVNFUl9URU5BTkNZX0NPREUiOiJTQ0FTLFNQUlMsQ0JTLEFEUyIsIkNPTlRFWFRfVVNFUl9PUkciOiJTUFJTLEJEUCIsImV4cCI6MTY3ODc3ODEyOH0._U4fZN1QMDWH-HmqN__dzHbXgLGOldwoE99IV1mdAm8");

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
        File file = new File("src/test/resources/createRole_011.xls");
        String authentication = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdW5sb25namlhbmciLCJDT05URVhUX1VTRVJfSUQiOjk1MSwiQ09OVEVYVF9HUk9VUF9JRFMiOiI5MDA2MDUyOTkiLCJDT05URVhUX0VNQUlMIjoic3VubG9uZ2ppYW5nQHVzaGFyZWl0LmNvbSIsIkNPTlRFWFRfVEVOQU5UX0lEIjoxLCJDT05URVhUX1RFTkFOVF9OQU1FIjoic2hhcmVpdCIsIkNPTlRFWFRfVVNFUl9QQVNTV09SRCI6InVHSHNjR1lmZW5zY282T29xVmcrVEE9PSIsIkNPTlRFWFRfVVNFUl9URU5BTkNZX0NPREUiOiJTQ0FTLFNQUlMsQ0JTLEFEUyIsIkNPTlRFWFRfVVNFUl9PUkciOiJTUFJTLEJEUCIsImV4cCI6MTY3ODc3ODEyOH0._U4fZN1QMDWH-HmqN__dzHbXgLGOldwoE99IV1mdAm8";
        List <Demo> list = ReadExcel.addUsersForRole(file);
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
            String[] dbNameList = null;
            JSONObject argName = new JSONObject();
            if (dbName.contains(",")) {
                dbNameList = dbName.split(",");
            } else {
                dbNameList = dbName.split("、");
            }
            List <String> finaldbNameList = new ArrayList <>();
            for (String s : dbNameList) {

                if (s.equals("*")) {

                    finaldbNameList.add(sharitName + "." + "*");

                } else if (s.contains("*")) {
                    String prefix = s.split("\\*")[0];

                    for (int i = 0; i <= Test08.databaseList.size() - 1; i++) {
                        String dbNameAll = Test08.databaseList.getString(i);
                        if (dbNameAll.contains(prefix)) {
                            finaldbNameList.add(dbNameAll.trim());
                        }
                    }
                } else {
                    finaldbNameList.add(sharitName + "." + s.trim());
                }
                argName.put("objectNames", finaldbNameList);
            }

            String[] split1 = flag.split("&");
            for (String s : split1) {
                String s1 = database.get(s);
                String[] split = s1.split(",");
                argName.put("operation", split);
            }
            String[] ownerList = null;
            if (owner.contains(",")) {
                ownerList = owner.split(",");
            } else {
                ownerList = owner.split("、");
            }
            argName.put("objectType", "CATALOG");
            argName.put("operations", new JSONObject());
            argName.put("ownerUser", "");
            argName.put("roleName", role);
            argName.put("userIds", ownerList);
            argName.put("userIds", new ArrayList <>());
            System.out.println("执行参数为" + argName);

            JSONObject json = new JSONObject();
            json.put("ownerUser", "shilidong");
            json.put("roleName", role);
            DSUtil.createRole(json, authentication);
            DSUtil.addOwnerForRole(argName, authentication);
            DSUtil.addOwnerForPowner(argName, authentication);
//
        }
    }
}
