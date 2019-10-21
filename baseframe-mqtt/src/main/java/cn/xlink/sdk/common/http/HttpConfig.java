package cn.xlink.sdk.common.http;

/**
 * 请求配置,未完成
 * Created by taro on 2017/12/27.
 */
public class HttpConfig {
    private static HttpConfig mDefaultConfig;

    /* 是否使用缓存，默认不使用 */
    boolean mUseCache = false;
    /* 连接是否可被重定向，默认可以 */
    boolean mInstanceFollowRedirects = true;
    /* 链接超时，默认30秒 */
    int mConnTimeout = 30000;
    /* 读超时，默认30秒 */
    int mReadTimeout = 30000;
    //是否将postParams转成json提交而不是key=value的形式
    boolean mIsPostParamsForJson = true;
    SSLSocketFactoryProvider mSSLSocketProvider;

    /**
     * 使用default request创建get方式的请求参数
     *
     * @return
     */
    public static HttpRequest newGetRequest() {
        return new HttpRequest(HttpRequest.REQUEST_METHOD_GET);
    }

    /**
     * 使用Default request 创建post方式的请求参数
     *
     * @return
     */
    public static HttpRequest newPostRequest() {
        return new HttpRequest(HttpRequest.REQUEST_METHOD_POST);
    }

    public static HttpConfig getDefaultConfig() {
        return mDefaultConfig;
    }

    public static void setDefaultConfig(HttpConfig config) {
        mDefaultConfig = config;
    }

    public HttpConfig() {
    }

    /**
     * 是否启用缓存
     *
     * @param userCache
     * @return
     */
    public HttpConfig setUseCache(boolean userCache) {
        mUseCache = userCache;
        return this;
    }

    /**
     * 设置连接超时时间,默认为30秒
     *
     * @param connTimeout
     */
    public HttpConfig setConnTimeout(int connTimeout) {
        mConnTimeout = connTimeout;
        return this;
    }


    /**
     * 设置数据读取时间,默认为30秒
     *
     * @param readTimeout
     */
    public HttpConfig setReadTimeout(int readTimeout) {
        mReadTimeout = readTimeout;
        return this;
    }

    /**
     * 设置是否允许重定向
     *
     * @param enabled
     * @return
     */
    public HttpConfig setEnableRedirect(boolean enabled) {
        mInstanceFollowRedirects = enabled;
        return this;
    }

    /**
     * 设置是否将post参数转换成json字符串进行请求,否则以 {@code key=value&key=value} 形式请求
     *
     * @param isForJson
     * @return
     */
    public HttpConfig setIsPostParamsForJson(boolean isForJson) {
        mIsPostParamsForJson = isForJson;
        return this;
    }

    /**
     * 设置SSLSocket Factory创建对象
     *
     * @param provider
     * @return
     */
    public HttpConfig setSSLSocketFactoryProvider(SSLSocketFactoryProvider provider) {
        mSSLSocketProvider = provider;
        return this;
    }

    /**
     * 是否使用缓存
     *
     * @return
     */
    public boolean isUseCache() {
        return mUseCache;
    }

    /**
     * 获取连接超时时间
     *
     * @return
     */
    public int getConnTimeout() {
        return mConnTimeout;
    }

    /**
     * 获取读取数据超时时间
     *
     * @return
     */
    public int getReadTimeout() {
        return mReadTimeout;
    }

    /**
     * 是否使用重定向
     *
     * @return
     */
    public boolean isEnabledRedirect() {
        return mInstanceFollowRedirects;
    }

    /**
     * 是否提交参数转化成JSON数据进行提交
     *
     * @return
     */
    public boolean isPostParamsForJson() {
        return mIsPostParamsForJson;
    }

    /**
     * 获取SSL Socket创建对象
     *
     * @return
     */
    public SSLSocketFactoryProvider getSSLSocketFactoryProvider() {
        return mSSLSocketProvider;
    }

    public HttpConfig build() {
        return this;
    }
}
