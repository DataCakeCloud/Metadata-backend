package com.lakecat.web.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

public class TagUtils {

    public static final String[] SIZE_GROUP_DESC1 = new String[]{
        "低", "中等", "高"
    };

    public static final String[] SIZE_GROUP_DESC2 = new String[]{
        "小", "中等", "大"
    };

    public static final String[] SIZE_GROUP_DESC3 = new String[]{
        "低", "中", "高"
    };

    public static String assertTagValue(Double value, String prefix, double[] valueGroups, String[] tagGroups)
        throws Exception {
        Pair<double[], String[]> pair = Pair.of(valueGroups, tagGroups);
        return assertTagValue(value, prefix, pair, true);
    }

    public static String assertTagValue(Double value, String prefix, double[] valueGroups, String[] tagGroups , boolean isAsc)
        throws Exception {
        Pair<double[], String[]> pair = Pair.of(valueGroups, tagGroups);
        return assertTagValue(value, prefix, pair, isAsc);
    }

    public static String assertTagValue(Double value, String prefix, Pair<double[], String[]> rangePair)
        throws Exception {
        return assertTagValue(value, prefix, rangePair, true);
    }

    public static String assertTagValue(Double value, String prefix, Pair<double[], String[]> rangePair,
        boolean isAsc) throws Exception {
        String tag = null;
        checkLength(prefix, rangePair);
        double[] left = rangePair.getLeft();
        String[] right = rangePair.getRight();
        if (!isAsc) {
            right = ArrayUtils.clone(right);
            ArrayUtils.reverse(right);
        }
        if (value == null) {
            tag = prefix + right[0];
            return tag;
        }
        for (int i = 1; i <= left.length; i++) {
            if (i == left.length) {
                if (value >= left[i-1]) {
                    tag = prefix + right[i-1];
                    break;
                }
            }
            else if (value >= left[i-1] && value < left[i]) {
                tag = prefix + right[i-1];
                break;
            }
        }
        return tag;
    }

    public static void checkLength(String prefix, Pair<double[], String[]> rangePair) throws Exception {
        if (rangePair.getLeft().length > rangePair.getRight().length) {
            throw new Exception(prefix + ": Inconsistent group length.");
        }
    }
}
