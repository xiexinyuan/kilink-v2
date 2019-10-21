package cn.xlink.sdk.common.handler;

/**
 * 消息接口
 * Created by taro on 2017/12/7.
 */
public interface XMessageable {
    /**
     * 获取当前的消息ID
     *
     * @return
     */
    public int getMsgId( );

    /**
     * 获取当前消息推带的对象
     *
     * @return
     */
    public Object getObj( );

    /**
     * 获取参数1
     *
     * @return
     */
    public int getArg1( );

    /**
     * 获取参数2
     *
     * @return
     */
    public int getArg2( );

    /**
     * 获取当前消息携带的callback
     *
     * @return
     */
    public Runnable getRunnable( );

    /**
     * 获取当前消息保存的数据
     *
     * @param key 映射数据的key
     * @return
     */
    public Object getValue(String key);

    /**
     * 获取当前消息保存的数据
     *
     * @param clazz        数据类型
     * @param key          映射数据的key
     * @param defaultValue 默认值
     * @param <T>          数据具体类型
     * @return
     */
    public <T> T getValue(Class<T> clazz, String key, T defaultValue);

    /**
     * 释放消息资源
     */
    public void release( );
}
