package cn.xlink.sdk.common.http;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by joel on 2017/12/22.
 */
public class HttpResponse<T> {

    /* 应答码 */
    private int mCode = -1;
    /* 应答头 */
    private Map<String, List<String>> mHeaders = new HashMap<>();
    /* 应答体 */
    private T mBody;
    //原始的字符串数据
    private String mRawStr;
    //错误信息
    private Throwable mError;
    //请求参数
    private HttpRequest mRequest;
    //是否取消了请求任务
    private boolean mIsCanceled;

    public HttpResponse() {
    }

    public HttpResponse(HttpRequest request) {
        mRequest = request;
    }

    public HttpRequest getRequest() {
        return mRequest;
    }

    public int getCode() {
        return mCode;
    }

    public Map<String, List<String>> getHeaders() {
        return mHeaders;
    }

    @Nullable
    public T getBody() {
        return mBody;
    }

    public Throwable getError() {
        return mError;
    }

    public String getRawStr() {
        return mRawStr;
    }

    public boolean isSuccess() {
        //未取消请求的情况下,并且返回码为200
        return !mIsCanceled && mCode == 200;
    }

    public void setCode(int code) {
        mCode = code;
    }

    public void setCanceled(boolean isCanceled) {
        mIsCanceled = isCanceled;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        mHeaders = headers;
    }

    public void setBody(T body) {
        mBody = body;
    }

    public void setRawStr(String result) {
        mRawStr = result;
    }

    public void setError(Throwable thr) {
        mError = thr;
    }

    @Override
    public String toString() {
        int bodyLen = mRawStr != null ? mRawStr.length() : 0;
        bodyLen += 600;
        StringBuilder stringBuilder = new StringBuilder(bodyLen);
        stringBuilder.append('\n');
        stringBuilder.append("===================================================");
        stringBuilder.append("\n||  Http Code: ");
        stringBuilder.append(mCode);
        stringBuilder.append('\n');
        for (String key : mHeaders.keySet()) {
            List<String> value = mHeaders.get(key);
            stringBuilder.append('|');
            stringBuilder.append('|');
            stringBuilder.append(' ');
            stringBuilder.append(' ');
            stringBuilder.append(key);
            stringBuilder.append(": ");
            stringBuilder.append(Arrays.toString(value.toArray()));
            stringBuilder.append('\n');
        }
        stringBuilder.append("||  rawStr:");
        stringBuilder.append(mRawStr);
        stringBuilder.append("\n===================================================");
        stringBuilder.append('\n');
        return stringBuilder.toString();
    }
}
