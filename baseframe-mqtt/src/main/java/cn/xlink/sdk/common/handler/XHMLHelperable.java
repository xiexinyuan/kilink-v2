package cn.xlink.sdk.common.handler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * handler处理的辅助类,所有的handler的使用和创建都从此类开始
 * Created by taro on 2017/12/7.
 */
public interface XHMLHelperable {
    /**
     * 获取一个handler对象
     *
     * @param looper 用于初始化handler的looper对象
     * @return 返回值不为null, 但参数不正确时会抛出异常,使用默认的
     */
    @NotNull
    public XHandlerable getHandlerable(@Nullable XLooperable looper);

    /**
     * 获取一个新的消息对象
     *
     * @param msgId 消息ID
     * @return
     */
    @NotNull
    public XMessageable getMessageable(int msgId);

    /**
     * 获取一个新的消息对象
     *
     * @param msgId 消息ID
     * @param obj   消息附加内容
     * @return
     */
    @NotNull
    public XMessageable getMessageable(int msgId, Object obj);

    /**
     * 获取一个消息对象
     *
     * @param msgId 消息ID
     * @param obj   消息附加内容
     * @param run   消息回调
     * @param data  数据内容,请注意如果回调任务存在,实际上数据是不会被使用到的
     * @return
     */
    @NotNull
    public XMessageable getMessageable(int msgId, @Nullable Object obj, @Nullable Runnable run,
                                       @Nullable XBundle data);

    /**
     * 获取一个消息对象
     *
     * @param msgId 消息ID
     * @param obj   消息附加内容
     * @param arg1  消息附加参数
     * @param arg2  消息附加参数2
     * @return
     */
    @NotNull
    public XMessageable getMessageable(int msgId, @Nullable Object obj, int arg1, int arg2);

    /**
     * 获取一个消息对象
     *
     * @param msgId 消息ID
     * @param obj   消息附加内容
     * @param arg1  消息附加参数
     * @param arg2  消息附加参数2
     * @param run   消息回调
     * @param data  数据内容,请注意如果回调任务存在,实际上数据是不会被使用到的
     * @return
     */
    @NotNull
    public XMessageable getMessageable(int msgId, @Nullable Object obj, int arg1, int arg2,
                                       @Nullable Runnable run, @Nullable XBundle data);

    /**
     * 获取当前线程所关联的新looper
     *
     * @return
     */
    @NotNull
    public XLooperable newThreadLooperable( );

    /**
     * 获取一个新的looper
     *
     * @return
     */
    @NotNull
    public XLooperable newIndependentLooperable( );

    /**
     * 获取主线程的looper,在 非android 环境,此方法返回的looper是受到开发者调用的位置所影响的.
     *
     * @return
     */
    @NotNull
    public XLooperable getMainLooperable( );

    /**
     * 获取当前的线程处理的looper
     *
     * @return
     */
    @Nullable
    public XLooperable getCurrentThreadLooper( );

    /**
     * 预处理looper的handler,在获取某个handler和looper绑定之后,需要再调用一次此方法进行预处理
     *
     * @param handler
     * @param looper
     * @return
     */
    public Object prepareLooperable(@Nullable XHandlerable handler, @Nullable XLooperable looper);
}
