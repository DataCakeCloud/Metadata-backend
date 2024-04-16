package com.lakecat.web;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by slj on 2022/5/30.
 */
public class Test02 {

    public static void main(String[] args) {
        File file = new File("src/test/resources/niuqianxiong_columns.xls");
        String authentication = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxMzY2YzcxNDBiZTg0MDJlYmY5YjhiOTdjNTg1Zjk1MSIsImlhdCI6MTY1NTA5MDY0MSwic3ViIjoidG9rZW4gYnkgdXNoYXJlaXQiLCJleHAiOjE2NTUxNzcwNDF9.orrTyz_cTIjXAvClXeepwaVPNXOwJpeFT9RIeeqZh8I";
        List <Demo01> list = ReadExcel.readExcelUtilsForClu(file);
        Map <String, Demo01> map = new HashMap <>();
        for (Demo01 demo : list) {
            String dbName = demo.getDbName();
            String tableName = demo.getTableName();
            if (dbName == null || tableName == null) {
                continue;
            }
            String key = dbName + "." + tableName;
            if (!map.containsKey(key)) {
                Demo01 demo01 = new Demo01();
                JSONObject search = DSUtil.search(demo.getTableName(), authentication, demo.getDbName());
                demo01.setDbName(dbName);
                demo01.setTableName(tableName);
                demo01.setRegion(demo.getRegion());
                demo01.setId(search.getInteger("id"));
                JSONObject column = new JSONObject();
                column.put("name", demo.getName());
                column.put("comment", demo.getComment());
                column.put("type", demo.getType());
                JSONArray jsonArray = new JSONArray();
                jsonArray.add(column);
                demo01.setColumns(jsonArray);
                map.put(key, demo01);
            } else {
                JSONObject column = new JSONObject();
                column.put("name", demo.getName());
                column.put("comment", demo.getComment());
                column.put("type", demo.getType());
                Demo01 demo01 = map.get(key);
                JSONArray columns = demo01.getColumns();
                columns.add(column);
                demo01.setColumns(columns);
                map.put(key, demo01);
            }
        }
        for (String s : map.keySet()) {

            Demo01 demo01 = map.get(s);
            String dbName = demo01.getDbName();
            String tableName = demo01.getTableName();
            JSONArray columns = demo01.getColumns();
            String region = demo01.getRegion();
            Integer id = demo01.getId();
            if (id == null) {
                continue;
            }
            JSONObject data = new JSONObject();
            data.put("id", id);
            data.put("region", region);
            data.put("dbName", dbName);
            data.put("name", tableName);
            data.put("columns", columns.toJSONString());

            System.out.println(data);
            DSUtil.addColumns(data.toJSONString(), authentication);
        }

    }
}
