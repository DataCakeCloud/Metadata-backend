package com.lakecat.web.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import com.alibaba.fastjson.JSONObject;

public class FileUtils {


    public static <T> void writeToFile(Collection<T> list, String fileName)
        throws ExecutionException, InterruptedException {
        try {
            File outFile = new File(fileName);
            Writer out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outFile, true), StandardCharsets.UTF_8), 10240);
            for (T t : list) {
                if (t instanceof String) {
                    out.write(t + "\r\n");
                } else {
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
