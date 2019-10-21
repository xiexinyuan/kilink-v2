package cn.xlink.sdk.common.http;

/**
 * 取消请求的异常信息
 * Created by taro on 2018/1/5.
 */
public class HttpCancelException extends HttpException {
    public HttpCancelException() {
    }

    public HttpCancelException(String message) {
        super(message);
    }

    public HttpCancelException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpCancelException(Throwable cause) {
        super(cause);
    }
}
