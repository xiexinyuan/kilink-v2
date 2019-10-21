package cn.xlink.sdk.common.http.def;

import cn.xlink.sdk.common.http.HttpConvertable;
import cn.xlink.sdk.common.http.HttpResponse;
import cn.xlink.sdk.common.http.HttpRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Created by taro on 2018/1/15.
 */
public class ObjectConverter implements HttpConvertable {
    @Override
    public Object onResponseConvert(@NotNull HttpRunnable httpRun, @NotNull HttpResponse response, @NotNull String result) {
        return result;
    }
}
