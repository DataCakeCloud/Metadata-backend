package com.lakecat.web;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by slj on 2022/5/30.
 */
public class Test0111 {


    public static void main(String args[]) {
        String str = "1232312321";
        String pattern = "[0-9]+";

        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(str);
        System.out.println(m.matches());
    }

}
