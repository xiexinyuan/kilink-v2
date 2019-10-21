package cn.xlink.sdk.common.socket;

import cn.xlink.sdk.common.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * UDP后台端口监听服务
 * Created by taro on 2018/1/9.
 */
public class UdpServer implements Runnable {
    //UDP连接通道
    private DatagramSocket mUdpSocket;
    //UDP监听回调接口
    private Set<UdpDataListener> mDataListeners;
    //消息接收读取线程
    private Thread mReadThread;
    //缓存
    private byte[] mRevBuffer;

    private int mPort;
    private boolean mRunning;

    public static UdpServer newServer() {
        return new UdpServer();
    }

    private UdpServer() {
        this.defaultConfig();
    }

    /**
     * 默认配置功能
     */
    private void defaultConfig() {
        //1K的缓存空间,TODO:对于扫描操作实际的数据包很少,但是如果是扩展了协议,这个地方可能需要更新缓存数据容量
        mRevBuffer = new byte[1024];
        mDataListeners = new CopyOnWriteArraySet<>();
        //可以取最小值,无法取最大值
        int port = new Random().nextInt(65535 - 10000) + 10001;
        this.setPort(port);
    }

    /**
     * 设置端口的并检测合法性,端口必须在10000-65535之间,不能取10000
     *
     * @param port
     * @return
     */
    public UdpServer setPort(int port) {
        if (port > 10000 && port <= 65535) {
            mPort = port;
        }
        return this;
    }

    /**
     * 当前是否正常读取消息
     *
     * @return
     */
    public boolean isReadMsgRunning() {
        return mRunning;
    }

    /**
     * 服务是否可用存活
     *
     * @return
     */
    public boolean isServerAlive() {
        return mUdpSocket != null && !mUdpSocket.isClosed();
    }

    /**
     * 添加UDP数据接口监听对象
     *
     * @param listener
     */
    public void addUdpDataListener(UdpDataListener listener) {
        if (listener != null) {
            mDataListeners.add(listener);
        }
    }

    /**
     * 移除UDP数据接口监听对象
     *
     * @param listener
     */
    public void removeUdpDataListener(UdpDataListener listener) {
        if (listener != null) {
            mDataListeners.remove(listener);
        }
    }

    /**
     * 启动服务,可重复调用,仅会启动一次
     */
    public synchronized void start() {
        if (isServerAlive()) {
            //udp存在时,不重复进行Start操作
            return;
        }
        try {
            //create the udp socket
            mUdpSocket = new DatagramSocket(null);
            //设置地址可以重用,防止占用端口
            mUdpSocket.setReuseAddress(true);
            mUdpSocket.setBroadcast(true);
            mUdpSocket.bind(new InetSocketAddress(mPort));

            //create thread for reading
            this.mReadThread = new Thread(this, "UDPClientInterface");
            this.mRunning = true;
            this.mReadThread.start();
        } catch (Exception e) {
            stop();
        }
    }

    /**
     * 停止服务,并关闭UDP通道
     */
    public synchronized void stop() {
        //stop the reading thread (if any)
        this.mRunning = false;

        //close the out stream
        if (this.mUdpSocket != null) {
            this.mUdpSocket.close();
            this.mUdpSocket = null;
        }
    }

    /**
     * 发送消息
     *
     * @param targetIp   目标IP
     * @param targetPort 目标端口
     * @param data       数据内容
     * @return
     */
    public boolean sendMsg(@NotNull String targetIp, int targetPort, @NotNull byte[] data) {
        if (!isServerAlive()) {
            return false;
        }
        //将IP转成地址
        byte[] addressByte = StringUtil.ipToBytes(targetIp);
        if (addressByte.length <= 0) {
            return false;
        }
        try {
            //生成IP地址
            InetAddress address = InetAddress.getByAddress(addressByte);
            //创建数据包
            DatagramPacket packet = new DatagramPacket(data, data.length, address, targetPort);
            //发送
            mUdpSocket.send(packet);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * 读取消息,消息回调也一起在同一个线程
     */
    private void readMsg() {
        DatagramPacket packet = new DatagramPacket(mRevBuffer, 0, mRevBuffer.length);
        try {
            packet.setLength(mRevBuffer.length);
            mUdpSocket.receive(packet);
            //处理消息
            for (UdpDataListener listener : mDataListeners) {
                listener.onRevData(packet);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (mRunning) {
            readMsg();
        }
    }
}
