package cn.xlink.sdk.common.http;

import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLSocketFactory;

/**
 * SSL socket创建对象
 * Created by taro on 2018/2/28.
 */
public interface SSLSocketFactoryProvider {
    /**
     * 获取 SSL socket对象
     *
     * @return
     */
    public @Nullable SSLSocketFactory getSSLSocketFactory( );
}
