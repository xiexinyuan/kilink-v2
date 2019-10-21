/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *    Ian Craggs - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.paho.mqttsn.gateway;

import org.eclipse.paho.mqttsn.gateway.broker.AbstractBrokerConnectionFactory;
import org.eclipse.paho.mqttsn.gateway.broker.BrokerStateListener;
import org.eclipse.paho.mqttsn.gateway.broker.DefaultBrokerConnectionFactory;
import org.eclipse.paho.mqttsn.gateway.core.Dispatcher;
import org.eclipse.paho.mqttsn.gateway.core.GatewayMsgHandler;
import org.eclipse.paho.mqttsn.gateway.exceptions.MqttsException;
import org.eclipse.paho.mqttsn.gateway.messages.Message;
import org.eclipse.paho.mqttsn.gateway.messages.control.ControlMessage;
import org.eclipse.paho.mqttsn.gateway.timer.TimerService;
import org.eclipse.paho.mqttsn.gateway.utils.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Random;

/**
 * This is the entry point of the MQTT-SN Gateway.
 */
public class Gateway {
    private Dispatcher mDispatcher;

    private GWParameters mParameters;
    private GatewayMsgHandler mGatewayHandler;
    private TimerService mTimerService;
    private boolean mStarted;
    private AbstractBrokerConnectionFactory mBrokerFactory;

    private ConnectionStateListener mConnectionStateListener;

    public void start(GWParameters parameters) {
        if (isStarted())
            return;
        GatewayLogger.log(GatewayLogger.INFO, "gate gateway start");

        mParameters = parameters;
        new StartThread(this).start();
    }

    public void connectBroker() {
        if (mGatewayHandler == null) {
            throw new NullPointerException("Please start gateway first");
        }
        if (!isStarted()) {
            throw new IllegalStateException("Please start gateway first");
        }
        GatewayLogger.log(GatewayLogger.INFO, "Gateway connecting to broker.");

        //initialize the GatewayMsgHandler
        mGatewayHandler.initialize(this, new BrokerStateListener() {
            @Override
            public void onConnected(String ip, int port) {
                if (mConnectionStateListener != null) {
                    mConnectionStateListener.onBrokerConnected();
                }
            }

            @Override
            public void onDisconnected(String ip, int port) {
                if (mConnectionStateListener != null) {
                    mConnectionStateListener.onBrokerDisconnected();
                }
            }
        });

        //connect to the broker
        mGatewayHandler.connect();
    }

    public static GWParameters loadParametersFromFile(InputStream confStream, InputStream topicStream) {
        //load the gateway parameters from a file
        try {
            return ConfigurationParser.parseFile(confStream, topicStream);
        } catch (MqttsException e) {
            e.printStackTrace();
            GatewayLogger.log(GatewayLogger.ERROR, "Failed to load Gateway parameters. Gateway cannot start.");
        }
        return null;
    }

    public Dispatcher getDispatcher() {
        return mDispatcher;
    }

    public GWParameters getParameters() {
        return mParameters;
    }

    public TimerService getTimerService() {
        return mTimerService;
    }

    public boolean isStarted() {
        return mStarted;
    }

    public boolean isConnected() {
        return mGatewayHandler != null && mGatewayHandler.isConnected();
    }

    public AbstractBrokerConnectionFactory getBrokerFactory() {
        return mBrokerFactory;
    }

    public void setBrokerFactory(AbstractBrokerConnectionFactory brokerFactory) {
        mBrokerFactory = brokerFactory;
    }

    public void setConnectionStateListener(ConnectionStateListener connectionStateListener) {
        mConnectionStateListener = connectionStateListener;
    }

    public ConnectionStateListener getConnectionStateListener() {
        return mConnectionStateListener;
    }

    private static class StartThread extends Thread {

        private Gateway mGw;

        public StartThread(Gateway gateway) {
            mGw = gateway;
        }

        @Override
        public void run() {
            InetAddress ip = null;
            InetAddress localIp = null;
                /*
                    run in background thread
                 */
            try {
                ip = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
                localIp = mGw.getLocalAddress();
            } catch (IOException e) {
                e.printStackTrace();
                GatewayLogger.log(GatewayLogger.ERROR, "Failed to create the address of the Gateway.Gateway cannot start.");
                if (mGw.mConnectionStateListener != null) {
                    mGw.mConnectionStateListener.onGatewayStopped(mGw);
                }
                return;
            }

            // TODO - check this
            byte gatewayId = localIp.getAddress()[localIp.getAddress().length - 1];
            mGw.getParameters().setGwId(gatewayId);

            if (mGw.mBrokerFactory == null) {
                mGw.mBrokerFactory = new DefaultBrokerConnectionFactory();
            }

            //instantiate the mDispatcher
            mGw.mDispatcher = new Dispatcher();

            //initialize the mDispatcher
            mGw.mDispatcher.initialize(mGw);

            //instantiate the timer service
            mGw.mTimerService = new TimerService(mGw);

            //create the address of the gateway itself(see org.eclipse.paho.mqttsn.gateway.utils.GatewayAdress)
            int len = 1;
            byte[] addr = new byte[len];
            addr[0] = (byte) gatewayId;

            int port = mGw.mParameters.getUdpPort();

            GatewayAddress gatewayAddress = new GatewayAddress(addr, ip, port);
            mGw.mParameters.setGatewayAddress(gatewayAddress);

            //create a new GatewayMsgHandler (for the connection of the gateway itself)
            mGw.mGatewayHandler = new GatewayMsgHandler(mGw.mParameters.getGatewayAddress());

            //insert this handler to the Dispatcher's mapping table
            mGw.mDispatcher.putHandler(mGw.mParameters.getGatewayAddress(), mGw.mGatewayHandler);

            mGw.mStarted = true;
            GatewayLogger.log(GatewayLogger.INFO, "Gateway started.");

            if (mGw.mConnectionStateListener != null) {
                mGw.mConnectionStateListener.onGatewayStarted(mGw);
            }
        }
    }

