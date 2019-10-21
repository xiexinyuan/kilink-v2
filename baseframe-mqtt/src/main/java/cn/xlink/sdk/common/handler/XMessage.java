package cn.xlink.sdk.common.handler;

import java.util.concurrent.ScheduledFuture;

/**
 * Created by link on 2017/7/7.
 */

public class XMessage implements XMessageable {

    public int what;
    public int arg1;
    public int arg2;
    //保存的数据对象
    public Object obj;
    XHandler target;
    //数据内容
    XBundle data;
    //回调事件,一般情况下,回调事件与obj只会有一个存在,处理回调事件则不进行事件本身处理
    Runnable callback;

    ScheduledFuture<?> mScheduledFuture;

    public static XMessage newMessage() {
        return new XMessage();
    }

    @Override
    public String toString() {
        return "Message{" +
                "what=" + what +
                ", arg1=" + arg1 +
                ", arg2=" + arg2 +
                ", obj=" + obj +
                ", data=" + data +
                '}';
    }

    @Override
    public int getMsgId() {
        return what;
    }

    @Override
    public Object getObj() {
        return obj;
    }

    @Override
    public int getArg1() {
        return arg1;
    }

    @Override
    public int getArg2() {
        return arg2;
    }

    @Override
    public Runnable getRunnable() {
        return callback;
    }

    @Override
    public Object getValue(String key) {
        if (data != null) {
            return data.get(key);
        } else {
            return null;
        }
    }

    @Override
    public <T> T getValue(Class<T> clazz, String key, T defaultValue) {
        if (data != null) {
            Object obj = data.get(key);
            if (obj != null && obj.getClass() == clazz) {
                return (T) obj;
            }
        }
        return defaultValue;
    }

    @Override
    public void release() {
        obj = null;
        data = null;
        callback = null;
        target = null;
    }
}
