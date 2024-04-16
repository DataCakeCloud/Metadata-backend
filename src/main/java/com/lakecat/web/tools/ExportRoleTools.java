package com.lakecat.web.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.http.BaseResponse;
import com.lakecat.web.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
public class ExportRoleTools {

    static String SHOW_ROLE_URL = "https://lakecat-web.ushareit.org//metadata/role/showRoles";
    static String SHOW_USERS_BY_ROLE_URL = "https://lakecat-web.ushareit.org//metadata/role/showUsers";

    public static void main(String[] args) {
        getRoles(null);
    }

    public static void getRoles(String authentication) {
        ArrayList<String> list = new ArrayList<>();
        HashMap<String, String> headers = new HashMap <>(1);
        //headers.put("authentication", authentication);
        BaseResponse response = HttpUtil.get(
            SHOW_ROLE_URL, null, headers);

        JSONArray roles = JSON.parseArray(response.getData().toString());
        for (int i = 0; i < roles.size(); i++) {
            log.info("data: {}", roles.get(i));
            getUsersByRole(roles.get(i), list);
        }
        try {
            writeToFile(list, "role_user_mapping.txt");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void getUsersByRole(Object o, ArrayList<String> list) {
        JSONObject object = JSON.parseObject(JSONObject.toJSONString(o));
        HashMap<String, String> headers = new HashMap <>(1);
        //headers.put("authentication", authentication);
        HashMap<String, String> map = new HashMap<>();
        map.put("projectId", "shareit");
        String roleName = object.getString("roleName").trim();
        map.put("roleName", roleName);
        BaseResponse response = HttpUtil.get(SHOW_USERS_BY_ROLE_URL, map, headers);
        log.info("response: {}", response);
        if (response.getMessage().equals("success")){
            log.info("Data: {}", response.getData().toString());
            JSONArray toUsers = JSON.parseObject(response.getData().toString()).getJSONArray("toUsers");
            if (CollectionUtils.isNotEmpty(toUsers)) {
                StringBuffer sb = new StringBuffer(roleName).append(":");
                for (int i = 0; i < toUsers.size(); i++) {
                    sb.append(toUsers.get(i)).append(",");
                }
                list.add(sb.toString());
            }
        }

    }


    public static <T> void writeToFile(Collection<T> list, String fileName)
        throws ExecutionException, InterruptedException {
        try {
            File outFile = new File(fileName);
            Writer out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outFile, true), StandardCharsets.UTF_8), 10240);
            for (T t : list) {
                if (t instanceof String) {
                    out.write(t + "\r\n");
                }else {
                    out.write(JSONObject.toJSONString(t) + "\r\n");
                }
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
