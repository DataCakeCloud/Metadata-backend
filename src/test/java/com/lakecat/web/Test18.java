package com.lakecat.web;

import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.http.BaseResponse;

import java.util.HashMap;

/**
 * Created by slj on 2022/7/7.
 */
public class Test18 {


    public static void main(String[] args) {

        HashMap <String, String> headers = new HashMap <>(1);
        headers.put("authentication", "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwZDk5NTU0NWVkMmI0YWZiODk2ZTJiNDQxYWY2MTZiZiIsImlhdCI6MTY1NzEwNDE0MCwic3ViIjoidG9rZW4gYnkgdXNoYXJlaXQiLCJleHAiOjE2NTcxOTA1NDB9.SqvSxT6_OfPU64hR-arFAcguVMDgQWXjg-_ZotutoA0");

        BaseResponse response = HttpUtil.get("https://ds-task.ushareit.org/label/list",null,headers);

        System.out.println(response);

    }
}
