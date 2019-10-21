package cn.xlink.sdk.common.http;

import cn.xlink.sdk.common.JsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求参数
 * Created by taro on 2017/12/27.
 */
public class HttpRequest {
    public static final String REQUEST_METHOD_GET = "GET";
    public static final String REQUEST_METHOD_POST = "POST";
    public static final String REQUEST_METHOD_DELETE = "DELETE";
    public static final String REQUEST_METHOD_HEAD = "HEAD";
    public static final String REQUEST_METHOD_OPTIONS = "OPTIONS";
    public static final String REQUEST_METHOD_PUT = "PUT";
    public static final String REQUEST_METHOD_TRACE = "TRACE";

    //json数据
    public static final String CONTENT_TYPE_JSON = "application/json";
    //.txt文件,.sor文件,.sol文件
    public static final String CONTENT_TYPE_TEXT = "text/plain";
    //xhtml,plg,jsp,htx,html,htm 文件
    public static final String CONTENT_TYPE_HTML = "text/html";

    /* 是否使用缓存，默认不使用 */
    protected boolean mUseCache = false;
    /* 连接是否可被重定向，默认可以 */
    protected boolean mInstanceFollowRedirects = true;
    /* post的数据 */
    protected String mPostContent = null;
    /* 请求方法 */
    protected String mRequestMethod;
    /* 链接超时，默认30秒 */
    protected int mConnTimeout = 30000;
    /* 读超时，默认30秒 */
    protected int mReadTimeout = 15000;
    //是否将postParams转成json提交而不是key=value的形式
    protected boolean mIsPostParamsForJson = true;

    protected String mUrl;
    protected Map<String, String> mHeader;
    //URL请求参数
    protected Map<String, String> mQueryParams;
    //POST请求参数
    protected Map<String, String> mPostParams;
    //post的对象
    protected Object mPostBody;

    public HttpRequest(String requestMethod) {
        //使用默认配置请求
        this.withConfig(HttpConfig.getDefaultConfig());
        //请求方法
        mRequestMethod = requestMethod;
        mHeader = new HashMap<>();
        mPostParams = new HashMap<>();
        mQueryParams = new HashMap<>();
        //请求内容类型
        setContentType(CONTENT_TYPE_JSON);
    }

    /**
     * 使用默认的请求合并到新的请求中,新请求的独立设置的属性会覆盖默认的设置
     *
     * @param config
     */
    private void withConfig(HttpConfig config) {
        if (config != null) {
            this.mUseCache = config.mUseCache;
            this.mInstanceFollowRedirects = config.mInstanceFollowRedirects;
            this.mIsPostParamsForJson = config.mIsPostParamsForJson;
            this.mConnTimeout = config.mConnTimeout;
            this.mReadTimeout = config.mReadTimeout;
        }
    }

    /**
     * 合并已经有的请求到当前请求中
     *
     * @param request
     * @return
     */
    public HttpRequest withRequest(HttpRequest request) {
        if (request != null) {
            this.mUseCache = request.mUseCache;
            this.mInstanceFollowRedirects = request.mInstanceFollowRedirects;
            this.mIsPostParamsForJson = request.mIsPostParamsForJson;
            this.mPostContent = request.mPostContent;
            this.mConnTimeout = request.mConnTimeout;
            this.mReadTimeout = request.mReadTimeout;
            this.mUrl = request.mUrl;
            this.mHeader.putAll(request.mHeader);
            this.mQueryParams.putAll(request.mQueryParams);
            this.mPostParams.putAll(request.mPostParams);
            this.mPostBody = request.mPostBody;
        }
        return this;
    }

    /**
     * 设置请求方式
     *
     * @param requestMethod
     * @return
     */
    public HttpRequest setRequestMethod(String requestMethod) {
        mRequestMethod = requestMethod;
        return this;
    }

    /**
     * 设置请求URL
     *
     * @param url
     * @return
     */
    public HttpRequest setUrl(String url) {
        mUrl = url;
        return this;
    }

