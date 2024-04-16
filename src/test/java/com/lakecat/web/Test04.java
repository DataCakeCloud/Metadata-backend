package com.lakecat.web;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.List;

/**
 * Created by slj on 2022/5/30.
 */
public class Test04 {

    public static void main(String[] args) {
        File file = new File("src/test/resources/result.xls");
        String authentication = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI0ODVkNTBkMTkwNzg0OTA2OTUzY2RlYWQ3YjEzOTgxOCIsImlhdCI6MTY1OTA2NjQ5OCwic3ViIjoidG9rZW4gYnkgdXNoYXJlaXQiLCJleHAiOjE2NTkxNTI4OTh9.qM_ZLwVZEWCpxpB8Lrw_GG2KN40gn_TJbzDQXSjGvcI";
        List <Demo> list = ReadExcel.readExcelOne(file);
        for (Demo demo : list) {
            JSONObject search = DSUtil.search(demo.getTableName(), authentication, demo.getDbName());
            demo.setId(search.getInteger("id"));
        }

        int i = 0;
        for (Demo demo : list) {
            System.out.println(demo);
            if (demo == null || demo.getId() == null) {
                i++;
                continue;
            }

            System.out.println(demo);
            DSUtil.changeOwner(demo.getId(), demo.getOwner(),authentication,"sg2","");
        }
        System.out.println("没有更新的数据为"+i);
    }
}
