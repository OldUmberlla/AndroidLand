package com.power.land.sample;

import com.power.base.BaseSingleton;

/**
 * 作者：Gongsensen
 * 日期：2022/6/15
 * 说明：BaseSingleton使用示例
 */
public class BaseSingletonSample {
    public static BaseSingletonSample getInstance() {
        return new BaseSingleton<BaseSingletonSample>() {
            @Override
            protected BaseSingletonSample newInstance() {
                return new BaseSingletonSample();
            }
        }.getInstance();
    }

    public void b() {

    }
}
