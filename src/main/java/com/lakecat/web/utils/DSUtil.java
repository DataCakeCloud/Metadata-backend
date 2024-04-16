package com.lakecat.web.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.http.BaseResponse;
import com.lakecat.web.http.HttpUtil;
import com.lakecat.web.response.Response;

import java.util.HashMap;
/**
 * Created by slj on 2022/6/15.
 */
public class DSUtil {


    public static Response changeOwner(Integer id, String owner, String authentication) {
        HashMap <String, String> headers = new HashMap <>(1);
        headers.put("authentication", authentication);
        HttpUtil.get("https://lakecat-web.ushareit.org/metadata/table/alterOwner?id=" + id + "&owner=" + owner, null, headers);
        return Response.success("更新成功");
    }


    public static BaseResponse post(String url, String body, String token) {
        HashMap <String, String> headers = new HashMap <>(1);
        headers.put("token", token);
        BaseResponse response = HttpUtil.postWithJson(
                url, body, headers);
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
            String title = jsonObject.getString("title");
            if (title.equals(dbName + "." + tableName)) {
                Integer id = jsonObject.getInteger("id");
                String owner = jsonObject.getString("owner");
                result.put("id", id);
                result.put("owner", owner);
            }
        }
        return result;
    }


}
