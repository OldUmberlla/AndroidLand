package com.power.base.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 作者：Gongsensen
 * 日期：2022/6/10
 * 说明：封装线程池+异步任务回调主线程工具类
 */
public class JobExecutor {
    private static final String TAG = "JobExecutor";

    private static JobExecutor sJobExecutor;

    public static JobExecutor instance() {
        synchronized (JobExecutor.class) {
            if (sJobExecutor == null) {
                synchronized (JobExecutor.class) {
                    sJobExecutor = new JobExecutor();
                    return sJobExecutor;
                }
            }
            return sJobExecutor;
        }
    }

    private ThreadPoolExecutor mExecutor;
    private final Handler mHandler;
    private final List<Future<?>> mFutureList = new ArrayList<>();

    public static JobExecutor newInstance() {
        return new JobExecutor();
    }

    public static JobExecutor newInstance(@NonNull Lifecycle lifecycle) {
        return new JobExecutor(lifecycle);
    }

    private JobExecutor() {
        mHandler = new Handler(Looper.getMainLooper());
        mExecutor = new ThreadPoolExecutor(8, 16, 10,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(32),
                new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    private JobExecutor(Lifecycle lifecycle) {
        this();

        lifecycle.addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                JobExecutor.this.onDestroy();
            }
        });
    }

    public void onDestroy() {
        if (mExecutor == null) {
            throw new NullPointerException("Method onDestroy() should be called only once.");
        }
        mExecutor.shutdown();
        mExecutor = null;
        mHandler.removeCallbacksAndMessages(null);
    }

    public void execute(final Runnable task) {
        if (mExecutor == null) {
            throw new NullPointerException("Shouldn't call execute() after onDestroy() invoked.");
        }
        mExecutor.execute(task);
    }

    public <T> void execute(final Task<T> task) {
        if (mExecutor == null) {
            throw new NullPointerException("Shouldn't call execute() after onDestroy() invoked.");
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    T res = task.call();
                    postOnMainThread(task, res);
                    task.onJobThread(res);
                } catch (Exception e) {
                    postOnMainThread(e.getMessage());
                }
            }

            private void postOnMainThread(final Task<T> task, final T res) {
                mHandler.post(() -> {
                    task.onMainThread(res);
                    task.onJobCompleted();
                });
            }

            private void postOnMainThread(String errMsg) {
                mHandler.post(() -> {
                    task.onJobError(errMsg);
                    task.onJobCompleted();
                });
            }
        });
    }

    public <T> Future<T> submitCallable(Callable<T> task) {
        if (mExecutor == null) {
            throw new NullPointerException("Shouldn't call submit() after onDestroy() invoked.");
        }
        Future<T> future = mExecutor.submit(task);
        mFutureList.add(future);
        return future;
    }

    public <T> Future<T> submit(Task<T> task) {
        if (mExecutor == null) {
            throw new NullPointerException("Shouldn't call submit() after onDestroy() invoked.");
        }
        Future<T> future = mExecutor.submit(new Callable<T>() {
            @Override
            public T call() {
                try {
                    T res = task.call();
                    postOnMainThread(task, res);
                    task.onJobThread(res);
                    return res;
                } catch (Exception e) {
                    postOnMainThread(e.getMessage());
                }
                return null;
            }

            private void postOnMainThread(final Task<T> task, final T res) {
                //noinspection Convert2Lambda
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        task.onMainThread(res);
                        task.onJobCompleted();
                    }
                });
            }

            private void postOnMainThread(String errMsg) {
                //noinspection Convert2Lambda
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        task.onJobError(errMsg);
                        task.onJobCompleted();
                    }
                });
            }
        });
        mFutureList.add(future);
        return future;
    }

    public void cancelAll(boolean mayInterruptIfRunning) {
        Iterator<Future<?>> it = mFutureList.iterator();
        while (it.hasNext()) {
            Future<?> future = it.next();
            future.cancel(mayInterruptIfRunning);
            it.remove();
        }
    }

    public interface Task<T> extends Callable<T> {
        default void onMainThread(T result) {
            // default no implementation
        }

        default void onJobThread(T result) {
            // default no implementation
        }

        default void onJobError(String msg) {
            Log.e(TAG, "job execute error:" + msg);
        }

        default void onJobCompleted() {
        }
    }
}
