package cn.xlink.sdk.common.http;

import org.jetbrains.annotations.NotNull;

/**
 * http请求结果转换对象
 * Created by taro on 2018/1/4.
 */
public interface HttpConvertable<T> {
    /**
     * 响应数据转换成实际的对象
     *
     * @param httpRun  http请求
     * @param response 响应体
     * @param result   响应原始数据
     * @return
     */
    public T onResponseConvert(@NotNull HttpRunnable<T> httpRun, @NotNull HttpResponse<T> response,
                               @NotNull String result);
}
