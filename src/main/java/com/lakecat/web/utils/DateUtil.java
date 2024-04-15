package com.lakecat.web.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class DateUtil {

    static SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static LocalDateTime getLocalDateTime(String time) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(time, dateTimeFormatter);
    }

    // 将时间戳转成字符串
    public static String getDateToString(long time) {
        Date d = new Date(time);
        return yyyyMMddHHmmss.format(d);
    }


    // 将时间戳转成字符串
    public static String getDateToStringNow() {
        Date d = new Date();
        return yyyyMMddHHmmss.format(d);
    }


    public static String getCurrentDateStrForBlood() {
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault()));
    }

    public static String getCurrentDateStr() {
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).withZone(ZoneId.systemDefault()));
    }

    public static String date2yyyymmddHHmmss(int dateTime){

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, dateTime);
        Date time = calendar.getTime();
        return yyyyMMddHHmmss.format(time);
    }


    public static void get(){

        DateFormat format=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            System.out.println(format.parse("2022-06-30T05:30:41.375GMT").toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        get();
    }
    
    
}
