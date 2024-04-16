package com.lakecat.web.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.lakecat.web.common.CommonParameters;
import com.lakecat.web.entity.CurrentUser;
import com.lakecat.web.entity.InfTraceContextHolder;
import com.lakecat.web.http.BaseResponse;
import com.lakecat.web.http.HttpUtil;
import com.lakecat.web.vo.blood.UserGroupVo;
import lombok.extern.slf4j.Slf4j;
import com.lakecat.web.vo.blood.Actor;
import com.lakecat.web.vo.blood.SourceRead;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class DSUtilForLakecat {


    public static final Integer cpu = 6;
    public static final String mem = "40G";
    public static final Integer instances = 300;

    @Value("${task.url}")
    private String taskUrl;

    private String taskSourceUrl;

    private String taskUserIdsByGroupIdsUrl;

    private String taskSearchUrl;

    private String taskTreeUrl;

    private String taskChildrenUrl;

    private String tasKTenantNameUrl;


    /*@Value("${task.resource.url}")*/
    private String tasKResourceUrl;

    private String userGroupUrl;

    @PostConstruct
    public void init(){
        taskUserIdsByGroupIdsUrl=taskUrl+"/ds_task/group/listUserIdsByGroupIds?groupIds=";
        taskSourceUrl=taskUrl+"/ds_task/actor/sources/all";
        taskSearchUrl=taskUrl+"/ds_task/task/list?outputGuids=";
        taskTreeUrl=taskUrl+"/ds_task/group/userTree?userId=";
        taskChildrenUrl=taskUrl+"/ds_task/group/getChildrenUser?userId=";
        tasKTenantNameUrl=taskUrl+"/ds_task/tenant/list";
        tasKResourceUrl=taskUrl+"/cluster-service/cloud/resource/search";
        userGroupUrl=taskUrl+"/ds_task/userGroup/selectAllUserGroup";
        log.info("TASKURL-->"+taskUrl);
    }

    private static Cache <String, String> cache = CacheBuilder.newBuilder()
            .maximumSize(10)
            .expireAfterWrite(24, TimeUnit.HOURS)
            .build();



    public List<SourceRead> getActorSource(){
        List<SourceRead> soruces= Lists.newArrayList();
        String authentication=InfTraceContextHolder.get().getAuthentication();
        HashMap <String, String> headers = new HashMap <>(2);
        headers.put("Authentication",authentication);
        BaseResponse<String> response=HttpUtil.get(taskSourceUrl, null, headers);
        if (response!=null&& response.getData()!=null){
            BaseResponse<String> baseResponse=GsonUtil.parse(response.getData(),BaseResponse.class);
            String a=GsonUtil.toJson(baseResponse.getData(),false);
            System.out.println(a);
            //获取组内成员
            return GsonUtil.parseFromJson(a,new TypeToken<List<SourceRead>>(){}.getType());
        }
        return soruces;
    }

    public JSONArray getUserTree(String userId) {
        HashMap <String, String> headers = new HashMap <>(2);
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String authentication = request.getHeader("Authentication");
        headers.put("Authentication", authentication);
        BaseResponse response = HttpUtil.get(taskTreeUrl + userId, null, headers);
        if (!"SUCCESS".equals(response.getCode())) {
            return null;
        }
        return JSON.parseObject(response.getData().toString()).getJSONArray("data");
    }

    public List<String> getUserIdsByGroupIds(String groupIds) {
        HashMap <String, String> headers = new HashMap <>(2);
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String authentication = request.getHeader("Authentication");
        headers.put("Authentication", authentication);
        BaseResponse<String> response = HttpUtil.get(taskUserIdsByGroupIdsUrl + groupIds, null, headers);
        if (!"SUCCESS".equals(response.getCode())) {
            return null;
        }
        if (response!=null&& response.getData()!=null){
            BaseResponse<String> baseResponse=GsonUtil.parse(response.getData(),BaseResponse.class);
            String a=GsonUtil.toJson(baseResponse.getData(),false);
            //获取组内成员
            return GsonUtil.parseFromJson(a,new TypeToken<List<String>>(){}.getType());
        }
        return Lists.newArrayList();
    }


    public List<UserGroupVo> getAllUserGroup() {
        HashMap <String, String> headers = new HashMap <>(2);
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String authentication = request.getHeader("Authentication");
        headers.put("Authentication", authentication);
        BaseResponse<String> response = HttpUtil.get(userGroupUrl , null, headers);
        if (!"SUCCESS".equals(response.getCode())) {
            return null;
        }
        if (response!=null&& response.getData()!=null){
            BaseResponse<String> baseResponse=GsonUtil.parse(response.getData(),BaseResponse.class);
            String a=GsonUtil.toJson(baseResponse.getData(),false);
            //获取组内成员
            return GsonUtil.parseFromJson(a,new TypeToken<List<UserGroupVo>>(){}.getType());
        }
        return Lists.newArrayList();
    }

    public JSONArray getUserChildren(String userId) {

        JSONArray result = new JSONArray();
        result.add(userId);
        try {
            HashMap <String, String> headers = new HashMap <>(2);
            HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            String authentication = request.getHeader("Authentication");
            headers.put("Authentication", authentication);
            BaseResponse response = HttpUtil.get(taskChildrenUrl + userId, null, headers);
            if (!"SUCCESS".equals(response.getCode())) {
                return result;
            }
            return JSON.parseObject(response.getData().toString()).getJSONArray("data");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public JSONObject getOneTableName(String tableName, String dbName, String region, String tenantName) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        //生成当前参数
        HashMap <String, String> headers = new HashMap <>(2);
        String authentication = request.getHeader("Authentication");
        headers.put("Authentication", authentication);
        String key = dbName + "." + tableName + "@" + region;

        BaseResponse response = null;
        try {
            response = HttpUtil.get(taskSearchUrl + key, null, headers);
        } catch (Exception e) {
            log.error("",e);
            return null;
        }
        System.out.println("请求是否连通" + response);
        if (!"SUCCESS".equals(response.getCode())) {
            return null;
        }
        JSONArray data = JSON.parseObject(response.getData().toString()).getJSONArray("data");
        if (data == null || data.isEmpty()) {
            return null;
        }
        JSONArray tasks = JSON.parseObject(response.getData().toString()).getJSONArray("data");
        JSONObject results = new JSONObject();
        for (int i = 0; i < tasks.size(); i++) {
            JSONObject task = tasks.getJSONObject(i);
            String outputGuids = task.getString("outputGuids");
            if (outputGuids.equalsIgnoreCase(key)) {
                results.put("key", key);
                results.put("name", task.getString("name"));
                results.put("id", task.getLong("id"));
            }
        }
        return results;
    }

    public List <String> getAllTenantName() {
        HashMap <String, String> headers = new HashMap <>(2);
        BaseResponse response = null;
        try {
            response = HttpUtil.get(tasKTenantNameUrl, null, headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("执行结果为:" + response);
        JSONArray data = response.get().getJSONArray("data");
        List <String> tenantNames = new ArrayList <>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            tenantNames.add(jsonObject.getString("name"));
        }
        return tenantNames;
    }

    public List <CurrentUser> getAllTenant() {
        HashMap <String, String> headers = new HashMap <>(2);
        BaseResponse response = null;
        try {
            response = HttpUtil.get(tasKTenantNameUrl, null, headers);
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println("执行结果为:" + response);
        JSONArray data = response.get().getJSONArray("data");
        List <CurrentUser> tenantNames = new ArrayList <>();
        for (int i = 0; i < data.size(); i++) {
            CurrentUser currentUser=new CurrentUser();
            JSONObject jsonObject = data.getJSONObject(i);
            currentUser.setTenantId(jsonObject.getInteger("id"));
            currentUser.setTenantName(jsonObject.getString("name"));
            tenantNames.add(currentUser);
        }
        return tenantNames;
    }

    public List<String> getRegions(){
        HashMap <String, String> headers = new HashMap <>(2);
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        headers.put("tenantId", tenantId.toString());
        BaseResponse response = null;
        try {
            response = HttpUtil.get(tasKResourceUrl, null, headers);
        } catch (Exception e) {

            e.printStackTrace();
        }
        if (!"SUCCESS".equals(response.getCode())) {
            return null;
        }
        JSONObject data = response.get().getJSONObject("data");
        if (data == null || data.isEmpty()) {
            return null;
        }
        List<String> regions= Lists.newArrayList();
        JSONArray list = data.getJSONArray("list");
        for (int i = 0; i < list.size(); i++) {
            JSONObject jsonObject = list.getJSONObject(i);
            regions.add(jsonObject.getString("regionAlias"));
        }
        return regions;
    }

    public String getResource(String authentication) {
        HashMap <String, String> headers = new HashMap <>(2);
        headers.put("Authentication", authentication);
        BaseResponse response = null;
        try {
            response = HttpUtil.get(tasKResourceUrl, null, headers);
        } catch (Exception e) {

            e.printStackTrace();
        }
        if (!"SUCCESS".equals(response.getCode())) {
            return null;
        }
        JSONObject data = response.get().getJSONObject("data");
        if (data == null || data.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        JSONArray list = data.getJSONArray("list");
        for (int i = 0; i < list.size(); i++) {
            JSONObject jsonObject = list.getJSONObject(i);
            String tenantName = InfTraceContextHolder.get().getTenantName();
            String description = jsonObject.getString("description");
            String name = jsonObject.getString("regionAlias");
            String catalogName="";
            if (InfTraceContextHolder.gcp()){
                catalogName =  name;// 这里需要确认 原来写死的shareit+"_"+
            }else {
                catalogName="shareit_"+name;
            }
            String storage = jsonObject.getString("storage");
            String key = description + "#" + name + "#" + catalogName + "#" + storage;
            sb.append(key).append(",\n");
        }
        return sb.toString();
    }


    //组装配置项
    public static String assembleArgs(String executorCores, String executorMemory, String instances) {

        StringBuilder sb = new StringBuilder();
        if (executorCores != null) {
            sb.append("--conf ").append("spark.executor.cores=").append(executorCores).append(" \n");
        }
        if (executorMemory != null) {
            sb.append("--conf ").append("spark.executor.memory=").append(executorMemory).append(" \n");
        }
        if (instances != null) {
            sb.append("--conf ").append("spark.executor.instances=").append(instances).append(" \n");
        }
        return sb.toString();
    }


    public static String fromArgs(String argString, String recommendConf) {

        String[] args = argString.split("\\\n");
        List <String> list = Arrays.asList(args);
        List <String> finalList = new LinkedList <>(list);
        for (String arg : args) {
            String trim = arg.trim();
            if (trim.contains("executor")) {
                finalList.remove(arg);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String key : finalList) {
            sb.append(key).append(" \n");
        }
        sb.append(recommendConf);
        return sb.toString();
    }


    public static JSONObject fromArgs(String argString) {
        String[] args = argString.split("\\\n");
        List <String> list = Arrays.asList(args);
        List <String> finalList = new LinkedList <>(list);
        final Map <String, Object> map = new HashMap <>(args.length / 2);
        int i = 0;
        while (i < args.length) {
            final String key;

            if (args[i].startsWith("--conf")) {
                key = args[i].substring(7);
                if (key.contains("executor")) {
                    finalList.remove(args[i]);
                }
            } else if (args[i].startsWith("--file")) {
                i++;
                continue;
            } else if (args[i].startsWith("--jars")) {
                i++;
                continue;
            } else if (args[i].startsWith("--")) {
                key = args[i].substring(2);
                if (key.contains("executor")) {
                    finalList.remove(args[i]);
                }
            } else {
                i++;
                continue;
            }
            if (key.contains("=")) {
                String[] keys = key.split("=");
                map.put(keys[0], keys[1].trim());
            } else {
                String[] keys = key.split(" ");
                map.put(keys[0], keys[1].trim());
            }

            i++;
        }
        return new JSONObject(map);
    }

    public static JSONObject fromArgsForSearch(String argString) {
        String[] args = argString.split("\\\n");
        List <String> list = Arrays.asList(args);
        final Map <String, Object> map = new HashMap <>(args.length / 2);
        int i = 0;
        while (i < args.length) {
            final String key;

            String trim = args[i].trim();
            if (trim.startsWith("--conf")) {
                key = trim.substring(7);
            } else if (trim.startsWith("--file")) {
                i++;
                continue;
            } else if (trim.startsWith("--jars")) {
                i++;
                continue;
            } else if (trim.startsWith("--")) {
                key = trim.substring(2);
            } else {
                i++;
                continue;
            }
            if (key.contains("=")) {
                String[] keys = key.split("=");
                map.put(keys[0].trim(), keys[1].trim());
            } else {
                String[] keys = key.split(" ");
                map.put(keys[0].trim(), keys[1].trim());
            }
            i++;
        }
        return new JSONObject(map);
    }


    public static void main(String[] args) {

        String conf = "--jars s3://shareit.deploy.us-east-1/BDP/BDP-lbs/lbs-udf/ads_base-1.0.0.jar\n" +
                "--conf spark.executor.cores=2\n" +
                "--conf spark.executor.memory=4G \n" +
                "--conf spark.executor.memoryOverhead=5G \n" +
                "--conf spark.sql.shuffle.partitions=500 \n" +
                "--conf spark.sql.broadcastTimeout=60000 \n" +
                "--conf spark.executor.instances=2\n" +
                "--conf spark.dynamicAllocation.enabled=false";

        String s = assembleArgs("3", "2G", "1800");
        String s1 = fromArgs(conf, s);


    }

}
