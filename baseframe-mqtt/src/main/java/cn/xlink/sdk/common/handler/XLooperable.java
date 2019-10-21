package cn.xlink.sdk.common.handler;

/**
 * looper接口
 * Created by taro on 2017/12/7.
 */
public interface XLooperable {
    /**
     * 获取当前looper处理消息所在线程
     *
     * @return
     */
    public Thread getXThread( );

    /**
     * 判断当前的looper处理消息是否在当前的线程<br>
     * 注意在 非Android 环境中,很可能返回值永远是 false
     *
     * @return
     */
    public boolean isCurrentXThread( );

    /**
     * 判断当前looper与给定looper是否同一个
     *
     * @param looper
     * @return
     */
    public boolean isSameXLooperable(XLooperable looper);

    /**
     * 退出当前looper
     */
    public void quitXLooper( );
}
