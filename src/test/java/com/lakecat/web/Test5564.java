package com.lakecat.web;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.List;

/**
 * Created by slj on 2022/5/30.
 */
public class Test5564 {


    public static void main(String[] args) {
        File file = new File("src/test/resources/task_table4.xls");
        String authentication = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdW5sb25namlhbmciLCJDT05URVhUX1VTRVJfSUQiOjk1MSwiQ09OVEVYVF9HUk9VUF9JRFMiOiI5MDA2MDUyOTkiLCJDT05URVhUX0VNQUlMIjoic3VubG9uZ2ppYW5nQHVzaGFyZWl0LmNvbSIsIkNPTlRFWFRfVEVOQU5UX0lEIjoxLCJDT05URVhUX1RFTkFOVF9OQU1FIjoic2hhcmVpdCIsIkNPTlRFWFRfVVNFUl9QQVNTV09SRCI6InVHSHNjR1lmZW5zY282T29xVmcrVEE9PSIsIkNPTlRFWFRfVVNFUl9URU5BTkNZX0NPREUiOiJTQ0FTLFNQUlMsQ0JTLEFEUyIsIkNPTlRFWFRfVVNFUl9PUkciOiJTUFJTLEJEUCIsImV4cCI6MTY3ODE4NTA5OH0.Qx0RfVy6g0lxIL2AXWMQW-7sbLmTKXPyoG939YlBZDM";
        JSONObject jsonObject1 = JSONObject.parseObject("{\"admin\":true,\"group\":\"BDP\",\"groupIds\":\"900605349\",\"id\":969,\"org\":\"BDP\",\"roles\":\"common,root,gatgeway\",\"tenantId\":1,\"tenantName\":\"shareit\",\"userId\":\"sunlongjiang\",\"userName\":\"tianxu\"}");
        System.out.println(jsonObject1.toJSONString());
        String current_login_user = jsonObject1.toJSONString();
        List <Demo> list = ReadExcel.taskTable(file);
        int i = 0;
        for (Demo demo : list) {
            i++;
            String key = demo.getKey();
            if (key == null || key.equals("")) {
                continue;
            }

            if (key.equals("null")) {
                continue;
            }

            //analyst.temp_20210511_version4060018_coverage@ue1

            String[] split3 = key.split("\\.");

            if (split3.length != 2) {
                continue;
            }
            String dbName = split3[0];
            String tableName = split3[1];
            JSONObject searchHive = DSUtil.searchAndRegion(tableName + "_hive", authentication, current_login_user, dbName);
            JSONObject search = DSUtil.searchAndRegion(tableName, authentication, current_login_user, dbName);
            demo.setId(searchHive.getInteger("id"));
            demo.setRegion(searchHive.getString("region"));
            demo.setOwner(search.getString("owner"));
            if (demo.getId() == null) {
                System.out.println("该表异常----执行的表名为:" + dbName + "." + tableName);
                continue;
            }
            System.out.println("执行的表名为:" + dbName + "." + tableName);
            System.out.println(demo.getOwner());
            System.out.println(demo);


//            DSUtil.changeOwner(demo.getId(), demo.getOwner(), authentication, region, current_login_user);

        }
        System.out.println(i);
    }
}
