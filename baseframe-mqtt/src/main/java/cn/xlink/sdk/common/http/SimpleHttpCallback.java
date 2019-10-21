package cn.xlink.sdk.common.http;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleHttpCallback<T> implements HttpCallback<T> {
    public void onSuccess(@Nullable T result) {
    }

    public void onError(int httpCode, Throwable thr) {
    }

    public void onCancel(HttpRunnable<T> httpRun) {
    }

    @Override
    public void onSuccess(@NotNull HttpRunnable<T> httpRun, @NotNull HttpResponse<T> response) {
        onSuccess(response.getBody());
    }

    @Override
    public void onError(@NotNull HttpRunnable<T> httpRun, @NotNull HttpResponse<T> response, Throwable thr) {
        onError(response.getCode(), thr);
    }

    @Override
    public void onCancel(@NotNull HttpRunnable<T> httpRun, @Nullable HttpRequest request) {
        onCancel(httpRun);
    }
}