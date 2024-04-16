package com.lakecat.web;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.lakecat.web.http.BaseResponse;
import com.lakecat.web.response.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by slj on 2022/6/15.
 */
public class DSUtil {


    public static Response changeOwner(Integer id, String owner, String authentication, String region, String current_login_user) {
        System.out.println("表owner" + owner);
        HashMap <String, String> headers = new HashMap <>(2);
        headers.put("authentication", authentication);
        headers.put("current_login_user", current_login_user);
        String url = "https://ds-api-gateway.datacake.cloud/metadata/table/alterOwner?id=" + id + "&owner=" + owner + "&region=" + region;
        System.out.println(url);
        BaseResponse response = HttpUtil.get(url, null, headers);
        System.out.println(response);
        return Response.success("更新成功");
    }


    public static Response changeTable(Integer id, String owner, String authentication, String region, String current_login_user) {
        System.out.println("表owner" + owner);
        HashMap <String, String> headers = new HashMap <>(2);
        headers.put("authentication", authentication);
        headers.put("current_login_user", current_login_user);
        String url = "https://ds-api-gateway.datacake.cloud/metadata/table/migration?id=" + id + "&owner=" + owner + "&region=" + region;
        System.out.println(url);
        BaseResponse response = HttpUtil.get(url, null, headers);
        System.out.println(response);
        return Response.success("更新成功");
    }


    public static BaseResponse addColumns(String body, String authentication) {
        HashMap <String, String> headers = new HashMap <>(1);
        headers.put("authentication", authentication);
        BaseResponse response = HttpUtil.postWithJson(
                "https://lakecat-web.ushareit.org/metadata/table/addColumn", body, headers);
        return response;
    }

    public static JSONObject search(String tableName, String authentication, String dbName) {
        JSONObject result = new JSONObject();
        HashMap <String, String> headers = new HashMap <>(1);
        headers.put("authentication", authentication);
        BaseResponse response = HttpUtil.get(
                "https://lakecat-web.ushareit.org/metadata/table/search?userId=sunlongjiang&keyWord=" + tableName + "&pageNum=1&pageSize=10", null, headers);
        JSONObject data = JSONObject.parseObject(response.getData().toString());
        JSONArray results = data.getJSONArray("results");
        for (int i = 0; i < results.size(); i++) {
            JSONObject jsonObject = results.getJSONObject(i);
            String key = jsonObject.getString("db_name") + "." + jsonObject.getString("table_name");
            if (key.equals(dbName + "." + tableName)) {
                Integer id = jsonObject.getInteger("id");
                result.put("id", id);
            }
        }
        return result;
    }


    public static JSONObject searchAndRegion(String tableName, String authentication, String current_login_user, String dbName) {
        JSONObject result = new JSONObject();
        HashMap <String, String> headers = new HashMap <>(2);
        headers.put("current_login_user", current_login_user);
        headers.put("authentication", authentication);
        BaseResponse response = HttpUtil.get("https://ds-api-gateway.datacake.cloud/metadata/table/search?userId=hanzenggui&keyWord=" + tableName + "&pageNum=1&pageSize=10", null, headers);
        if (!"0".equals(response.getCode())) {
            return null;
        }
        JSONObject data = JSONObject.parseObject(response.getData().toString());
        JSONArray results = data.getJSONArray("results");
        for (int i = 0; i < results.size(); i++) {
            JSONObject jsonObject = results.getJSONObject(i);
            String key = jsonObject.getString("db_name") + "." + jsonObject.getString("table_name");
            if (key.equals(dbName + "." + tableName)) {
                Integer id = jsonObject.getInteger("id");
                String owner = jsonObject.getString("owner");
                String region = jsonObject.getString("region");
                result.put("id", id);
                result.put("owner", owner);
                result.put("region", region);
            }
        }
        return result;
    }

    public static List<JSONObject> searchAndRegionss(String tableName, String authentication, String current_login_user, String dbName) {
        List<JSONObject> list= Lists.newArrayList();
        HashMap <String, String> headers = new HashMap <>(2);
        headers.put("current_login_user", current_login_user);
        headers.put("authentication", authentication);
        BaseResponse response = HttpUtil.get("https://ds-api-gateway.datacake.cloud/metadata/table/search?userId=hanzenggui&keyWord=" + tableName + "&pageNum=1&pageSize=10", null, headers);
        if (!"0".equals(response.getCode())) {
            return null;
        }
        JSONObject data = JSONObject.parseObject(response.getData().toString());
        JSONArray results = data.getJSONArray("results");
        for (int i = 0; i < results.size(); i++) {
            JSONObject result = new JSONObject();
            JSONObject jsonObject = results.getJSONObject(i);
            String key = jsonObject.getString("db_name") + "." + jsonObject.getString("table_name");
            if (key.equals(dbName + "." + tableName)) {
                Integer id = jsonObject.getInteger("id");
                String owner = jsonObject.getString("owner");
                String region = jsonObject.getString("region");
                result.put("id", id);
                result.put("owner", owner);
                result.put("region", region);
            }
            list.add(result);
        }
        return list;
    }


