package com.power.base;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 作者：Gongsensen
 * 日期：2022/6/14
 * 说明：容器 + 锁 封装单例工具
 */
public class Singleton<T> {
    private static final ConcurrentMap<Class, Object> INSTANCE_MAP = new ConcurrentHashMap<>();

    private Singleton() {

    }

    public static <T> T getSingleton(Class<T> type) {
        Object obj = INSTANCE_MAP.get(type);
        try {
            if (obj == null) {
                synchronized (INSTANCE_MAP) {
                    obj = type.newInstance();
                    INSTANCE_MAP.put(type, obj);
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (T) obj;
    }
}
