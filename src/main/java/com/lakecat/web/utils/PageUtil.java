package com.lakecat.web.utils;


import com.lakecat.web.entity.BaseInfo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 自定义List分页工具
 *
 * @author hanwl
 */
public class PageUtil {

    /**
     * 开始分页
     *
     * @param pageNum  页码
     * @param pageSize 每页多少条数据
     */
    public static <T> List <T> startPage(List <T> list, Integer pageNum, Integer pageSize) {
        if (list == null) {
            return Collections.emptyList();
        }
        if (list.isEmpty()) {
            return list;
        }

        // 记录总数
        Integer count = list.size();
        // 页数
        int pageCount;
        if (count % pageSize == 0) {
            pageCount = count / pageSize;
        } else {
            pageCount = count / pageSize + 1;
        }

        int fromIndex; // 开始索引
        int toIndex; // 结束索引

        if (!Objects.equals(pageNum, pageCount)) {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = fromIndex + pageSize;
        } else {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = count;
        }

        return list.subList(fromIndex, toIndex);
    }
}