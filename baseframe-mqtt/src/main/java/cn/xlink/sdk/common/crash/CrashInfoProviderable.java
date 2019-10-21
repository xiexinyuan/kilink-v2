package cn.xlink.sdk.common.crash;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 异常信息提供接口
 * Created by taro on 2017/11/28.
 */
public interface CrashInfoProviderable {
    /**
     * 提供当前运行环境信息
     *
     * @return
     */
    public String provideEnvironment( );

    /**
     * 提供异常文件保存的路径
     *
     * @return
     */
    @NotNull
    public String provideCrashFileStoragePath( );

    /**
     * 提供异常文件的文件名
     *
     * @param dateTime
     * @return
     */
    @Nullable
    public String provideCrashFileName(@NotNull String dateTime);
}
