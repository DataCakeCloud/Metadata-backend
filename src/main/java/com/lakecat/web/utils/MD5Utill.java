package com.lakecat.web.utils;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by slj on 2022/5/26.
 */
public class MD5Utill {

    public static String md5(String str) {
        if (str == null) {
            return "";
        }
        return DigestUtils.md5Hex(str);

    }

    public static String getPartitonNames(String name) {
        StringBuffer stringBuffer = new StringBuffer();
        if(name.contains("/")){
            String[] split = name.split("\\/");
            for (int i = 0; i < split.length; i++) {
                String split2 = split[i];
                String partitonName = split2.split("=")[0];
                stringBuffer.append(partitonName);
                stringBuffer.append(",");
            }
        }else{
            String partitonName = name.split("=")[0];
            stringBuffer.append(partitonName);
            stringBuffer.append(",");
        }

        return stringBuffer.substring(0,stringBuffer.length()-1);
    }

}
