package cn.xlink.sdk.common.handler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by taro on 2017/12/7.
 */
public final class XLinkHandlerHelper implements XHMLHelperable {
    //自定义桥接使用的接口
    XHMLHelperable mHelperable;

    private XLinkHandlerHelper() {
    }

    /**
     * 获取唯一实例对象
     *
     * @return
     */
    public static XLinkHandlerHelper getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 设置自定义处理的handlerHelper
     *
     * @param helperable
     */
    public void setHandlerHelperable(XHMLHelperable helperable) {
        mHelperable = helperable;
    }

    @Override
    @NotNull
    public XHandlerable getHandlerable(XLooperable looper) {
        if (mHelperable != null) {
            return mHelperable.getHandlerable(looper);
        } else {
            if (looper != null && looper instanceof XLooper) {
                return new XHandler((XLooper) looper);
            } else {
                throw new IllegalArgumentException("default handler can only accept a default looper");
            }
        }
    }

    @Override
    @NotNull
    public XMessageable getMessageable(int msgId) {
        return getMessageable(msgId, null, null, null);
    }

    @Override
    @NotNull
    public XMessageable getMessageable(int msgId, Object obj) {
        return getMessageable(msgId, obj, null, null);
    }

    @Override
    @NotNull
    public XMessageable getMessageable(int msgId, Object obj, Runnable run, XBundle data) {
        return getMessageable(msgId, obj, 0, 0, run, data);
    }

    @Override
    public @NotNull XMessageable getMessageable(int msgId, @Nullable Object obj, int arg1, int arg2) {
        return getMessageable(msgId, obj, arg1, arg2, null, null);
    }

    @Override
    public @NotNull XMessageable getMessageable(int msgId, @Nullable Object obj, int arg1, int arg2, @Nullable Runnable run, @Nullable XBundle data) {
        if (mHelperable != null) {
            return mHelperable.getMessageable(msgId, obj, run, data);
        } else {
            XMessage xMsg = XMessage.newMessage();
            xMsg.what = msgId;
            xMsg.obj = obj;
            xMsg.arg1 = arg1;
            xMsg.arg2 = arg2;
            xMsg.callback = run;
            //传递的数据只有在callback不存在时才会传递过去,否则不会传递任何的数据内容
            //存在callback的情况下,只会运行callback
            if (run == null &&
                    data != null && data.size() > 0) {
                xMsg.data = data;
            }
            return xMsg;
        }
    }

    @Override
    @NotNull
    public XLooperable newThreadLooperable() {
        if (mHelperable != null) {
            return mHelperable.newThreadLooperable();
        } else {
            return XLooper.newLooper();
        }
    }

    @Override
    @NotNull
    public XLooperable newIndependentLooperable() {
        return new XLooper();
    }

    @Override
    @NotNull
    public XLooperable getMainLooperable() {
        if (mHelperable != null) {
            return mHelperable.getMainLooperable();
        } else {
            return XLooper.getMainLooper();
        }
    }

    @Override
    public XLooperable getCurrentThreadLooper() {
        if (mHelperable != null) {
            return mHelperable.getCurrentThreadLooper();
        } else {
            return XLooper.myLooper();
        }
    }

    @Override
    public Object prepareLooperable(@Nullable XHandlerable handler, @Nullable XLooperable looper) {
        //默认的looper 与handler 并不需要进行任何的预处理操作
        return null;
    }

    private static final class Holder {
        private static final XLinkHandlerHelper INSTANCE = new XLinkHandlerHelper();
    }
}
