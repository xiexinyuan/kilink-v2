package cn.xlink.sdk.common.http.def;

import cn.xlink.sdk.common.http.HttpConvertable;
import cn.xlink.sdk.common.http.HttpResponse;
import cn.xlink.sdk.common.http.HttpRunnable;

/**
 * Created by taro on 2018/1/15.
 */
public class StringConverter implements HttpConvertable<String> {
    @Override
    public String onResponseConvert(HttpRunnable<String> httpRun, HttpResponse<String> response, String result) {
        return result;
    }
}
