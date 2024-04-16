package com.lakecat.web;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.List;

/**
 * Created by slj on 2022/5/30.
 */
public class Test01 {

    public static void main(String[] args) {
        File file = new File("src/test/resources/changOnwer3.xls");
        String authentication = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIyY2JjYWVmZjU0YzA0OGY4YWUwODBmNDgwZDczNDQ5MCIsImlhdCI6MTY2OTAxNzQ0Mywic3ViIjoidG9rZW4gYnkgdXNoYXJlaXQiLCJleHAiOjE2NjkxMDM4NDN9.oxO4WzBYOuRhu0XTWyvCauoPrIYHL81-AJgpfSRg1EE";
        List <Demo> list = ReadExcel.readExcelUtils_TEST(file);
        for (Demo demo : list) {
            String tableName = demo.getTableName();
            if (tableName == null || tableName.equals("")) {
                continue;
            }
            String[] split = tableName.split("\\.");
            if (split.length != 2) {
                continue;
            }
            String dbName = split[0];
            String table = split[1];
            JSONObject search = DSUtil.search(table, authentication, dbName);
            demo.setId(search.getInteger("id"));
        }

        for (Demo demo : list) {
            if (demo == null || demo.getId() == null) {
                continue;
            }
            /**
             * {
             *     "id":"32284",
             *     "interval":null,
             *     "hierarchical":"dws",
             *     "subject":"user",
             *     "application":"SHAREIT_A",
             *     "description":"sadas"
             * }
             */
//            System.out.println(demo);
            JSONObject name = new JSONObject();
            name.put("id", demo.getId());
            name.put("interval", demo.getInterval());
            name.put("hierarchical", demo.getHierarchical());
            name.put("subject", demo.getSubject());
            name.put("application", demo.getApplication());
            name.put("description", demo.getDescription());
            System.out.println(demo.getId() + "====" + demo.getOwner() + "====" + demo.getNewOwner());
//            DSUtil.changeTableDetail(name.toJSONString(), authentication);
            DSUtil.changeOwner(demo.getId(), demo.getOwner(),authentication,"ue1","");
        }
    }
}
