package cn.xlink.sdk.common.crash;

/**
 * 异常信息处理监听回调
 * Created by taro on 2017/11/28.
 */
public interface CrashHandlerListener {
    /**
     * 处理异常信息
     *
     * @param thread    当前异常发生的线程信息
     * @param throwable 当前异常发生的问题
     */
    void onProcessedCrashInfo(Thread thread, Throwable throwable);
}
