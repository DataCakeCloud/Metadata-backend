package com.lakecat.web;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by slj on 2022/5/30.
 */
public class Test07 {



    static final Map<String, String> map = new HashMap<String, String>();

    static {
        map.put("1","shareit_ue1");
        map.put("2","shareit_sg1");
        map.put("3","shareit_sg2");
        map.put("4","shareit_ue1,shareit_sg1,shareit_sg2");
    }

    public static void main(String[] args) {
        File file = new File("src/test/resources/createRole.xls");
        String authentication = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4YjZiMTIxN2FjMjc0NmQ2YWQ3MWE0MWUxNzA0YmE1MSIsImlhdCI6MTY1NjMwMDg0OCwic3ViIjoidG9rZW4gYnkgdXNoYXJlaXQiLCJleHAiOjE2NTYzODcyNDh9.kGX9qXs-HWT28-mNqUrNrmbsOWX0Bsjz4xcECLm1cI8";
        List <Demo> list = ReadExcel.addUsersForRoleForRegion(file);
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
            String flag = demo.getFlag();
            if(flag.equals("否")){
                continue;
            }
            JSONObject argName = new JSONObject();
            String[] split1 = region.split("&");
            for (String s : split1) {
                String s1 = map.get(s);
                String[] split = s1.split(",");
                argName.put("objectNames", split);
            }
            String[] split = owner.split(",");
            argName.put("objectType", "CATALOG");
            ArrayList <String> objects = new ArrayList <>();
            objects.add("CREATE_DATABASE");
            argName.put("operation", objects);
            argName.put("operations", new JSONObject());
            argName.put("ownerUser", "");
            argName.put("roleName", role);
            argName.put("userIds", split);
            System.out.println(argName);
            DSUtil.addOwnerForPowner(argName, authentication);

        }
    }
}
