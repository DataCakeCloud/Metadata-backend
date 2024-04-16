package com.lakecat.web;

import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.entity.RoleInputs;
import com.lakecat.web.http.BaseResponse;
import com.lakecat.web.response.Response;
import com.lakecat.web.utils.GsonUtil;
import org.assertj.core.util.Lists;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class TestRole {
    public static void main(String[] args) throws Exception {
        BufferedReader bufferedReader=new BufferedReader(new FileReader("D:\\data\\privilege.txt"));
        int i=0;
        String line="";
        while ((line=bufferedReader.readLine())!=null){
            i++;
            if (i>2){
                String arr[]=line.split("\\|");
                Demo demo=new Demo();
                demo.setRole("privilege_single_user_"+arr[3].trim());
                demo.setOwner(arr[3].trim());
                demo.setTableName(arr[1].trim());
                demo.setDbName(arr[0].trim());
                if (!"WRITE".equals(arr[2].trim())){
                    continue;
                }


                RoleInputs roleInputs =new RoleInputs();
                roleInputs.setRoleName(demo.getRole());
                roleInputs.setOwnerUser(demo.getOwner());
                if ("READ".equals(arr[2].trim())){
                    String[] a=new String[]{"查询数据"};
                    roleInputs.setOperation(a);
                }
                if ("WRITE".equals(arr[2].trim())){
                    String[] a=new String[]{"插入数据"};
                    roleInputs.setOperation(a);
                }


                roleInputs.setObjectType("TABLE");
                String[] b=new String[]{"ue1."+demo.getDbName()+"."+demo.getTableName()};
                roleInputs.setObjectNames(b);
                HashMap<String, String> headers = new HashMap <>(2);
                JSONObject jsonObject1 = JSONObject.parseObject("{\"admin\":true,\"group\":\"BDP\",\"groupIds\":\"900605349\",\"id\":969,\"org\":\"BDP\",\"roles\":\"common,root,gatgeway\",\"tenantId\":1,\"tenantName\":\"shareit\",\"userId\":\"hanzenggui\",\"userName\":\"hanzenggui\"}");
                String current_login_user = jsonObject1.toJSONString();
                headers.put("current_login_user", current_login_user);
                headers.put("authentication", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYW56ZW5nZ3VpIiwiQ09OVEVYVF9VU0VSX0lEIjoyNjcsIkNPTlRFWFRfR1JPVVBfSURTIjoiOTAwNjA1MzM4IiwiQ09OVEVYVF9FTUFJTCI6ImhhbnplbmdndWlAdXNoYXJlaXQuY29tIiwiQ09OVEVYVF9URU5BTlRfSUQiOjEsIkNPTlRFWFRfVEVOQU5UX05BTUUiOiJzaGFyZWl0IiwiQ09OVEVYVF9VU0VSX1BBU1NXT1JEIjoiaU9Fc2JtQlU1M2M9IiwiQ09OVEVYVF9VU0VSX1RFTkFOQ1lfQ09ERSI6IiNOL0EiLCJDT05URVhUX1VTRVJfT1JHIjoiQkRQIiwiZXhwIjoxNjg2MDE3ODEyfQ.cZR-SSNWfBu87V69l6BA0lIBsb9rmLSV8U2jHSz9DXY");
                JSONObject json = new JSONObject();
                json.put("ownerUser", roleInputs.getOwnerUser());
                json.put("roleName", roleInputs.getRoleName());
                System.out.println(GsonUtil.toJson(roleInputs,false));
                DSUtil.createRole(json, headers.get("authentication"));
                BaseResponse baseResponse = HttpUtil.postWithJson("https://ds-api-gateway.datacake.cloud/metadata/role/grantPrivilegeToRole", GsonUtil.toJson(roleInputs,false), headers);
                System.out.println(baseResponse + "*******************************************");
            }
        }
    }
}
