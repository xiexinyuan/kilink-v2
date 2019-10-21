package cn.xlink.sdk.common.http;

/**
 * http请求异常
 * Created by taro on 2018/1/4.
 */
public class HttpException extends Exception {
    public HttpException() {
    }

    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpException(Throwable cause) {
        super(cause);
    }
}
