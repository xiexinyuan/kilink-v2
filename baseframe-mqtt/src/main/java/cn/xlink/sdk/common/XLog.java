package cn.xlink.sdk.common;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 日志控制类，SDK内的日志输出必须使用此类。
 */
public final class XLog extends BaseLog {
    private static XLog mInstance;
    private static Config mConfig;

    public static boolean init(Config config) {
        if (mInstance != null) {
            mInstance.error("Xlog", "logger has been created and it's no use to set config");
        } else if (config != null) {
            mConfig = config;
            //创建新的log对象
            getInstance();
            return true;
        }
        return false;
    }

    @NotNull
    public static XLog getInstance() {
        if (mInstance == null) {
            synchronized (XLog.class) {
                if (mConfig != null) {
                    mInstance = new XLog(mConfig);
                } else {
                    Config config = new BaseLog.Config();
                    mInstance = new XLog(config);
                }
            }
        }
        return mInstance;
    }

    /**
     * 是否可以输出debug的log
     *
     * @return
     */
    public static boolean isDebug() {
        if (isInited()) {
            return getInstance().isEnableDebugLog();
        } else {
            return false;
        }
    }

    /**
     * 是否可以输出error的log
     *
     * @return
     */
    public static boolean isError() {
        if (isInited()) {
            return getInstance().isEnableErrorLog();
        } else {
            return false;
        }
    }

    /**
     * 是否已经初始化过
     *
     * @return
     */
    public static boolean isInited() {
        return mInstance != null;
    }

    /**
     * 获取当前设置的config
     *
     * @return
     */
    @Nullable
    public static Config getConfig() {
        return mConfig;
    }

    /**
     * 开始log准备
     */
    public static void startLog() {
        if (mInstance != null) {
            mInstance.start();
        }
    }

    /**
     * 结束log处理
     */
    public static void stopLog() {
        if (mInstance != null) {
            mInstance.stop();
        }
    }

    public static int d(String tag, String msg) {
        return getInstance().debug(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return getInstance().debug(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        return getInstance().info(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return getInstance().info(tag, msg, tr);
    }

    public static int e(String tag, String msg) {
        return getInstance().error(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return getInstance().error(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        return getInstance().warn(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return getInstance().warn(tag, msg, tr);
    }

    public static int v(String tag, String msg) {
        return getInstance().verbose(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return getInstance().verbose(tag, msg, tr);
    }

    private XLog(@NotNull Config config) {
        super(config);
    }
}