    /**
     * 是否启用缓存
     *
     * @param userCache
     * @return
     */
    public HttpRequest setUseCache(boolean userCache) {
        mUseCache = userCache;
        return this;
    }

    /**
     * 设置连接超时时间,默认为30秒
     *
     * @param connTimeout
     */
    public HttpRequest setConnTimeout(int connTimeout) {
        mConnTimeout = connTimeout;
        return this;
    }


    /**
     * 设置数据读取时间,默认为30秒
     *
     * @param readTimeout
     */
    public HttpRequest setReadTimeout(int readTimeout) {
        mReadTimeout = readTimeout;
        return this;
    }

    /**
     * 设置是否允许重定向
     *
     * @param enabled
     * @return
     */
    public HttpRequest setEnableRedirect(boolean enabled) {
        mInstanceFollowRedirects = enabled;
        return this;
    }

    /**
     * 设置post的文本内容,该内容会直接post
     *
     * @param content
     * @return
     */
    public HttpRequest setPostContent(String content) {
        mPostContent = content;
        return this;
    }

    /**
     * 设置是否将post参数转换成json字符串进行请求,否则以 {@code key=value&key=value} 形式请求
     *
     * @param isForJson
     * @return
     */
    public HttpRequest setIsPostParamsForJson(boolean isForJson) {
        mIsPostParamsForJson = isForJson;
        return this;
    }

    /**
     * 设置需要post为json数据的对象
     *
     * @param obj
     * @return
     */
    public HttpRequest setPostJson(Object obj) {
        mPostBody = obj;
        return this;
    }

    /**
     * 设置请求内容类型
     *
     * @param contentType
     * @return
     */
    public HttpRequest setContentType(String contentType) {
        mHeader.put("Content-Type", contentType);
        return this;
    }

    /**
     * 添加headers
     *
     * @param headers
     * @return
     */
    public HttpRequest addHeaders(Map<String, String> headers) {
        if (headers != null && headers.size() > 0) {
            mHeader.putAll(headers);
        }
        return this;
    }

    /**
     * 添加header信息
     *
     * @param key
     * @param value
     * @return
     */
    public HttpRequest addHeader(@NotNull String key, String value) {
        mHeader.put(key, value);
        return this;
    }

    /**
     * 添加URL请求参数
     *
     * @param params
     * @return
     */
    public HttpRequest addQueryParams(Map<String, String> params) {
        if (params != null && params.size() > 0) {
            mQueryParams.putAll(params);
        }
        return this;
    }

    /**
     * 添加URL请求参数
     *
     * @param key
     * @param value
     * @return
     */
    public HttpRequest addQueryParam(@NotNull String key, String value) {
        mQueryParams.put(key, value);
        return this;
    }

    /**
     * 添加POST请求参数
     *
     * @param params
     * @return
     */
    public HttpRequest addPostParams(Map<String, String> params) {
        if (params != null && params.size() > 0) {
            mPostParams.putAll(params);
        }
        return this;
    }

    /**
     * 添加POST请求参数
     *
     * @param key
     * @param value
     * @return
     */
    public HttpRequest addPostParam(@NotNull String key, String value) {
        mPostParams.put(key, value);
        return this;
    }

    /**
     * 获取请求方法
     *
     * @return
     */
    public String getRequestMethod() {
        return mRequestMethod;
    }

    /**
     * 判断当前请求是否携带提交参数
     *
     * @return
     */
    public boolean hasPostContent() {
        //这里不使用请求方法作用是否有post内容处理,因为有可能部分非post方法也会带请求体
        return mPostBody != null || mPostParams.size() > 0 || mPostContent != null;
    }

    /**
     * 判断是否启用缓存
     *
     * @return
     */
    public boolean isUseCache() {
        return mUseCache;
    }

    /**
     * 判断是否启用重定向
     *
     * @return
     */
    public boolean isEnableRedirect() {
        return mInstanceFollowRedirects;
    }

