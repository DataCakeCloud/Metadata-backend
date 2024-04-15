package com.lakecat.web.excel;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PubMethod {


    public static <T> Map<String,List<T>> listToListMap(String key,List<T> list){
        Map<String,List<T>> map=Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(list)){
            Map<String,PropertyDescriptor> propertyDescriptorMap=classToMap(list.get(0));
            PropertyDescriptor p=propertyDescriptorMap.get(key);
            for (T t:list){
                try {
                    String properties=(String)( p.getReadMethod().invoke(t));
                    if (map.containsKey(properties)){
                        map.get(properties).add(t);
                    }else {
                        map.put(properties,Lists.newArrayList(t));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    public static String convertSqlWithList(String sql,List<String> ids){
        Set<String> set= Sets.newHashSet();
        ids.forEach(s -> {
            set.add(s);
        });
        ids=Lists.newArrayList(set);
        String a="("+Joiner.on(",").join(ids)+")";
        sql=String.format(sql,a);
        return sql;
    }

    public static String joinList(List<String> strings){
        return Joiner.on(",").join(strings);
    }

    public static String joinList(Set<String> strings){
        return Joiner.on(",").join(strings);
    }

    public  static <T> List<List<T>> subList(List<T> list, int num){
        List<List<T>> listList=Lists.newArrayList();
        if (CollectionUtils.isEmpty(list)){
            return listList;
        }
        if (num<2){
            listList.add(list);
            return listList;
        }
        if (list.size()<=num){
            listList.add(list);
            return listList;
        }
        for (int i=0;i<list.size();i=i+num){
            if (i+num<list.size()){
                listList.add(list.subList(i,i+num));
            }else {
                listList.add(list.subList(i,list.size()));
            }
        }
        return listList;
    }


    public static String pkg(String preview,String platform){
        String result="";
        try {
            if ("android".equalsIgnoreCase(platform)){
                int index=preview.indexOf("id=");
                if (index>-1){
                    result=preview.substring(index+3);
                    int index2=result.indexOf("&");
                    if (index2>-1){
                        result=result.substring(0,index2);
                    }
                }
            }
            if ("ios".equalsIgnoreCase(platform)){
                int index=preview.indexOf("id");
                if (index>-1){
                    result=preview.substring(index);
                    int index2=result.indexOf("?");
                    if (index2>-1){
                        result=result.substring(0,index2);
                    }
                }
            }
        }catch (Exception e){

        }

        return result;
    }

    public static <T> Map<String, PropertyDescriptor> classToMap(T t){
        Map<String,PropertyDescriptor> stringPropertyDescriptorMap = Maps.newHashMap();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(t.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();
                if (!key.equals("class")){
                    stringPropertyDescriptorMap.put(key,property);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return stringPropertyDescriptorMap;
    }
    public static <T> void convertNull(T t){
        Map<String, PropertyDescriptor> propertyDescriptorMap=classToMap(t);
        try {
            for (Map.Entry<String, PropertyDescriptor>entry:propertyDescriptorMap.entrySet()){
                if (entry.getValue().getPropertyType()==String.class){
                    String s=(String) (entry.getValue().getReadMethod().invoke(t,null));
                    if (StringUtils.isBlank(s)){
                        entry.getValue().getWriteMethod().invoke(t,"");
                    }
                }
            }
        }catch (Exception e){
            System.out.println(e);
        }

    }

    public static <T> void printSql(T t){
        List<String> treeSet= Lists.newArrayList(Sets.newTreeSet(PubMethod.classToMap(t).keySet()));
        StringBuffer stringBuffer=new StringBuffer("INSERT into ").append(t.getClass().getName()).append("(");
        StringBuffer buffer2=new StringBuffer("(");
        for (String s:treeSet){
            buffer2.append("#{ad.").append(s).append("},");
            for(int i = 0; i < s.length(); ++i) {
                if (Character.isUpperCase(s.charAt(i))) {
                    stringBuffer.append("_").append(s.substring(i,i+1).toLowerCase());
                }else {
                    stringBuffer.append(s.substring(i,i+1));
                }
            }
            stringBuffer.append(",");
        }
        buffer2.append(")");
        stringBuffer.append(") VALUES <foreach collection=\"list\" item=\"ad\" index=\"index\" separator=\",\"> ");
        stringBuffer.append(buffer2).append("</foreach>");
        System.out.println(stringBuffer.toString());
    }
    public static <T> void printTitle(T t){
        List<String> treeSet= Lists.newArrayList(Sets.newTreeSet(PubMethod.classToMap(t).keySet()));
        StringBuffer title=new StringBuffer("<sql id=\"Base_Column\">");
        System.out.println(title.toString());
        StringBuffer stringBuffer=new StringBuffer("");
        for (String s:treeSet){
            for(int i = 0; i < s.length(); ++i) {
                if (Character.isUpperCase(s.charAt(i))) {
                    stringBuffer.append("_").append(s.substring(i,i+1).toLowerCase());
                }else {
                    stringBuffer.append(s.substring(i,i+1));
                }
            }
            stringBuffer.append("").append(",");
        }
        System.out.println(stringBuffer.toString());
        System.out.println("</sql>");
    }

    public static <T> void printPropetiesSql(T t){
        List<String> treeSet= Lists.newArrayList(Sets.newTreeSet(PubMethod.classToMap(t).keySet()));
        StringBuffer title=new StringBuffer("<resultMap id=\"BaseResultMap\" type=\"").append(t.getClass().getName()).append("\">");
        System.out.println(title.toString());
        for (String s:treeSet){
            StringBuffer stringBuffer=new StringBuffer("<result column=\"");
            for(int i = 0; i < s.length(); ++i) {
                if (Character.isUpperCase(s.charAt(i))) {
                    stringBuffer.append("_").append(s.substring(i,i+1).toLowerCase());
                }else {
                    stringBuffer.append(s.substring(i,i+1));
                }
            }
            stringBuffer.append("\"  property=\"").append(s);
            stringBuffer.append("\" />");
            System.out.println(stringBuffer.toString());
        }
        System.out.println("</resultMap>");
    }

    public static String resourceId(String resourceName){
        return resourceName.substring(resourceName.lastIndexOf("/")+1);
    }
    public static String httpurlquery(Map<String,String> map){
        List<String> params=Lists.newArrayList();
        for (Map.Entry<String,String> entry:map.entrySet()){
            params.add(entry.getKey()+"="+ URLEncoder.encode(entry.getValue()));
        }
        return Joiner.on("&").join(params);
    }
}
