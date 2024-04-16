package com.lakecat.web;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.*;

/**
 * Created by slj on 2022/5/30.
 */
public class Test112 {


    public static void main(String[] args) {
        File file = new File("src/test/resources/createRole_04.xls");
        String authentication = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ZTY5MGY4MmFiZGU0NzMxOTg3ZmJmZWE2NDBhYThjZSIsImlhdCI6MTY2Nzk2MDg5Nywic3ViIjoidG9rZW4gYnkgdXNoYXJlaXQiLCJleHAiOjE2NjgwNDcyOTd9.eK7zNki3C2dcQh9450apAaNTZxUZ1iFnh5MVmdMGhJY";
        List <String> finaldbNameList = new ArrayList <>();
        JSONObject argName = new JSONObject();
            finaldbNameList.clear();
            finaldbNameList.add("shareit_ue1.mps_dmp.*");
            argName.put("objectNames", finaldbNameList);
            argName.put("objectType", "TABLE");
            argName.put("operations", new JSONObject());
            argName.put("ownerUser", "");
            argName.put("roleName", "privilege_single_user_hurh");
//            argName.put("userIds", ownerList);
            argName.put("userIds", new ArrayList <>());
            System.out.println(argName);

//            Map <String, String> mapDelete = new HashMap <String, String>();
//            mapDelete.put("roleName", role);
//            DSUtil.deleteRole(mapDelete, authentication);

//            System.out.println("数据已经准备好。。。。。。。。。。。。。。。开始创建角色");
//           JSONObject json = new JSONObject();
//            json.put("ownerUser", "shilidong");
//            json.put("roleName", role);
//            System.out.println("执行的角色是"+role);
//            DSUtil.createRole(json, authentication);
//            System.out.println("开始给角色增加权限000000000");
//            DSUtil.addOwnerForRole(argName, authentication);
            DSUtil.addOwnerForPowner(argName, authentication);

        }
}
