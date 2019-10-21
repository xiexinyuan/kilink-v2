package cn.xlink.sdk.common.handler;

import org.jetbrains.annotations.NotNull;

/**
 * 处理消息的行为回调
 * Created by taro on 2017/12/7.
 */
public interface XMsgHandleAction {
    /**
     * 处理消息
     *
     * @param handlerable 处理当前消息的handler
     * @param msg         消息对象
     * @return 消息处理成功返回true, 否则返回false
     */
    public boolean handleMessage(@NotNull XHandlerable handlerable, @NotNull XMessageable msg);
}
