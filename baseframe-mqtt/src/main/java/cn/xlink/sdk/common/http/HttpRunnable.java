package cn.xlink.sdk.common.http;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by taro on 2017/12/27.
 */
public interface HttpRunnable<T> {
    public @NotNull HttpRequest getRequest( );

    public @NotNull HttpResponse<T> execute( );

    @Nullable
    public HttpCallback<T> getCallback( );

    public HttpRunnable<T> setResponseConverter(HttpConvertable<T> coverter);

    public boolean isCanceled( );

    public boolean isExecuted( );

    public void cancel( );

    public void enqueue(@Nullable HttpCallback<T> callback);
}
