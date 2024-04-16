package com.lakecat.web;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.List;

/**
 * 离职人员变动
 * Created by slj on 2022/5/30.
 */
public class Test556 {


    public static void main(String[] args) {
        File file = new File("src/test/resources/task_table2.xls");
        String authentication ="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYW56ZW5nZ3VpIiwiQ09OVEVYVF9VU0VSX0lEIjoyNjcsIkNPTlRFWFRfR1JPVVBfSURTIjoiOTAwNjA1MzM4IiwiQ09OVEVYVF9FTUFJTCI6ImhhbnplbmdndWlAdXNoYXJlaXQuY29tIiwiQ09OVEVYVF9URU5BTlRfSUQiOjEsIkNPTlRFWFRfVEVOQU5UX05BTUUiOiJzaGFyZWl0IiwiQ09OVEVYVF9VU0VSX1BBU1NXT1JEIjoiaU9Fc2JtQlU1M2M9IiwiQ09OVEVYVF9VU0VSX1RFTkFOQ1lfQ09ERSI6IiNOL0EiLCJDT05URVhUX1VTRVJfT1JHIjoiQkRQIiwiZXhwIjoxNjkzMDQwNjI4fQ.8fm4O6BoLryvhf3HOU6nb4-smVzXdkRDa2vq7kIFNWI";
        JSONObject jsonObject1 = JSONObject.parseObject("{\"admin\":true,\"group\":\"BDP\",\"groupIds\":\"900605349\",\"id\":969,\"org\":\"BDP\",\"roles\":\"common,root,gatgeway\",\"tenantId\":1,\"tenantName\":\"shareit\",\"userId\":\"hanzenggui\",\"userName\":\"hanzenggui\"}");
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
            List<JSONObject> searchss = DSUtil.searchAndRegionss(tableName, authentication, current_login_user, dbName);
            if (searchss == null||searchss.size()==0) {
                continue;
            }
            for (JSONObject search:searchss){
                demo.setId(search.getInteger("id"));
                demo.setRegion(search.getString("region"));
                String region = search.getString("region");

                if (demo.getId() == null) {
                    System.out.println("该表异常----执行的表名为:" + dbName + "." + tableName);
                    continue;
                }
                System.out.println("执行的表名为:" + dbName + "." + tableName+":"+demo.getId());
                System.out.println(region);
                System.out.println(demo.getOwner());


                DSUtil.changeOwner(demo.getId(), demo.getOwner(), authentication, region, current_login_user);
            }


        }
        System.out.println(i);
    }
}