    public static boolean searchNot(String tableName, String authentication) {
        HashMap <String, String> headers = new HashMap <>(1);
        headers.put("authentication", authentication);
        BaseResponse response = HttpUtil.get(
                "https://ds-api-gateway.datacake.cloud/metadata/table/search?userId=sunlongjiang&keyWord=" + tableName + "&pageNum=1&pageSize=10", null, headers);
        JSONObject data = JSONObject.parseObject(response.getData().toString());
        JSONArray results = data.getJSONArray("results");
        if (results.size() > 0) {
            return false;
        }
        return true;
    }


    public static Response createRole(JSONObject body, String authentication) {
        HashMap <String, String> headers = new HashMap <>(2);
        headers.put("authentication", authentication);
        JSONObject jsonObject1 = JSONObject.parseObject("{\"admin\":true,\"group\":\"BDP\",\"groupIds\":\"900605349\",\"id\":969,\"org\":\"BDP\",\"roles\":\"common,root,gatgeway\",\"tenantId\":1,\"tenantName\":\"shareit\",\"userId\":\"sunlongjiang\",\"userName\":\"tianxu\"}");
        String current_login_user = jsonObject1.toJSONString();
        headers.put("current_login_user", current_login_user);
        HttpUtil.postWithJson("https://ds-api-gateway.datacake.cloud/metadata/role/createRole", body.toJSONString(), headers);
//        HttpUtil.doPost("https://lakecat-web.ushareit.org/metadata/role/createRole", body, headers);
        return Response.success("更新成功");
    }


    public static Response addOwnerForRole(JSONObject body, String authentication) {
        HashMap <String, String> headers = new HashMap <>(2);
        headers.put("authentication", authentication);
        JSONObject jsonObject1 = JSONObject.parseObject("{\"admin\":true,\"group\":\"BDP\",\"groupIds\":\"900605349\",\"id\":969,\"org\":\"BDP\",\"roles\":\"common,root,gatgeway\",\"tenantId\":1,\"tenantName\":\"shareit\",\"userId\":\"sunlongjiang\",\"userName\":\"tianxu\"}");
        String current_login_user = jsonObject1.toJSONString();
        headers.put("current_login_user", current_login_user);
        HttpUtil.postWithJson("https://ds-api-gateway.datacake.cloud/metadata/role/addUsers", body.toJSONString(), headers);
        return Response.success("更新成功");
    }


    public static Response deleteRole(Map <String, String> body, String authentication) {
        HashMap <String, String> headers = new HashMap <>(2);
        JSONObject jsonObject1 = JSONObject.parseObject("{\"admin\":true,\"group\":\"BDP\",\"groupIds\":\"900605349\",\"id\":969,\"org\":\"BDP\",\"roles\":\"common,root,gatgeway\",\"tenantId\":1,\"tenantName\":\"shareit\",\"userId\":\"sunlongjiang\",\"userName\":\"tianxu\"}");
        String current_login_user = jsonObject1.toJSONString();
        headers.put("current_login_user", current_login_user);
        headers.put("authentication", authentication);
        HttpUtil.delete("https://ds-api-gateway.datacake.cloud/metadata/role/dropRole", body, headers);
        System.out.println("*******************************************");
        return Response.success("更新成功");
    }

    public static Response addOwnerForPowner(JSONObject body, String authentication) {

        HashMap <String, String> headers = new HashMap <>(2);
        JSONObject jsonObject1 = JSONObject.parseObject("{\"admin\":true,\"group\":\"BDP\",\"groupIds\":\"900605349\",\"id\":969,\"org\":\"BDP\",\"roles\":\"common,root,gatgeway\",\"tenantId\":1,\"tenantName\":\"shareit\",\"userId\":\"sunlongjiang\",\"userName\":\"tianxu\"}");
        String current_login_user = jsonObject1.toJSONString();
        headers.put("current_login_user", current_login_user);
        headers.put("authentication", authentication);
        BaseResponse baseResponse = HttpUtil.postWithJson("https://ds-api-gateway.datacake.cloud/metadata/role/grantPrivilegeToRole", body.toJSONString(), headers);
        System.out.println(baseResponse + "*******************************************");
        return Response.success("更新成功");
    }


    public static Response addOwnerNotForPowner(String authentication) {
        List <String> args = new ArrayList <>();
        args.add("ue1");
        args.add("sg2");
//        args.add("sg1");
        for (String arg : args) {
            HashMap <String, String> headers = new HashMap <>(2);
            JSONObject jsonObject1 = JSONObject.parseObject("{\"admin\":true,\"group\":\"BDP\",\"groupIds\":\"900605349\",\"id\":969,\"org\":\"BDP\",\"roles\":\"common,root,gatgeway\",\"tenantId\":1,\"tenantName\":\"shareit\",\"userId\":\"sunlongjiang\",\"userName\":\"tianxu\"}");
            String current_login_user = jsonObject1.toJSONString();
            headers.put("current_login_user", current_login_user);
            headers.put("authentication", authentication);
            BaseResponse response = HttpUtil.get("https://ds-api-gateway.datacake.cloud/metadata/role/showNoPrivileges?projectId=shareit&roleName=sunlongjiang222&region=" + arg, null, headers);

            JSONObject data = JSONObject.parseObject(response.getData().toString());
            Test08.tables.addAll(data.getJSONArray("DESC_TABLE"));
            Test08.databaseList.addAll(data.getJSONArray("DESC_DATABASE"));
        }

        System.out.println("*******************************************");
        return Response.success("更新成功");
    }


}
