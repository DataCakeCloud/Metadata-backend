package com.lakecat.web.utils;

import java.util.concurrent.TimeUnit;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

public class CacheUtils {

    public static <K, V> ExpiringMap<K, V> getCacheMap(int cacheCapacity, int expirationSecond, Class<K> kClass, Class<V> vClass) {
        ExpiringMap<K, V> map = ExpiringMap.builder()
            .maxSize(cacheCapacity)
            .expiration(expirationSecond, TimeUnit.SECONDS)
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .variableExpiration()
            .build();
        return map;
    }
}
