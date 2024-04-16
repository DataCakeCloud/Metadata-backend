package com.lakecat.web;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by slj on 2022/5/30.
 */
public class Test06 {

    public static void main(String[] args) {
        File file = new File("src/test/resources/createRole.xls");
        String authentication = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4YjZiMTIxN2FjMjc0NmQ2YWQ3MWE0MWUxNzA0YmE1MSIsImlhdCI6MTY1NjMwMDg0OCwic3ViIjoidG9rZW4gYnkgdXNoYXJlaXQiLCJleHAiOjE2NTYzODcyNDh9.kGX9qXs-HWT28-mNqUrNrmbsOWX0Bsjz4xcECLm1cI8";
        List <Demo> list = ReadExcel.readExcelOneForRole(file);
        for (Demo demo : list) {
            if(demo.getRole()==null || demo.getRole().equals("")){
                continue;

            }            //创建角色
           JSONObject json = new JSONObject();
            json.put("ownerUser", "shilidong");
            json.put("roleName", demo.getRole());
            DSUtil.createRole(json, authentication);
            System.out.println(demo.getRole());

        }
    }
}
