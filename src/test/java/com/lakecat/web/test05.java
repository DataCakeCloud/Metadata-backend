package com.lakecat.web;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by slj on 2022/6/21.
 */
public class test05 {

    public static void main(String[] args) {

        StringBuilder sb = new StringBuilder();
        String keyWord = "analyst.share_game_apk_summary";
        if (StringUtils.isNotBlank(keyWord)) {
            String[] keyWords = keyWord.split(" ");
            for (String word : keyWords) {
                String replace;
                if (word.contains(".")) {
                    replace = word.split("\\.")[1];

                } else {
                    replace = word;
                }
                sb.append("+");
                sb.append(replace);
            }
        }

        System.out.println(sb);

    }

}
