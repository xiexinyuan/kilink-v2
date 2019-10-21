package cn.xlink.sdk.common.http;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * http请求回调接口
 * Created by taro on 2017/12/27.
 */
public interface HttpCallback<T> {
    /**
     * 请求成功回调
     *
     * @param httpRun  请求对象
     * @param response 请求结果
     */
    public void onSuccess(@NotNull HttpRunnable<T> httpRun, @NotNull HttpResponse<T> response);

    /**
     * 请求错误回调
     *
     * @param httpRun  请求对象
     * @param response 请求结果
     * @param thr      错误信息
     */
    public void onError(@NotNull HttpRunnable<T> httpRun, @NotNull HttpResponse<T> response,
                        Throwable thr);

    /**
     * 取消请求回调
     *
     * @param httpRun 请求对象
     * @param request 请求参数
     */
    public void onCancel(@NotNull HttpRunnable<T> httpRun, @Nullable HttpRequest request);
}