    /**
     *
     */
    public void stop() {
        if (!isStarted()) {
            return;
        }
        GatewayLogger.log(GatewayLogger.INFO, "stop gate gateway");
        //generate a control message
        ControlMessage controlMsg = new ControlMessage();
        controlMsg.setMsgType(ControlMessage.SHUT_DOWN);

        //construct an "internal" message and put it to mDispatcher's queue
        //@see org.eclipse.paho.mqttsn.gateway.core.Message
        Message msg = new Message(null);
        msg.setType(Message.CONTROL_MSG);
        msg.setControlMessage(controlMsg);
        mDispatcher.putMessage(msg);

        // ANS - 检查是否影响上面的CONTROL_MSG: 不影响，请跟踪代码
        mDispatcher.shutdown();

        mStarted = false;

        if (mConnectionStateListener != null) {
            mConnectionStateListener.onGatewayStopped(this);
        }
    }

//    /**
//     * 以一定间隔激活gateway广播一定次数
//     *
//     * @param times    广播次数
//     * @param interval 广播间隔
//     */
//    public void scheduleAdvertise(int times, int interval) {
//        if (!isStarted()) {
//            throw new IllegalStateException("Please start gateway first");
//        }
//
//        unscheduledAdvertise();
//
//        mAdvertiseTimer = new AdvertiseTimer(this, times);
//        mTimerService.getTimer().scheduleAtFixedRate(
//                mAdvertiseTimer
//                , 0
//                , interval
//        );
//    }

//    public void unscheduledAdvertise() {
//        if (mAdvertiseTimer != null) {
//            mAdvertiseTimer.cancel();
//            mAdvertiseTimer = null;
//        }
//    }

    public void broadcastAdvertiseOnce() {
        if (!isStarted()) {
            throw new IllegalStateException("Please start gateway first");
        }

//        new AdvertiseTimer(this, Integer.MAX_VALUE).run();

        //create new control message
        ControlMessage controlMsg = new ControlMessage();
        controlMsg.setMsgType(ControlMessage.SEND_ADVERTISE_MSG);

        //create an "internal" message
        Message msg = new Message(this.mParameters.getGatewayAddress());
        msg.setType(Message.CONTROL_MSG);
        msg.setControlMessage(controlMsg);

        //put this message to the Dispatcher's queue
        if (this.mDispatcher != null) {
            this.mDispatcher.putMessage(msg);
        }
    }

    @Override
    public String toString() {
        return "Gateway{" +
                "mGatewayHandler=" + mGatewayHandler +
                ", mStarted=" + mStarted +
                '}';
    }

//    private AdvertiseTimer mAdvertiseTimer;

    private InetAddress getLocalAddress() throws IOException {
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                // TODO - what about IPV6?
                if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                    //return inetAddress.getHostAddress().toString();
                    return inetAddress;
                }
            }
        }
        return InetAddress.getByAddress(new byte[]{127, 0, 0, (byte) (new Random().nextInt(255 - 1) + 1)});
    }
    //////////////////////////////////////////////////////////////////////

//    private static class AdvertiseTimer extends TimerTask {
//
//        private Gateway mGateway;
//
//        private int mCounter;
//
//        public AdvertiseTimer(Gateway gateway, int times) {
//            mGateway = gateway;
//            mCounter = times > 0 ? times : Integer.MAX_VALUE;
//        }
//
//        @Override
//        public void run() {
//            //create new control message
//            ControlMessage controlMsg = new ControlMessage();
//            controlMsg.setMsgType(ControlMessage.SEND_ADVERTISE_MSG);
//
//            //create an "internal" message
//            Message msg = new Message(mGateway.mParameters.getGatewayAddress());
//            msg.setType(Message.CONTROL_MSG);
//            msg.setControlMessage(controlMsg);
//
//            //put this message to the Dispatcher's queue
//            if (mGateway.mDispatcher != null) {
//                mGateway.mDispatcher.putMessage(msg);
//            }
//
//            if (mCounter-- < 0) {
//                this.cancel();
//                mGateway.unscheduledAdvertise();
//            }
//        }
//    }

    public interface ConnectionStateListener {

        void onGatewayStarted(Gateway gateway);

        void onGatewayStopped(Gateway gateway);

        void onBrokerConnected( );

        void onBrokerDisconnected( );

        void onClientConnected(ClientAddress address);

        void onClientDisconnected(ClientAddress address);
    }
}
