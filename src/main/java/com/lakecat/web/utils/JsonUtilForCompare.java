package com.lakecat.web.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Carpoor
 * @date 2021年5月19日
 */
public class JsonUtilForCompare {

    public static Set<String> set = new HashSet<>();

    static {
        set.add("String");
        set.add("Integer");
        set.add("Double");
    }

    public static <T>T str2Obj(String json, Class<T> clazz) {
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .serializeNulls()
                .create();
        return gson.fromJson(json, clazz);
    }

    public static String obj2Str(Object obj) {
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .serializeNulls()
                .create();
        return gson.toJson(obj);
    }

    public static Map<String, String> jsonCompare(String currentJson, String expectJson) {
        Map<String, String> result = new HashMap<>();
        Map<String, Object> curr = str2Obj(currentJson, Map.class);
        Map<String, Object> exp = str2Obj(expectJson, Map.class);

        mapProc(curr, exp, result, "");

        return result;
    }

    public static void mapProc(Map<String, Object> curr, Map<String, Object> exp, Map<String, String> result, String keyStr) {

        if (curr.size() != exp.size()) {
            result.put(keyStr == "" ? "首层":keyStr, String.format("key=[%s]Map字段个数不一致，期望为[%s],但当前为[%s]", keyStr == "" ? "首层":keyStr, exp.size(), curr.size()));
        }

        for (String currKey : curr.keySet()) {
            String keyStrCopy = keyStr + "."+ currKey;
            if (keyStrCopy.indexOf(".") == 0) {
                keyStrCopy = keyStrCopy.substring(1);
            }
            Object currObj = curr.get(currKey);

            //当前为null，对比期望为null 和 不为null两种情况
            if (currObj == null) {
                Object expObj = exp.get(currKey);
                if (expObj != null) {
                    result.put(keyStrCopy, String.format("key=[%s]期望为[%s],但当前为[%s]", keyStrCopy, expObj, currObj));
                }
                continue;
            }

            //当前不为null，对比期望为null的情况
            Object expObj = exp.get(currKey);
            if (expObj == null) {
                result.put(keyStrCopy, String.format("key=[%s]期望为[%s],但当前为[%s]", keyStrCopy, expObj, currObj));
                continue;
            }

            //类型不一致
            if (!expObj.getClass().getSimpleName().equals(currObj.getClass().getSimpleName())) {
                result.put(keyStrCopy, String.format("key=[%s]类型不匹配，期望为[%s],但当前为[%s]", keyStrCopy, expObj.getClass().getSimpleName(), currObj.getClass().getSimpleName()));
                continue;
            }

            if (set.contains(currObj.getClass().getSimpleName())) {
                if (!expObj.equals(currObj)) {
                    result.put(keyStrCopy, String.format("key=[%s]期望为[%s],但当前为[%s]", keyStrCopy, expObj, currObj));
                }
            }
            else if (currObj instanceof List) {
                List currList = (List)currObj;

                List expList = (List)expObj;

                //key对应结果条数不一致
                if (currList.size() != expList.size()) {
                    result.put(keyStrCopy, String.format("key=[%s]List结果条数不一致，期望为[%s],但当前为[%s]", keyStrCopy, expList.size(), currList.size()));
                    continue;
                }

                for (int i = 0; i < currList.size(); i++) {
                    Object currListObj = currList.get(i);
                    Object expListObj = expList.get(i);

                    //List中泛型的类型不匹配
                    if (!currListObj.getClass().getSimpleName().equals(expListObj.getClass().getSimpleName())) {
                        result.put(keyStrCopy, String.format("key=[%s]List中泛型的类型不匹配，期望为[%s],但当前为[%s]", keyStrCopy,
                                expListObj.getClass().getSimpleName(), currListObj.getClass().getSimpleName()));
                        break;
                    }

                    if (currListObj instanceof Map) {
                        Map<String, Object> currListMap = (Map)currListObj;
                        Map<String, Object> expListMap = (Map)expListObj;

                        mapProc(currListMap, expListMap, result, keyStrCopy+"-"+ i +"");
                    }
                    else if (set.contains(currListObj.getClass().getSimpleName())) {
                        if (!expListObj.equals(currListObj)) {
                            result.put(keyStrCopy+"-"+i+"", String.format("key=[%s]期望为[%s],但当前为[%s]", keyStrCopy+"-"+ i +"", expListObj, currListObj));
                        }
                    }
                    else {
                        result.put(keyStrCopy+"-"+ i +"", String.format("key=[%s]List中泛型的类型为[%s]，目前未支持，期望为[%s],但当前为[%s]", keyStrCopy+"-"+ i +"", currListObj.getClass().getSimpleName(), expListObj, currListObj));
                    }
                }
            }
            else if (currObj instanceof Map) {
                Map<String, Object> currListMap = (Map)currObj;
                Map<String, Object> expListMap = (Map)expObj;

                mapProc(currListMap, expListMap, result, keyStrCopy);
            }
            else {
                result.put(keyStrCopy, String.format("key=[%s]类型为[%s]，目前未支持，期望为[%s],但当前为[%s]", keyStrCopy, currObj.getClass().getSimpleName(), expObj, currObj));
            }
        }

        //补 exp多出来的字段差异
        exp.forEach((key, value) -> {
//            if(){//curr中不包含key
//                //result写差异原因
//            }
        });
    }


