package cn.xlink.sdk.common.http;

import cn.xlink.sdk.common.XLog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * 默认的http请求实现类
 * Created by taro on 2017/12/27.
 */
public class HttpHandler<T> implements HttpRunnable<T> {
    public static final String SEPARATE_LINE = "======================================================";

    private static final String TAG = "HttpHandler";
    //请求结果
    HttpResponse<T> mResponse = null;
    HttpRequest mRequest;
    HttpURLConnection mHttpUrlConn = null;
    // 输出流
    OutputStream mOutputStream = null;
    BufferedWriter mBufferedWriter = null;
    // 输入流
    InputStream mInputStream = null;
    BufferedReader mBufferedReader = null;
    //是否取消该任务
    volatile boolean mIsSetCancel = false;
    //是否已经执行了任务
    volatile boolean mIsExecuted = false;

    //回调事件
    HttpCallback<T> mCallback = null;
    //数据转换结果
    HttpConvertable<T> mConverter = null;

    public HttpHandler(@NotNull HttpRequest request) {
        mRequest = request;
    }

    /**
     * 关闭流
     */
    private void closeStream() {
        //关闭流
        if (mBufferedWriter != null) {
            try {
                mBufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mBufferedReader != null) {
            try {
                mBufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //若需要取消掉任务,中止连接操作
        if (mHttpUrlConn != null && mIsSetCancel) {
            mHttpUrlConn.disconnect();
        }
        //如果断开url connection,则底层的socket是无法被复用到的.可以不管他.
//        if (mHttpUrlConn != null) {
//            mHttpUrlConn.disconnect();
//        }
        mBufferedWriter = null;
        mBufferedReader = null;
        mOutputStream = null;
        mInputStream = null;
        mHttpUrlConn = null;
    }

    /**
     * 检测当前是否已经取消掉
     *
     * @return
     */
    private boolean checkCancel() {
        if (mIsSetCancel) {
            if (mResponse == null) {
                mResponse = new HttpResponse<T>(mRequest);
            }
            mResponse.setCanceled(true);
            mResponse.setError(new HttpCancelException("http request has been canceled"));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public @NotNull HttpRequest getRequest() {
        return mRequest;
    }

    @Override
    public @NotNull HttpResponse<T> execute() {
        if (checkCancel()) {
            return mResponse;
        }
        mResponse = new HttpResponse<T>(mRequest);
        try {
            String urlStr = mRequest.getRequestUrl();
            XLog.d(TAG, SEPARATE_LINE);
            XLog.d(TAG, mRequest.getRequestMethod() + " --> " + urlStr);

            // 打开http连接
            URL url = new URL(urlStr);
            mHttpUrlConn = (HttpURLConnection) url.openConnection();
            //设置https的SSL请求处理
            if (mHttpUrlConn instanceof HttpsURLConnection) {
                //获取SSL socket创建对象
                SSLSocketFactory sslFactory = null;
                if (HttpConfig.getDefaultConfig() != null &&
                        HttpConfig.getDefaultConfig().mSSLSocketProvider != null) {
                    sslFactory = HttpConfig.getDefaultConfig().mSSLSocketProvider.getSSLSocketFactory();
                }
                if (sslFactory != null) {
                    //https请求
                    HttpsURLConnection https = (HttpsURLConnection) mHttpUrlConn;
                    https.setSSLSocketFactory(sslFactory);
                }
            }
            // 设置连接超时
            mHttpUrlConn.setConnectTimeout(mRequest.getConnTimeout());
            // 设置读超时
            mHttpUrlConn.setReadTimeout(mRequest.getReadTimeout());
            // 设置方法
            mHttpUrlConn.setRequestMethod(mRequest.getRequestMethod());
            // 设置连接是否可读入数据
            mHttpUrlConn.setDoInput(true);
            //若为POST则允许读写流
            if (mRequest.hasPostContent()) {
                // 设置是否使用缓存
                mHttpUrlConn.setUseCaches(mRequest.isUseCache());
                // 设置连接是否可被重定向
                mHttpUrlConn.setInstanceFollowRedirects(mRequest.isEnableRedirect());
                // 设置连接是否可输出数据
                mHttpUrlConn.setDoOutput(true);
            }
            // 设置请求头
            Map<String, String> requestProperty = mRequest.getHeaders();
            if (requestProperty != null && requestProperty.size() > 0) {
                for (String key : requestProperty.keySet()) {
                    String value = requestProperty.get(key);
                    mHttpUrlConn.setRequestProperty(key, value);
                    // 打印日志
                    XLog.d(TAG, key + ": " + value);
                }
            }

            if (checkCancel()) {
                return mResponse;
            }
            //建立连接
            mHttpUrlConn.connect();
            if (mRequest.hasPostContent()) {
                // post数据
                String content = mRequest.getRequestContent();
                mOutputStream = mHttpUrlConn.getOutputStream();
                mBufferedWriter = new BufferedWriter(new OutputStreamWriter(mOutputStream));
                mBufferedWriter.write(content);
                mBufferedWriter.flush();
                XLog.d(TAG, "body: " + content);
            }
            XLog.d(TAG, "End " + mRequest.getRequestMethod() + " --> " + urlStr);
            XLog.d(TAG, SEPARATE_LINE);

            // 获取返回码
            int responseCode = mHttpUrlConn.getResponseCode();

            mResponse.setCode(responseCode);
            mResponse.setHeaders(mHttpUrlConn.getHeaderFields());

            if (responseCode == HttpURLConnection.HTTP_OK ||
                    responseCode == HttpURLConnection.HTTP_ACCEPTED ||
                    responseCode == HttpURLConnection.HTTP_CREATED) {
                // 读取数据
                mInputStream = mHttpUrlConn.getInputStream();
            } else {
                //错误数据
                mInputStream = mHttpUrlConn.getErrorStream();
            }

            if (checkCancel()) {
                return mResponse;
            }

            String responseStr = "";
            if (mInputStream != null) {
                //TODO:读取数据流,暂时全部读取为文本,如果是文件怎么办?
                mBufferedReader = new BufferedReader(new InputStreamReader(mInputStream));
                String str;
                StringBuilder stringBuilder = new StringBuilder();
                while ((str = mBufferedReader.readLine()) != null) {
                    stringBuilder.append(str);
                }
                responseStr = stringBuilder.toString();
            } else {
                responseStr = "null response stream";
            }
            //保存原始文本
            mResponse.setRawStr(responseStr);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                if (mConverter != null) {
                    try {
                        //转换数据对象
                        T result = mConverter.onResponseConvert(this, mResponse, responseStr);
                        mResponse.setBody(result);
                    } catch (Exception ex) {
                        mResponse.setBody(null);
                        mResponse.setError(ex);
                    }
                }
            } else {
                //请求失败设置错误
                mResponse.setError(new HttpException(responseStr));
            }

            //连接建立并已经请求得到数据时,即已经执行了
            mIsExecuted = true;
            XLog.d(TAG, mRequest.getRequestMethod() + " <-- " + urlStr);
            XLog.d(TAG, mResponse.toString());
            XLog.d(TAG, "END " + mRequest.getRequestMethod() + " <--" + urlStr);
        } catch (Exception ex) {
            XLog.e(TAG, "REQUEST-" + mRequest.getRequestMethod() + " --> " + mRequest.getRequestUrl());
            XLog.e(TAG, ex.getMessage());
            mResponse.setCanceled(mIsSetCancel);
            mResponse.setError(new HttpException(ex));
        } finally {
            closeStream();
        }
        return mResponse;
    }

    @Override
    @Nullable
    public HttpCallback<T> getCallback() {
        return mCallback;
    }

    @Override
    public HttpRunnable<T> setResponseConverter(HttpConvertable<T> coverter) {
        mConverter = coverter;
        return this;
    }

    @Override
    public boolean isCanceled() {
        return mIsSetCancel;
    }

    @Override
    public boolean isExecuted() {
        return mIsExecuted;
    }

    @Override
    public void cancel() {
        if (!mIsSetCancel) {
            String url = mRequest != null ? mRequest.getRequestUrl() : "UNKNOWN_URL";
            XLog.e(TAG, "CANCEL --> " + url);
            //已经处理过取消事件,则不会再处理
            mIsSetCancel = true;
            closeStream();
            //只要回调还存在,进行回调通知
            if (mCallback != null && mIsExecuted) {
                mCallback.onCancel(this, mRequest);
            }
        }
    }

    @Override
    public void enqueue(@Nullable HttpCallback<T> callback) {
        mCallback = callback;
        //默认在并行任务中执行
        HttpExecutor.executeInParallel(this);
    }
}
