package com.lakecat.web;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.response.Response;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by slj on 2022/5/30.
 */
public class Test555 {


    public static void main(String[] args) {
        File file = new File("src/test/resources/task_table2.xls");
        String authentication = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdW5sb25namlhbmciLCJDT05URVhUX1VTRVJfSUQiOjk1MSwiQ09OVEVYVF9HUk9VUF9JRFMiOiI5MDA2MDUyOTkiLCJDT05URVhUX0VNQUlMIjoic3VubG9uZ2ppYW5nQHVzaGFyZWl0LmNvbSIsIkNPTlRFWFRfVEVOQU5UX0lEIjoxLCJDT05URVhUX1RFTkFOVF9OQU1FIjoic2hhcmVpdCIsIkNPTlRFWFRfVVNFUl9QQVNTV09SRCI6InVHSHNjR1lmZW5zY282T29xVmcrVEE9PSIsIkNPTlRFWFRfVVNFUl9URU5BTkNZX0NPREUiOiJTQ0FTLFNQUlMsQ0JTLEFEUyIsIkNPTlRFWFRfVVNFUl9PUkciOiJTUFJTLEJEUCIsImV4cCI6MTY4NDU3NTQ0Nn0.9amW6lLlBkf9_vVrJ-jxTUrVV3l516CDettJAh2ryOw";
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
            System.out.println("执行的表名为:" + dbName + "." + tableName);
            JSONObject search = DSUtil.searchAndRegion(tableName, authentication, current_login_user, dbName);
            if (search == null) {
                continue;
            }
            demo.setId(search.getInteger("id"));
            demo.setRegion(search.getString("region"));
            System.out.println(demo);
            DSUtil.changeTable(demo.getId(), demo.getOwner(), authentication, demo.getRegion(), current_login_user);

        }
        System.out.println(i);
    }
}
