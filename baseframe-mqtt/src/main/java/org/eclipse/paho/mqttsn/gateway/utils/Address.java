/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corp.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * <p>
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * <p>
 * Contributors:
 * Ian Craggs - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.paho.mqttsn.gateway.utils;

import cn.xlink.sdk.common.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

/**
 * 连接对象的地址信息
 */
public abstract class Address {
    private byte[] mAddress = null;
    private String mIpAddressInfo;
    private InetAddress mIpAddress = null;
    private int mPort = 0;

    /**
     * 创建ip与port的唯一标识
     *
     * @param ip
     * @param port
     * @return
     */
    @NotNull
    public static String generateAddressInfo(String ip, int port) {
        if (StringUtil.isEmpty(ip) || port <= 0) {
            return "";
        } else {
            return "[" + ip + ':' + port + ']';
        }
    }

    public Address(byte[] addr) {
        this(addr, null, 0);
    }

    public Address(byte[] addr, InetAddress ipAddr, int port) {
        this.mAddress = addr;
        this.mIpAddress = ipAddr;
        this.mPort = port;
    }


    /**
     * 判断地址是否相同的,最终需要重写到类的equals()方法
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Address)) return false;
        if (o == this) return true;

        Address targetAddress = (Address) o;
        String targetInfo = targetAddress.getIpAddressInfo();
        String oldInfo = this.getIpAddressInfo();
        //是否相同的地址由ip+port决定,原本只由Address的byte[]数组决定
        if (oldInfo == targetInfo) {
            return true;
        } else if (oldInfo == null || targetInfo == null) {
            return false;
        } else {
            return oldInfo.equals(targetInfo);
        }
    }

    @Override
    public int hashCode() {
        String ipInfo = this.getIpAddressInfo();
        if (ipInfo == null) {
            return 0;
        } else {
            return ipInfo.hashCode();
        }
    }

    @Override
    public String toString() {
        return "Address" + this.getIpAddressInfo();
    }

    /**
     * 设置client的IP地址
     *
     * @param addr
     */
    public void setIPaddress(Address addr) {
        if (addr != null) {
            mIpAddress = addr.getIPaddress();
            mPort = addr.getPort();
            mIpAddressInfo = null;
        }
    }

    /**
     * 获取地址信息, TODO:这里暂时不确定address返回的地址字节数组是什么内容
     *
     * @return
     */
    public byte[] getAddress() {
        return mAddress;
    }

    /**
     * 获取IP地址
     *
     * @return
     */
    public InetAddress getIPaddress() {
        return mIpAddress;
    }

    /**
     * 获取IP
     *
     * @return
     */
    @NotNull
    public String getIp() {
        if (mIpAddress != null) {
            return mIpAddress.getHostAddress() != null ? mIpAddress.getHostAddress() : "";
        }
        return "";
    }

    /**
     * 获取端口号
     *
     * @return
     */
    public int getPort() {
        return mPort;
    }

    /**
     * 获取唯一的IP地址信息,应该是IP+端口号的形式
     *
     * @return
     */
    public String getIpAddressInfo() {
        if (this.mIpAddressInfo == null) {
            this.mIpAddressInfo = generateAddressInfo(getIp(), mPort);
        }
        return this.mIpAddressInfo;
    }
}
