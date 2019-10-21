package com.konka.iot.baseframe.common.utils;

import lombok.Data;
import org.apache.http.conn.HttpClientConnectionManager;

/**
 * @Author xiexinyuan
 * @Date 2019-09-09 15:44
 * @Description 定期清理无效的http连接
 **/
@Data
public class IdleConnectionEvictor implements Runnable {

    private HttpClientConnectionManager connMgr;

    private volatile boolean shutdown;

    public IdleConnectionEvictor(HttpClientConnectionManager connMgr) {
        super();
        this.connMgr = connMgr;
        new Thread(this).start();
    }


    @Override
    public void run( ) {
        try {
            while (!shutdown) {
                synchronized (this) {
                    wait(5000);
                    // 关闭失效的连接
                    connMgr.closeExpiredConnections();
                }
            }
        } catch (InterruptedException ex) {
            // 结束
        }
    }

    //关闭清理无效连接的线程
    public void shutdown() {
        shutdown = true;
        synchronized (this) {
            notifyAll();
        }
    }
}
