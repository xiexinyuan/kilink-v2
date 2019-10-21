package cn.xlink.sdk.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 日志输出接口
 * Created by taro on 2017/12/7.
 */
public interface Loggable {
    /**
     * Priority constant for the println method; use v.
     */
    public static final int VERBOSE = 2;

    /**
     * Priority constant for the println method; use Log.d.
     */
    public static final int DEBUG = 3;

    /**
     * Priority constant for the println method; use this.i.
     */
    public static final int INFO = 4;

    /**
     * Priority constant for the println method; use this.w.
     */
    public static final int WARN = 5;

    /**
     * Priority constant for the println method; use this.e.
     */
    public static final int ERROR = 6;

    /**
     * Priority constant for the println method.
     */
    public static final int NONE = Integer.MIN_VALUE;

    /**
     * 日志输出
     *
     * @param level 日志等级,查看
     * @param tag   标签
     * @param msg   消息
     * @param e     错误信息
     * @return
     */
    public int log(int level, @Nullable String tag, @NotNull String msg, @Nullable Throwable e);
}
