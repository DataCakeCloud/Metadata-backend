package com.lakecat.web.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.istack.NotNull;
import org.apache.commons.lang3.StringUtils;

public class SqlUtils {

    private static final String SPACE = " ";

    private static final String REGEX = "\"([^\"]*)\"|'([^']*)'|[^\"|^']+[^\"|^']";

    public static Boolean matchKeyword(String text, String keyword, Boolean containInQuoted) {
        if (keyword != null) {
            Pattern regex = Pattern.compile(REGEX);
            Matcher regexMatcher = regex.matcher(text);
            String matchStr = null;
            while (regexMatcher.find()) {
                if (regexMatcher.group(1) != null) {
                    if (containInQuoted) {
                        matchStr = regexMatcher.group(1);
                    }
                } else if (regexMatcher.group(2) != null) {
                    if (containInQuoted) {
                        matchStr = regexMatcher.group(2);
                    }
                } else {
                    matchStr = regexMatcher.group();
                }
                if (matchStr != null && matchStr.contains(keyword)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 关键字匹配， 默认引号内的不匹配
     *
     * @param text
     * @param keyword
     * @return
     */
    public static Boolean matchKeyword(String text, String keyword) {
        return matchKeyword(text, keyword, false);
    }

    public static String trimLine(String sql) {
        return sql.replaceAll("\r|\n|\r\n", SPACE).trim();
    }


    public static String replaceLastSemicolon(String sql) {
        if (sql.trim().endsWith(";")) {
            sql = replaceLast(sql.trim(), ";", SPACE);
        }
        return sql;
    }


    public static String replaceLast(String text, String strToReplace, String replaceWithThis) {
        return text.replaceFirst("(?s)" + strToReplace + "(?!.*?" + strToReplace + ")", replaceWithThis);
    }

    public static String getMysqlLike(@NotNull String keyword) {
        return "%" + keyword.trim() + "%";
    }

    private static final Pattern sqlInjectionPattern = Pattern.compile(
            "'|--|(/\\\\*(?:.|[\\\\nr])*?\\\\*/)|" +
                    "(\\b(select|update|union|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|drop|execute)\\\\b)", Pattern.CASE_INSENSITIVE);

    public static boolean containsSqlInjection(String obj) {
        if (StringUtils.isBlank(obj)){
            return false;
        }
        return sqlInjectionPattern.matcher(obj).find();
    }



    public static <T> String convert(List <T> list) {

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < list.size(); i++) {
            sb.append("'").append(list.get(i).toString()).append("'");
            if (i < list.size() - 1) {
                sb.append(",");
            } else {
                sb.append(")");
            }
        }
        return sb.toString();
    }
}