    public static void main(String[] args) {
        Map<String, String> map = test01();
//		test02();
//        result.putAll(test03());
        for (String key : map.keySet()) {
            System.out.println(key + " <---> " + map.get(key));
        }
    }

    public static Map test03() {
        System.out.println("==================测试多层json-List==========================");
        Map<String, Object> exp = new HashMap<>();
        List<Map<String, Object>> expList = new ArrayList<>();
        List<Object> expListSimple = new ArrayList<>();
        Map<String, Object> exp01 = new HashMap<>();
        exp01.put("AA", "-aa");
        exp01.put("AB", 22);
        exp01.put("AC", 33);
        expList.add(exp01);
        Map<String, Object> exp02 = new HashMap<>();
        exp02.put("AA", "-aa");
        exp02.put("AB", 22);
        exp02.put("AC", 33);
        expList.add(exp02);

        expListSimple.add(2);
        expListSimple.add("3");
        expListSimple.add(true);

//		exp.put("", "");
        exp.put("A", "aa");
        exp.put("B", expList);
        exp.put("C", expList);
        exp.put("D", expListSimple);

        Map<String, Object> curr = new HashMap<>();
        List<Map<String, Object>> currList = new ArrayList<>();
        List<Object> currListSimple = new ArrayList<>();
        Map<String, Object> curr01 = new HashMap<>();
        curr01.put("AB", 22);
        curr01.put("AC", 22);
        currList.add(curr01);
        Map<String, Object> curr02 = new HashMap<>();
        curr02.put("AA", "-aa");
        curr02.put("AB", 22);
        curr02.put("AC", 33);
        currList.add(curr02);

        currListSimple.add(2);
        currListSimple.add("3");
        currListSimple.add(true);

//		curr.put("", "");
        curr.put("B", currList);
        curr.put("C", new ArrayList<>());
        curr.put("D", currListSimple);

        String expectJson = obj2Str(exp);
        String currentJson = obj2Str(curr);
        Map jsonCompare = jsonCompare(currentJson, expectJson);

        return jsonCompare;
//		System.out.println(obj2Str(jsonCompare));
    }

    public static Map test02() {
        System.out.println("==================测试多层json-Map==========================");
        Map<String, Object> exp = new HashMap<>();
        Map<String, Object> exp01 = new HashMap<>();
        exp01.put("AA", "-aa");
        exp01.put("AB", 22);
        exp01.put("AC", 33);

//		exp.put("", "");
        exp.put("A", "aa");
        exp.put("B", exp01);
        exp.put("C", exp01);

        Map<String, Object> curr = new HashMap<>();
        Map<String, Object> curr01 = new HashMap<>();
        curr01.put("AB", 22);
        curr01.put("AC", 22);



//		curr.put("", "");
        curr.put("B", curr01);
        curr.put("C", new ArrayList<>());

        String expectJson = obj2Str(exp);
        String currentJson = obj2Str(curr);
        Map jsonCompare = jsonCompare(currentJson, expectJson);

        return jsonCompare;
//		System.out.println(obj2Str(jsonCompare));
    }

    public static Map<String, String> test01() {
        System.out.println("==================测试单层json==========================");
        Map<String, Object> exp = new HashMap<>();
//		exp.put("", "");
        exp.put("A", "aa");
        exp.put("B", "ba");
        exp.put("C", 2);
        exp.put("D", null);
        exp.put("E", null);
        exp.put("F", 2);

        Map<String, Object> curr = new HashMap<>();
//		curr.put("", "");
        curr.put("B", "bb");
        curr.put("C", "2");
        curr.put("D", "2");
        curr.put("E", null);
        curr.put("F", null);

        String expectJson = obj2Str(exp);
        String currentJson = obj2Str(curr);
        Map<String, String> jsonCompare = jsonCompare(currentJson, expectJson);

        return jsonCompare;
//		System.out.println(obj2Str(jsonCompare));
    }
}


