package cn.xlink.sdk.common.handler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * Created by link on 2017/7/7.
 */

public class XLooper implements XLooperable {
    private static final ThreadLocal<XLooper> sThreadLocaL = new ThreadLocal<>();
    private static XLooper mMainLooper;

    private Thread mThread;

    @NotNull
    public static XLooper getMainLooper() {
        if (mMainLooper == null) {
            //这里需要注意,实际上mainLooper不一定会在当前创建XlinkSDK的线程,而是会在默认第一个调用此方法的线程中存在
            mMainLooper = newLooper("main");
        }
        return mMainLooper;
    }

    @NotNull
    public static XLooper newLooper() {
        return newLooper(null);
    }

    @Nullable
    public static XLooper myLooper() {
        return sThreadLocaL.get();
    }

    @NotNull
    private static XLooper newLooper(String thName) {
        XLooper looper = sThreadLocaL.get();
        if (looper == null) {
            looper = new XLooper(thName);
            sThreadLocaL.set(looper);
        }
        return looper;
    }

    private ScheduledExecutorService mExecutorService;

    protected XLooper() {
        this(null);
    }

    /**
     * 带名称的looper
     * @param thName
     */
    protected XLooper(String thName) {
        ThreadCreator creator;
        if (thName != null) {
            creator = new ThreadCreator(thName);
        } else {
            creator = new ThreadCreator();
        }
        Executors.newSingleThreadScheduledExecutor(creator);
        mExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public Thread getXThread() {
        return mThread;
    }

    @Override
    public boolean isCurrentXThread() {
        return Thread.currentThread() == mThread;
    }

    @Override
    public boolean isSameXLooperable(XLooperable looper) {
        if (looper instanceof XLooper) {
            return myLooper() == looper;
        } else {
            return false;
        }
    }

    @Override
    public void quitXLooper() {
        if (!mExecutorService.isShutdown()) {
            mExecutorService.shutdownNow();
        }
        mExecutorService = null;
        mThread = null;
    }

    public ScheduledExecutorService getExecutorService() {
        return mExecutorService;
    }

    private class ThreadCreator implements ThreadFactory {
        String mThreadName;

        ThreadCreator() {
        }

        ThreadCreator(@NotNull String thName) {
            mThreadName = thName;
        }

        @Override
        public Thread newThread(Runnable r) {
            mThread = new Thread(r);
            if (mThreadName != null) {
                mThread.setName(mThreadName);
            }
            //此处为模仿系统默认创建方式的类实现 Executors.DefaultThreadFactory
            if (mThread.isDaemon()) {
                mThread.setDaemon(false);
            }
            if (mThread.getPriority() != Thread.NORM_PRIORITY) {
                mThread.setPriority(Thread.NORM_PRIORITY);
            }
            return mThread;
        }
    }
}
