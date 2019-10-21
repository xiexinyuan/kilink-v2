package cn.xlink.sdk.common.handler;

import org.jetbrains.annotations.NotNull;

/**
 * handler接口,定义了handler的一些必要操作
 * Created by taro on 2017/12/7.
 */
public interface XHandlerable {
    /**
     * 获取当前handler消息所在处理的looper
     *
     * @return
     */
    public XLooperable getXLooper( );

    /**
     * 释放资源,回收当前的handler,此处应该把所有需要处理的消息也都清除掉
     */
    public void releaseXHandler( );

    /**
     * 是否有当前的消息
     *
     * @param msgWhat
     * @return
     */
    public boolean hasXMessage(int msgWhat);

    /**
     * 移除消息
     *
     * @param msgWhat 消息ID
     */
    public void removeXMessages(int msgWhat);

    /**
     * 移除消息及其回调
     *
     * @param msgObj 这里的Token用于匹配{@link XMessageable#getObj()},相匹配的消息将会被移除掉
     */
    public void removeCallbacksAndXMessages(Object msgObj);

    /**
     * 立即发送消息消息进行处理
     *
     * @param msg 消息对象,不可为null
     */
    public void sendXMessage(@NotNull XMessageable msg);

    /**
     * 立即发送一条仅有消息ID的消息进行处理
     *
     * @param what 消息ID
     */
    public void sendEmptyXMessage(int what);

    /**
     * 提交一个回调任务进行处理,该处理不会通过 {@link XMsgHandleAction#handleMessage(XHandlerable, XMessageable)} 进行回调
     *
     * @param runnable 回调任务
     */
    public void postXRunnable(Runnable runnable);

    /**
     * 延迟提交一个回调任务进行处理
     *
     * @param runnable   回调任务
     * @param delayMills 延迟时间,毫秒
     */
    public void postXRunnableDelayed(Runnable runnable, long delayMills);

    /**
     * 延迟提交一个带标识的回调任务(可用于取消该任务)
     *
     * @param runnable   回调任务
     * @param token      标识
     * @param delayMills 延迟时间,毫秒
     */
    public void postXRunnableDelayed(Runnable runnable, Object token, long delayMills);

    /**
     * 延迟发送一个消息进行处理
     *
     * @param msg   消息
     * @param delay 延迟时间
     */
    public void sendXMessageDelayed(@NotNull XMessageable msg, long delay);

    /**
     * 设置回调处理行为
     *
     * @param action 行为
     */
    public void setXHandleMsgAction(XMsgHandleAction action);
}