    /**
     * 获取连接超时时长
     *
     * @return
     */
    public int getConnTimeout() {
        return mConnTimeout;
    }

    /**
     * 获取读取数据超时时长
     *
     * @return
     */
    public int getReadTimeout() {
        return mReadTimeout;
    }

    /**
     * 获取URL
     *
     * @return
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * 获取headers
     *
     * @return
     */
    public Map<String, String> getHeaders() {
        return mHeader;
    }

    /**
     * 获取post参数
     *
     * @return
     */
    public Map<String, String> getPostParams() {
        return mPostParams;
    }

    /**
     * 获取请求参数
     *
     * @return
     */
    public Map<String, String> getQueryParams() {
        return mQueryParams;
    }

    /**
     * 获取请求的实体对象
     *
     * @return
     */
    public Object getPostBody() {
        return mPostBody;
    }

    /**
     * 获取post的参数文本
     *
     * @return
     */
    public String getPostContent() {
        return mPostContent;
    }

    /**
     * 获取请求的内容,当存在post对象时,转换成json字符串;<br>
     * 当存在post参数时,转换成json字符串;<br>
     * 当存在postContent的直接请求内容时,直接返回该字符串内容;<br>
     *
     * @return
     */
    public String getRequestContent() {
        if (mPostBody != null) {
            return JsonBuilder.toJson(mPostBody).toString();
        } else if (mPostParams != null && mPostParams.size() > 0) {
            if (mIsPostParamsForJson) {
                //参数值转成json
                return new JsonBuilder()
                        .put(mPostParams)
                        .toString();
            } else {
                //参数值转成key=value
                StringBuilder builder = new StringBuilder(mPostParams.size() * 20);
                for (Map.Entry<String, String> entry : mPostParams.entrySet()) {
                    builder.append(entry.getKey());
                    builder.append('=');
                    builder.append(entry.getValue() == null ? "" : entry.getValue());
                    builder.append('&');
                }
                return builder.toString();
            }
        } else if (mPostContent != null) {
            return mPostContent;
        } else {
            return "";
        }
    }

    /**
     * 获取请求URL,将请求参数已经拼接
     *
     * @return
     */
    public String getRequestUrl() {
        //无效URL
        if (mUrl == null || mUrl.length() <= 0) {
            return null;
        }
        //去除多余空白字符
        String urlStr = mUrl.trim();
        Map<String, String> query = mQueryParams;

        StringBuilder builder = new StringBuilder(500);
        String lower = urlStr.toLowerCase();
        //判断是否带http请求前缀
        if (!lower.startsWith("http://")
                && !lower.startsWith("https://")) {
            builder.append("http://");
        }
        builder.append(urlStr);

        if (query != null && query.size() > 0) {
            //url最后带/时,并且当前需要添加参数时,去掉/
            if (urlStr.endsWith("/")) {
                builder.deleteCharAt(builder.length() - 1);
            }
            // 拼接第一个query时，先拼接"?"，再拼接query
            builder.append('?');
            for (String key : query.keySet()) {
                builder.append(key);
                builder.append('=');
                String value = query.get(key);
                if (value != null && !value.isEmpty()) {
                    builder.append(value);
                }
                // 其他则先拼接"&"，再拼接query
                builder.append('&');
            }
        }
        return builder.toString();
    }

    /**
     * 创建指定返回类型的请求对象,默认为obj
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> HttpRunnable<T> createHttpRunnable(@Nullable Class<T> clazz) {
        return new HttpHandler<T>(this);
    }

    @Override
    public String toString() {
        JsonBuilder builder = new JsonBuilder();
        builder.put("method", this.getRequestMethod())
                .put("url", this.getRequestUrl())
                .put("connTimeout", this.getConnTimeout())
                .put("readTimeout", this.getReadTimeout())
                .put("header", this.getHeaders());
        return builder.toString();
    }
}
