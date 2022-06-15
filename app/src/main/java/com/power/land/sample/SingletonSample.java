package com.power.land.sample;

import com.power.base.Singleton;

/**
 * 作者：Gongsensen
 * 日期：2022/6/15
 * 说明：Singleton工具类使用示例
 */
public class SingletonSample {
    public static SingletonSample getInstance() {
        return Singleton.getSingleton(SingletonSample.class);
    }

    public void a() {

    }

    public void b() {

    }
}
