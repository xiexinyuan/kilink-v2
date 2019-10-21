package cn.xlink.sdk.common.socket;

import org.jetbrains.annotations.NotNull;

import java.net.DatagramPacket;

/**
 * UDP数据接收监听
 * Created by taro on 2018/1/9.
 */
public interface UdpDataListener {
    /**
     * 监听数据接收
     *
     * @param packet UDP数据包
     */
    public void onRevData(@NotNull DatagramPacket packet);
}
