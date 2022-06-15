package com.power.base;

/**
 * 作者：Gongsensen
 * 日期：2022/6/14
 * 说明：以抽象类实现单例类基类
 */
public abstract class BaseSingleton<T> {
    private T instance;

    protected abstract T newInstance();

    public final T getInstance() {
        if (instance == null) {
            synchronized (BaseSingleton.class) {
                if (instance == null) {
                    instance = newInstance();
                }
            }
        }
        return instance;
    }

}
