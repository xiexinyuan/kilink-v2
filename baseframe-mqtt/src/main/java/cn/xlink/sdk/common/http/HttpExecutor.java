package cn.xlink.sdk.common.http;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 请求执行类
 * Created by taro on 2017/12/27.
 */
public enum HttpExecutor {
    INSTANCE;

    //任务栈
    private Set<RunnableExecutor> mTaskSet;
    //串行任务执行器
    private ExecutorService mSerialExecutor;
    //并行任务执行器
    private ExecutorService mParallelExecutor;

    public static HttpExecutor getInstance() {
        return INSTANCE;
    }

    /**
     * 在串行任务中执行
     *
     * @param runnable 请求对象
     */
    public static void executeInSerial(@NotNull HttpRunnable runnable) {
        synchronized (INSTANCE) {
            getInstance().initSerialExecutor();
            RunnableExecutor executor = new RunnableExecutor(runnable);
            getInstance().mTaskSet.add(executor);
            getInstance().mSerialExecutor.submit(executor);
        }
    }

    /**
     * 在并行任务中执行
     *
     * @param runnable 请求对象
     */
    public static void executeInParallel(@NotNull HttpRunnable runnable) {
        synchronized (INSTANCE) {
            getInstance().initParallelExecutor();
            RunnableExecutor executor = new RunnableExecutor(runnable);
            getInstance().mTaskSet.add(executor);
            getInstance().mParallelExecutor.submit(executor);
        }
    }

    private HttpExecutor() {
        //最大有32个任务缓存
        mTaskSet = new HashSet<>(32);
    }

    /**
     * 释放资源,结束任务所有任务与操作
     */
    public void release() {
        synchronized (INSTANCE) {
            //取消所有的任务
            for (RunnableExecutor executor : mTaskSet) {
                executor.mRunnable.cancel();
            }
            //清除所有的任务
            mTaskSet.clear();
            if (mSerialExecutor != null) {
                mSerialExecutor.shutdown();
            }
            if (mParallelExecutor != null) {
                mParallelExecutor.shutdown();
            }
            mSerialExecutor = null;
            mParallelExecutor = null;
        }
    }

    /**
     * 结束任务执行对象
     *
     * @param executor
     */
    protected void endRunnableExecutor(@NotNull RunnableExecutor executor) {
        synchronized (HttpExecutor.class) {
            mTaskSet.remove(executor);
        }
    }

    /**
     * 初始化串行任务执行器
     */
    protected synchronized void initSerialExecutor() {
        if (mSerialExecutor == null) {
            mSerialExecutor = Executors.newSingleThreadExecutor();
        }
    }

    /**
     * 初始化并行任务执行器
     */
    protected synchronized void initParallelExecutor() {
        if (mParallelExecutor == null) {
            mParallelExecutor = Executors.newFixedThreadPool(8);
        }
    }

    /**
     * 用于线程池执行的任务对象
     */
    private static class RunnableExecutor implements Runnable {
        HttpRunnable mRunnable;

        RunnableExecutor(@NotNull HttpRunnable runnable) {
            mRunnable = runnable;
        }

        @Override
        public void run() {
            //执行任务
            HttpResponse response = mRunnable.execute();
            HttpCallback callback = mRunnable.getCallback();
            if (mRunnable.isCanceled()) {
                if (callback != null) {
                    callback.onCancel(mRunnable, mRunnable.getRequest());
                }
                return;
            }
            //任务回调
            if (callback != null) {
                if (mRunnable.isExecuted()) {
                    if (response.isSuccess()) {
                        callback.onSuccess(mRunnable, response);
                    } else {
                        callback.onError(mRunnable, response, response.getError());
                    }
                } else {
                    callback.onError(mRunnable, response, response.getError());
                }
            }
            //移除掉当前的任务
            getInstance().endRunnableExecutor(this);
        }
    }
}
