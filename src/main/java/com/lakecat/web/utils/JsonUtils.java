package com.lakecat.web.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version V1.0
 * @Description：JSON工具类
 */
public class JsonUtils {

    /**数据更新标识*/
    public static boolean flag = false;

    /****
     * @Description :根据json字段名称查找对应值
     * @param json
     * @param param
     * @return java.lang.String
     */
    public static String getJsonParam(String json, String param) {
        String regex = param + "\":(.*?)(,|})";//
        Matcher matcher = Pattern.compile(regex).matcher(json);
        String returnStr = null;
        while (matcher.find()) {
            String ret = matcher.group(1);
            returnStr = ret;
        }
        if (returnStr == null) {
            return null;
        } else {
            return returnStr.replaceAll("\"", "").replaceAll("}", "").replaceAll("]", "");
        }

    }

    /***
     * @Description :自定义获取节点数据
     * @param objJson
     * @param node
     * @return java.lang.Object
     */
    public static Object getNodeJson(Object objJson, String node) {
        if (objJson instanceof JSONArray) {
            JSONArray objArray = (JSONArray) objJson;
            for (int i = 0; i < objArray.size(); i++) {
                getNodeJson(objArray.get(i), node);
            }
        } else if (objJson instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) objJson;
            Iterator it = jsonObject.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next().toString();
                Object object = jsonObject.get(key);
                if (key.equals(node)) {
                    objJson = object;
                    break;
                }
                if (object instanceof JSONArray) {
                    JSONArray objArray = (JSONArray) object;
                    getNodeJson(objArray, node);
                } else if (object instanceof JSONObject) {
                    getNodeJson(object, node);
                }
            }
        }
        return objJson;
    }

    /***
     * @Description :更新JSON数据
     * @param objJson
     * @param nodeKey
     * @param nodeValue
     * @return java.lang.Object
     */
    public static Object updateJson(Object objJson, String nodeKey, String nodeValue) {
        //如果obj为json数组
        if (objJson instanceof JSONArray) {
            JSONArray objArray = (JSONArray) objJson;
            for (int i = 0; i < objArray.size(); i++) {
                updateJson(objArray.get(i), nodeKey, nodeValue);
            }
        } else if (objJson instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) objJson;
            Iterator it = jsonObject.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next().toString();
                Object object = jsonObject.get(key);
                if (object instanceof JSONArray) {
                    JSONArray objArray = (JSONArray) object;
                    updateJson(objArray, nodeKey, nodeValue);
                } else if (object instanceof JSONObject) {
                    updateJson(object, nodeKey, nodeValue);
                } else {
                    if (key.equals(nodeKey)) {
                        //替换数据
                        jsonObject.put(key, nodeValue);
                    }
                }
            }
        }
        return objJson;
    }

    /***
     * @Description :更新节点下的JSON数据
     * @param objJson
     * @param node
     * @param nodeKey
     * @param nodeValue
     * @return java.lang.Object
     */
    public static Object updateNodeJson(Object objJson, String node, String nodeKey, String nodeValue) {
        //如果obj为json数组
        if (objJson instanceof JSONArray) {
            JSONArray objArray = (JSONArray) objJson;
            for (int i = 0; i < objArray.size(); i++) {
                updateNodeJson(objArray.get(i), node, nodeKey, nodeValue);
            }
        } else if (objJson instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) objJson;
            Iterator it = jsonObject.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next().toString();
                Object object = jsonObject.get(key);
                //判断key是否相等
                if (key.equals(node)) {
                    //修改对应的字段
                    setJsonValue(object, nodeKey, nodeValue);
                    flag = true;
                    break;
                } else {
                    if (object instanceof JSONArray) {
                        JSONArray objArray = (JSONArray) object;
                        updateNodeJson(objArray, node, nodeKey, nodeValue);
                    } else if (object instanceof JSONObject) {
                        updateNodeJson(object, node, nodeKey, nodeValue);
                    }
                }
            }
        }
        return objJson;
    }

    /***
     * @Description :修改对应的字段
     * @param obj
     * @param key
     * @param value
     * @return void
     */
    public static void setJsonValue(Object obj, String key, String value) {
        if (obj instanceof JSONArray) {
            JSONArray arr = (JSONArray) obj;
            for (int i = 0; i < arr.size(); i++) {
                JSONObject jsonObject = arr.getJSONObject(i);
                if (StringUtils.isNotEmpty(jsonObject.getString(key))) {
                    //替换数据
                    jsonObject.put(key, value);
                }
            }
        } else {
            JSONObject jsonObject = (JSONObject) obj;
            if (StringUtils.isNotEmpty(jsonObject.getString(key))) {
                //替换数据
                jsonObject.put(key, value);
            }
        }
    }
}
