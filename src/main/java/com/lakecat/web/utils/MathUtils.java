package com.lakecat.web.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MathUtils {

    /**
     * 获取等分
     *
     * @param partNum
     * @param start
     * @param end
     * @return
     */
    public static List<String> getEqualDivision(int partNum, int start, int end) {
        List<String> list = new ArrayList<>();
        int interval = end - start;
        int incrNum = interval / partNum + (interval % partNum == 0 ? 0 : 1);
        int tmp;
        for (int i = start; i < end; i += incrNum) {
            tmp = i + incrNum;
            if (tmp > end) {
                tmp = end;
            }
            list.add("[" + i + "," + tmp + ")");
        }
        return list;
    }


    public static int getRandomInt(int max) {
        return (int) (Math.random() * max);
    }

    /**
     * 对double类型数据格式化
     *
     * @param value
     * @param num   小数点后位数
     * @return
     */
    public static double formatDouble(double value, int num) {
        return Double.parseDouble(new Formatter().format("%." + num + "f", value).toString());
    }

    /**
     * list 求差集
     *
     * @param n
     * @param m
     * @param <T>
     * @return
     */
    public static <T> Set<T> getDifferenceSet(Collection<T> n, Collection<T> m) {
        //转化最长列表
        Set<T> set = new HashSet<>(n.size() > m.size() ? n : m);
        //循环最短列表
        Collection<T> ts = n.size() > m.size() ? m : n;
        for (T t : ts) {
            if (set.contains(t)) {
                set.remove(t);
            } else {
                set.add(t);
            }
        }
        return new HashSet<T>(set);
    }

    /**
     * list 求交集
     *
     * @param n
     * @param m
     * @param <T>
     * @return
     */
    public static <T> List<T> getIntersection(List<T> n, List<T> m) {
        Set<T> setN = new HashSet<>(n);
        Set<T> setM = new HashSet<>(m);
        setN.retainAll(setM);
        return new ArrayList<T>(setN);
    }

    /**
     * list 集合并集
     *
     * @param n
     * @param m
     * @param <T>
     * @return
     */
    public static <T> List<T> getUnion(List<T> n, List<T> m) {
        Set<T> setN = new HashSet<>(n);
        Set<T> setM = new HashSet<>(m);
        setN.addAll(setM);
        return new ArrayList<T>(setN);
    }

    /**
     * 数组求差集
     *
     * @param n
     * @param m
     * @param <T>
     * @return
     */
    public static <T> T[] getDifferenceSet(T[] n, T[] m) {
        Set<T> set = MathUtils.getDifferenceSet(Arrays.asList(n), Arrays.asList(m));
        return set.toArray(Arrays.copyOf(n, set.size()));
    }

    /**
     * 数组求交集
     *
     * @param n
     * @param m
     * @param <T>
     * @return
     */
    public static <T> T[] getIntersection(T[] n, T[] m) {
        List<T> list = MathUtils.getIntersection(Arrays.asList(n), Arrays.asList(m));
        return list.toArray(Arrays.copyOf(n, list.size()));
    }

    /**
     * 数组并集
     *
     * @param n
     * @param m
     * @param <T>
     * @return
     */
    public static <T> T[] getUnion(T[] n, T[] m) {
        List<T> list = MathUtils.getUnion(Arrays.asList(n), Arrays.asList(m));
        return list.toArray(Arrays.copyOf(n, list.size()));
    }
}
