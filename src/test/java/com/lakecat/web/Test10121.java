package com.lakecat.web;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.utils.GsonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by slj on 2022/5/30.
 */
public class Test10121 {


    public static void main(String[] args) {

       String tableName="temp_database.user_type_0730_haoyi";
        String[] split = tableName.split("\\.");
        System.out.println(split[0]);
        System.out.println(split[1]);

    }
}
