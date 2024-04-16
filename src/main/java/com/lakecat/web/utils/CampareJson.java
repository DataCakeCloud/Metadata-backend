package com.lakecat.web.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.lakecat.web.utils.JsonUtilForCompare.jsonCompare;
import static com.lakecat.web.utils.JsonUtilForCompare.obj2Str;

@Component
public class CampareJson {
    public String campareJsonObject(String oldJsonStr, String newJsonStr1) {
        //将字符串转换为json对象
        JSON oldJson = JSON.parseObject(oldJsonStr);
        JSON newJson = JSON.parseObject(newJsonStr1);
        //递归遍历json对象所有的key-value，将其封装成path:value格式进行比较
        Map <String, Object> oldMap = new LinkedHashMap <>();
        Map <String, Object> newMap = new LinkedHashMap <>();
        convertJsonToMap(oldJson, "", oldMap);
        convertJsonToMap(newJson, "", newMap);
        Map <String, Object> differenceMap = campareMap(oldMap, newMap);
        //将最终的比较结果把不相同的转换为json对象返回
        String jsonObject = convertMapToJson(differenceMap);
        return jsonObject;
    }

    /**
     * 将json数据转换为map存储用于比较
     *
     * @param json
     * @param root
     * @param resultMap
     */
    private void convertJsonToMap(Object json, String root, Map <String, Object> resultMap) {
        if (json instanceof JSONObject) {
            JSONObject jsonObject = ((JSONObject) json);
            Iterator iterator = jsonObject.keySet().iterator();
            while (iterator.hasNext()) {
                Object key = iterator.next();
                Object value = jsonObject.get(key);
                String newRoot = "".equals(root) ? key + "" : root + "." + key;
                if (value instanceof JSONObject || value instanceof JSONArray) {
                    convertJsonToMap(value, newRoot, resultMap);
                } else {
                    resultMap.put(newRoot, value);
                }
            }
        } else if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;
            for (int i = 0; i < jsonArray.size(); i++) {
                Object vaule = jsonArray.get(i);
                String newRoot = "".equals(root) ? "[" + i + "]" : root + ".[" + i + "]";
                if (vaule instanceof JSONObject || vaule instanceof JSONArray) {
                    convertJsonToMap(vaule, newRoot, resultMap);
                } else {
                    resultMap.put(newRoot, vaule);
                }
            }
        }
    }

    /**
     * 比较两个map，返回不同数据
     *
     * @param oldMap
     * @param newMap
     * @return
     */
    private Map <String, Object> campareMap(Map <String, Object> oldMap, Map <String, Object> newMap) {
        //遍历newMap，将newMap的不同数据装进oldMap，同时删除oldMap中与newMap相同的数据
        campareNewToOld(oldMap, newMap);
        //將舊的有新的沒有的數據封裝數據結構存在舊的裡面
        campareOldToNew(oldMap);
        return oldMap;
    }

    /**
     * 將舊的有新的沒有的數據封裝數據結構存在舊的裡面
     *
     * @param oldMap
     * @return
     */
    private void campareOldToNew(Map <String, Object> oldMap) {
        //统一oldMap中newMap不存在的数据的数据结构，便于解析
        for (Iterator <Map.Entry <String, Object>> it = oldMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry <String, Object> item = it.next();
            String key = item.getKey();
            Object value = item.getValue();
            int lastIndex = key.lastIndexOf(".");
            if (!(value instanceof Map)) {
                Map <String, Object> differenceMap = new HashMap <>();
                differenceMap.put("oldValue", value);
                differenceMap.put("newValue", "");
                oldMap.put(key, differenceMap);
            }
        }
    }

    /**
     * 將新的map與舊的比較，並將數據統一存在舊的裡面
     *
     * @param oldMap
     * @param newMap
     */
    private void campareNewToOld(Map <String, Object> oldMap, Map <String, Object> newMap) {
        for (Iterator <Map.Entry <String, Object>> it = newMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry <String, Object> item = it.next();
            String key = item.getKey();
            Object newValue = item.getValue();
            Map <String, Object> differenceMap = new HashMap <>();
            int lastIndex = key.lastIndexOf(".");
            String lastPath = key.substring(lastIndex + 1).toLowerCase();
            if (oldMap.containsKey(key)) {
                Object oldValue = oldMap.get(key);
                if (newValue.equals(oldValue)) {
                    oldMap.remove(key);
                    continue;
                } else {
                    differenceMap.put("oldValue", oldValue);
                    differenceMap.put("newValue", newValue);
                    oldMap.put(key, differenceMap);
                }
            } else {
                differenceMap.put("oldValue", "");
                differenceMap.put("newValue", newValue);
                oldMap.put(key, differenceMap);
            }
        }
    }

    /**
     * 将已经找出不同数据的map根据key的层级结构封装成json返回
     *
     * @param map
     * @return
     */
    private String convertMapToJson(Map <String, Object> map) {
        JSONObject resultJSONObject = new JSONObject();
        for (Iterator <Map.Entry <String, Object>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry <String, Object> item = it.next();
            String key = item.getKey();
            Object value = item.getValue();
            String[] paths = key.split("\\.");
            int i = 0;
            Object remarkObject = null;//用於深度標識對象
            int indexAll = paths.length - 1;
            while (i <= paths.length - 1) {
                String path = paths[i];
                if (i == 0) {
                    //初始化对象标识
                    if (resultJSONObject.containsKey(path)) {
                        remarkObject = resultJSONObject.get(path);
                    } else {
                        if (indexAll > i) {
                            if (paths[i + 1].matches("\\[[0-9]+\\]")) {
                                remarkObject = new JSONArray();
                            } else {
                                remarkObject = new JSONObject();
                            }
                            resultJSONObject.put(path, remarkObject);
                        } else {
                            resultJSONObject.put(path, value);
                        }
                    }
                    i++;
                    continue;
                }
                if (path.matches("\\[[0-9]+\\]")) {//匹配集合对象
                    int startIndex = path.lastIndexOf("[");
                    int endIndext = path.lastIndexOf("]");
                    int index = Integer.parseInt(path.substring(startIndex + 1, endIndext));
                    if (indexAll > i) {
                        if (paths[i + 1].matches("\\[[0-9]+\\]")) {
                            while (((JSONArray) remarkObject).size() <= index) {
                                if (((JSONArray) remarkObject).size() == index) {
                                    ((JSONArray) remarkObject).add(index, new JSONArray());
                                } else {
                                    ((JSONArray) remarkObject).add(null);
                                }
                            }
                        } else {
                            while (((JSONArray) remarkObject).size() <= index) {
                                if (((JSONArray) remarkObject).size() == index) {
                                    ((JSONArray) remarkObject).add(index, new JSONObject());
                                } else {
                                    ((JSONArray) remarkObject).add(null);
                                }
                            }
                        }
                        remarkObject = ((JSONArray) remarkObject).get(index);
                    } else {
                        while (((JSONArray) remarkObject).size() <= index) {
                            if (((JSONArray) remarkObject).size() == index) {
                                ((JSONArray) remarkObject).add(index, value);
                            } else {
                                ((JSONArray) remarkObject).add(null);
                            }
                        }
                    }
                } else {
                    if (indexAll > i) {
                        if (paths[i + 1].matches("\\[[0-9]+\\]")) {
                            if (!((JSONObject) remarkObject).containsKey(path)) {
                                ((JSONObject) remarkObject).put(path, new JSONArray());
                            }
                        } else {
                            if (!((JSONObject) remarkObject).containsKey(path)) {
                                ((JSONObject) remarkObject).put(path, new JSONObject());
                            }
                        }
                        remarkObject = ((JSONObject) remarkObject).get(path);
                    } else {
                        ((JSONObject) remarkObject).put(path, value);
                    }
                }
                i++;
            }
        }
        return JSON.toJSONString(resultJSONObject);
    }

    public static void main(String[] args) {
        String oldStr = "[\n" +
                "    {\n" +
                "        \"name\":\"exp_id\",\n" +
                "        \"dataGrade\":\"3级\",\n" +
                "        \"comment\":\"exp_id\",\n" +
                "        \"id\":\"795a9543-ebac-49c2-bd85-99b3dba0116d\",\n" +
                "        \"logic\":\"去表id\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"group_id\",\n" +
                "        \"dataGrade\":\"2级\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"16899321-2cb5-4b91-92ee-7dceaeedbe28\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"int\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"compare_group_id\",\n" +
                "        \"dataGrade\":\"3级\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"a4126709-bb5e-4315-9647-03017865d213\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"int\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"start_time\",\n" +
                "        \"dataGrade\":\"1级\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"53234469-c282-4d80-81bc-9bb841e69c72\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"end_time\",\n" +
                "        \"dataGrade\":\"1级\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"c9d4455d-ce28-4b4e-a7c4-1baccf4a723a\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"record_hour\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"96317cbe-2916-4676-8b26-ee023fa7a6bd\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"int\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"time_granularity\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"68e92374-5fe3-4c4e-b231-3465bf506909\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"indicator_group_id\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"11bbc3b9-e485-40b3-b6f0-f9d94b802d8e\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"int\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"group_num\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"c9dd853e-6159-4617-90c3-ff5809def69e\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"BIGINT\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"indicator_name\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"97019173-49ed-46d8-be48-223f388f8e13\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"indicator_value\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"25c03548-ccd0-4d83-a351-945423785924\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"country\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"707c4e62-6340-4fa1-a64d-2c5cbeefbb6f\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"user_type\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"3baf955b-60ef-459f-835c-081b9260d91b\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"app_ver\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"7b6d402c-6816-4403-aeb2-1a59f157cf76\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"BIGINT\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"exp_analysis_id\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"22464034-ff26-4af5-bbed-0a812bdcf930\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"create_time\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"6fd6d0e3-5e29-444f-801e-5719860ad8c2\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    }\n" +
                "]";

        String newStr = "[\n" +
                "    {\n" +
                "        \"name\":\"exp_id\",\n" +
                "        \"dataGrade\":\"5级\",\n" +
                "        \"comment\":\"exp_id1\",\n" +
                "        \"id\":\"795a9543-ebac-49c2-bd85-99b3dba0116d\",\n" +
                "        \"logic\":\"去表id111\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"group_id\",\n" +
                "        \"dataGrade\":\"2级\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"16899321-2cb5-4b91-92ee-7dceaeedbe28\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"int\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"compare_group_id\",\n" +
                "        \"dataGrade\":\"3级\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"a4126709-bb5e-4315-9647-03017865d213\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"int\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"start_time\",\n" +
                "        \"dataGrade\":\"1级\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"53234469-c282-4d80-81bc-9bb841e69c72\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"end_time\",\n" +
                "        \"dataGrade\":\"1级\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"c9d4455d-ce28-4b4e-a7c4-1baccf4a723a\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"record_hour\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"96317cbe-2916-4676-8b26-ee023fa7a6bd\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"int\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"time_granularity\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"68e92374-5fe3-4c4e-b231-3465bf506909\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"indicator_group_id\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"11bbc3b9-e485-40b3-b6f0-f9d94b802d8e\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"int\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"group_num\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"c9dd853e-6159-4617-90c3-ff5809def69e\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"BIGINT\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"indicator_name\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"97019173-49ed-46d8-be48-223f388f8e13\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"indicator_value\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"25c03548-ccd0-4d83-a351-945423785924\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"country\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"707c4e62-6340-4fa1-a64d-2c5cbeefbb6f\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"user_type\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"3baf955b-60ef-459f-835c-081b9260d91b\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"app_ver\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"7b6d402c-6816-4403-aeb2-1a59f157cf76\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"BIGINT\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"exp_analysis_id\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"22464034-ff26-4af5-bbed-0a812bdcf930\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"create_time\",\n" +
                "        \"dataGrade\":\"\",\n" +
                "        \"comment\":\"from deserializer\",\n" +
                "        \"id\":\"6fd6d0e3-5e29-444f-801e-5719860ad8c2\",\n" +
                "        \"logic\":\"\",\n" +
                "        \"type\":\"STRING\"\n" +
                "    }\n" +
                "]";

        List <JSONObject> jsonArrayNewList = JSON.parseArray(oldStr, JSONObject.class);
        List <JSONObject> jsonArrayOldList = JSON.parseArray(newStr, JSONObject.class);
        if (jsonArrayNewList.size() != jsonArrayOldList.size()) {
            return;
        }
        for (int i = 0; i < jsonArrayNewList.size(); i++) {
            JSONObject newStrJson = jsonArrayNewList.get(i);
            JSONObject oldStrJson = jsonArrayOldList.get(i);
            String newName = newStrJson.getString("name");
            String oldName = oldStrJson.getString("name");
            if (!newName.equals(oldName)) {
                continue;
            }
            String s = new CampareJson().campareJsonObject(oldStrJson.toJSONString(), newStrJson.toJSONString());
            JSONObject result = JSONObject.parseObject(s);

            if (result.size() == 0) {
                continue;
            }
            result.put("name",newName);
            System.out.println(result.toJSONString());
        }
    }
}

