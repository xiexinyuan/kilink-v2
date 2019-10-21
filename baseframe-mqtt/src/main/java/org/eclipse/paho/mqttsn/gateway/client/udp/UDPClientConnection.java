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

package org.eclipse.paho.mqttsn.gateway.client.udp;

import org.eclipse.paho.mqttsn.gateway.Gateway;
import org.eclipse.paho.mqttsn.gateway.client.ClientConnection;
import org.eclipse.paho.mqttsn.gateway.client.ClientStateListener;
import org.eclipse.paho.mqttsn.gateway.core.Dispatcher;
import org.eclipse.paho.mqttsn.gateway.exceptions.MqttsException;
import org.eclipse.paho.mqttsn.gateway.messages.Message;
import org.eclipse.paho.mqttsn.gateway.messages.mqtts.*;
import org.eclipse.paho.mqttsn.gateway.utils.ClientAddress;
import org.eclipse.paho.mqttsn.gateway.utils.GWParameters;
import org.eclipse.paho.mqttsn.gateway.utils.GatewayLogger;
import org.eclipse.paho.mqttsn.gateway.utils.Utils;

import java.io.IOException;
import java.net.*;
import java.util.Vector;

//import java.net.SocketException;
//import org.eclipse.paho.mqttsn.gateway.client.udp.UDPClientInterface.Forwarder;

/**
 * This class implements a UDP interface to Mqtts clients.Implements the
 * interface {@link ClientConnection}.
 * For the reading functionality a reading thread is created.
 * There is only one instance of this class.
 */
public class UDPClientConnection implements ClientConnection, Runnable {

    private DatagramSocket mUdpSocket;
    private volatile boolean mRunning;
    private Thread mReadThread;
    private Vector<Forwarder> mForwarders;
    private Dispatcher mDispatcher;
    private byte[] recData = new byte[512 * 1024]; // 512k buffer

    private GWParameters mParameters;

    private ClientStateListener mStateListener;

    @Override
    public void connect() throws MqttsException {
        try {
            //create the udp socket
            mUdpSocket = new DatagramSocket(null);
            mUdpSocket.setReuseAddress(true);
            mUdpSocket.setBroadcast(true);
            mUdpSocket.bind(new InetSocketAddress(mParameters.getUdpPort()));

            //create thread for reading
            this.mReadThread = new Thread(this, "UDPClientInterface");
            this.mRunning = true;
            this.mReadThread.start();

            if (mStateListener != null) {
                mStateListener.onConnected(this);
            }
        } catch (Exception e) {
            disconnect();
        }
    }

    @Override
    public void disconnect() {
        //stop the reading thread (if any)
        this.mRunning = false;

        //close the out stream
        if (this.mUdpSocket != null) {
            this.mUdpSocket.close();
            this.mUdpSocket = null;
        }

        if (mStateListener != null) {
            mStateListener.onDisconnected(this);
        }
    }

    /**
     * This method initializes the interface.It creates an new UDP socket and
     * a new thread for reading from the socket.
     *
     * @param gateway
     * @throws MqttsException
     */
    public void initialize(Gateway gateway) throws MqttsException {
        mParameters = gateway.getParameters();
        mDispatcher = gateway.getDispatcher();
        mForwarders = new Vector<Forwarder>();
    }

    public void setStateListener(ClientStateListener stateListener) {
        mStateListener = stateListener;
    }

    /* (non-Javadoc)
         * @see org.eclipse.paho.mqttsn.gateway.client.ClientInterface#broadcastMsg(org.eclipse.paho.mqttsn.gateway.messages.mqtts.MqttsMessage)
         */
    public void broadcastMsg(MqttsMessage msg) {
        if (!mRunning) {
            GatewayLogger.log(GatewayLogger.ERROR, "send adv msg when udp connection not running");
            return;
        }
        // disable forwarder feature for xlink gateway
//        for (int i = mForwarders.size() - 1; i >= 0; i--) {
//            Forwarder fr = (Forwarder) mForwarders.get(i);
//            //check also if this forwarder is inactive
//            if (System.currentTimeMillis() > fr.timeout)
//                mForwarders.remove(i);
//            else {
//                try {
//                    byte[] wireMsg = msg.toBytes();
//                    byte[] data = new byte[wireMsg.length + 2];
//                    data[0] = (byte) 0x00;//0x00 means broadcast to all network
//                    data[1] = (byte) 0x00;
//                    System.arraycopy(wireMsg, 0, data, 2, wireMsg.length);
//                    DatagramPacket packet = new DatagramPacket(data, data.length, fr.addr, fr.port);
//                    mUdpSocket.send(packet);
//                } catch (IOException e) {
//                    GatewayLogger.log(GatewayLogger.ERROR, "UDPClientInterface - Error while writing on the UDP socket.");
//                }
//            }
//        }

        // - ADD - 局域网广播包
        try {
            byte[] wireMsg = msg.toBytes();
            // 广播地址
            InetAddress address = Inet4Address.getByAddress(null, new byte[]{(byte) 255, (byte) 255,
                    (byte) 255, (byte) 255});
            DatagramPacket packet = new DatagramPacket(wireMsg, wireMsg.length, address, mParameters.getBroadcastUdpPort());
            mUdpSocket.send(packet);
            GatewayLogger.log(GatewayLogger.INFO, "UDPClientInterface - Broadcasting Mqtts \"" + Utils.hexString(msg.toBytes()) + "\" message to the network.");
        } catch (IOException e) {
            GatewayLogger.log(GatewayLogger.ERROR, "UDPClientInterface - Error while writing on the UDP socket. e=" + e);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.paho.mqttsn.gateway.client.ClientInterface#broadcastMsg(int, org.eclipse.paho.mqttsn.gateway.messages.mqtts.MqttsMessage)
     */
    public void broadcastMsg(int radius, MqttsMessage msg) {
        //		GatewayLogger.log(GatewayLogger.INFO, "UDPClientInterface - Broadcasting Mqtts \"" +Utils.hexString(msg.toBytes())+"\" message to the network with broadcast radius "+radius+".");
        if (!mRunning) {
            return;
        }
        for (int i = mForwarders.size() - 1; i >= 0; i--) {
            Forwarder fr = (Forwarder) mForwarders.get(i);
            //check also if this forwarder is inactive
            if (System.currentTimeMillis() > fr.timeout)
                mForwarders.remove(i);
            else {
                try {
                    byte[] wireMsg = msg.toBytes();
                    byte[] data = new byte[wireMsg.length + 2];
                    data[0] = (byte) radius;//broadcast to the specified radius
                    data[1] = (byte) 0x00;
                    System.arraycopy(wireMsg, 0, data, 2, wireMsg.length);
                    DatagramPacket packet = new DatagramPacket(data, data.length, fr.addr, fr.port);
                    mUdpSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                    GatewayLogger.log(GatewayLogger.ERROR, "UDPClientInterface - Error while writing on the UDP socket.");
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.paho.mqttsn.gateway.client.ClientInterface#readMsg()
     */
    public void readMsg() {
        DatagramPacket packet = new DatagramPacket(recData, 0, recData.length);
        try {
            packet.setLength(recData.length);
            mUdpSocket.receive(packet);

            //add the forwarder from which we received the message to the list
            //if it is already on the list just update its timeout
            Forwarder forw = new Forwarder();
            forw.addr = packet.getAddress();
            forw.port = packet.getPort();
            forw.timeout = System.currentTimeMillis() + mParameters.getForwarderTimeout() * 1000;
//            GatewayLogger.log(GatewayLogger.INFO, "UDPClientInterface - new packet:addr = " + packet.getAddress() + " port = " + packet.getPort() + " data= " + ByteUtil.bytesToHex(packet.getData()));

            boolean found = false;
            for (int i = 0; i < mForwarders.size(); i++) {
                Forwarder fr = (Forwarder) mForwarders.get(i);
                if (forw.equals(fr)) {
                    found = true;
                    fr.timeout = System.currentTimeMillis() + mParameters.getForwarderTimeout() * 1000;
                    break;
                }
            }
            if (!found) mForwarders.add(forw);

//			if(packet.getLength() > 3) { // not a keep alive packet
            byte[] data = new byte[packet.getLength()];
            System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());

            //old encaps v1.1
//				byte[] clAddr = new byte[data[1]];  //data[1] contains length of clAddr (wireless node id)
//				System.arraycopy(data, 2, clAddr, 0, clAddr.length);
//				ClientAddress address = new ClientAddress(clAddr, packet.getAddress(), packet.getPort());
//				byte[] mqttsData = new byte[data.length - clAddr.length - 2];
//				System.arraycopy(data, clAddr.length + 2, mqttsData, 0, mqttsData.length);
            //end old encaps v1.1

            byte[] mqttsData = null;
            ClientAddress address = null;

            if (data[0] == (byte) 0x00) {  //old encaps v 1.1
                byte[] clAddr = new byte[data[1]];  //data[1] contains length of clAddr (wireless node id)
                System.arraycopy(data, 2, clAddr, 0, clAddr.length);
                byte[] encaps = new byte[data[1] + 2];
                System.arraycopy(data, 0, encaps, 0, encaps.length);
                address = new ClientAddress(clAddr, packet.getAddress(), packet.getPort(), true, encaps);
                mqttsData = new byte[data.length - clAddr.length - 2];
                System.arraycopy(data, clAddr.length + 2, mqttsData, 0, mqttsData.length);
            } else if (data[1] == (byte) MqttsMessage.ENCAPSMSG) { //new encaps v1.2
                //we have an encapsulated msg
                byte[] clAddr = new byte[(data[0] & 0xFF) - 3];  //data[0]: length of encaps
                System.arraycopy(data, 3, clAddr, 0, clAddr.length);
                byte[] encaps = new byte[data[0] & 0xff];
                System.arraycopy(data, 0, encaps, 0, encaps.length);
                address = new ClientAddress(clAddr, packet.getAddress(), packet.getPort(), true, encaps);
                mqttsData = new byte[data[data[0]] & 0xff];
                System.arraycopy(data, data[0] & 0xff, mqttsData, 0, mqttsData.length);
            } else {
                //we have a non-encapsulated mqtts msg
                //we will create an address out of the forwarder address
                byte[] a1 = packet.getAddress().getAddress();
                byte[] a2 = new byte[2];
                a2[0] = (byte) ((packet.getPort() >> 8) & 0xFF);
                a2[1] = (byte) (packet.getPort() & 0xFF);
                byte[] clAddr = new byte[a1.length + a2.length];
                System.arraycopy(a1, 0, clAddr, 0, a1.length);
                System.arraycopy(a2, 0, clAddr, a1.length, a2.length);
                address = new ClientAddress(clAddr, packet.getAddress(), packet.getPort(), false, null);

                if ((data[0] & 0xff) < 0) {
                    GatewayLogger.log(GatewayLogger.ERROR, "UDPClientInterface - invalid mqtts data. Ignore it");
                    return;
                }
                int mqttsDataLen = data[0] & 0xff;
                int srcPos = 0;
                if (mqttsDataLen == 0x01) {
                    if (data.length < 3) {
                        GatewayLogger.log(GatewayLogger.ERROR, "UDPClientInterface - invalid mqtts data len=" + data.length + ". Ignore it");
                        return;
                    }
                    mqttsDataLen = ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                    srcPos = 2;
                }

                mqttsData = new byte[mqttsDataLen - srcPos];
                System.arraycopy(data, srcPos, mqttsData, 0, mqttsData.length);
            }


//start-just for the testing purposes we simulate here a network delay
//This will NOT be included in the final version
//				try {
//					Thread.sleep(10);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}

            //end
            decodeMsg(mqttsData, address);
//			}
        } catch (Exception ex) {
            ex.printStackTrace();
            GatewayLogger.log(GatewayLogger.ERROR, "UDPClientInterface - An I/O error occurred while reading from the socket.");
            //TODO:解析出错时需要断开连接?
//            if (mRunning) {
//                disconnect();
//            }
        }
    }

    /**
     * This method decodes the received Mqtts message and then constructs a
     * general "internal" message {@link org.eclipse.paho.mqttsn.gateway.messages.Message}
     * which puts it to Dispatcher's queue.
     *
     * @param data    The received Mqtts packet.
     * @param address The address of the SA client.
     */
    public void decodeMsg(byte[] data, ClientAddress address) {
        MqttsMessage mqttsMsg = null;

        //do some checks for the received packet
        if (data == null) {
            GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - The received data packet is null. The packet cannot be processed.");
            return;
        }

        if (data.length < mParameters.getMinMqttsLength()) {
            GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts message. The received data packet is too short (length = " + data.length + "). The packet cannot be processed.");
            return;
        }

        if (data.length > mParameters.getMaxMqttsLength()) {
            GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts message. The received data packet is too long (length = " + data.length + "). The packet cannot be processed.");
            return;

        }

        if (data.length < mParameters.getMinMqttsLength()) {
            GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts message. Field \"Length\" in the received data packet is less than " + mParameters.getMinMqttsLength() + " . The packet cannot be processed.");
            return;
        }

        if ((data[1] & 0xFF) == MqttsMessage.ADVERTISE) {
            return;
        } else {
            GatewayLogger.log(GatewayLogger.INFO, "[ " + address.getIPaddress().getHostAddress() + "] <=== recv udp packet:" + Utils.hexString(data));
        }

        int msgType = (data[1] & 0xFF);
        switch (msgType) {
            case MqttsMessage.ADVERTISE:
                if (data.length != 5) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts ADVERTISE message. Wrong packet length (length = " + data.length + "). The packet cannot be processed.");
                    return;
                }
                mqttsMsg = new MqttsAdvertise(data);
                // TODO Handle this case for load balancing issues
                break;

            case MqttsMessage.SEARCHGW:
                if (data.length != 3) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts SEARCHGW message. Wrong packet length (length = " + data.length + "). The packet cannot be processed.");
                    return;
                }
                mqttsMsg = new MqttsSearchGW(data);
                break;

            case MqttsMessage.GWINFO:
                mqttsMsg = new MqttsGWInfo(data);
                //TODO Handle this case for load balancing issues
                break;

            case MqttsMessage.CONNECT:
                if (data.length < 7) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts CONNECT message. Wrong packet length (length = " + data.length + "). The packet cannot be processed.");
                    return;
                }
                mqttsMsg = new MqttsConnect(data);
                break;

            case MqttsMessage.CONNACK:
                //we will never receive such a message from the client
                break;

            case MqttsMessage.WILLTOPICREQ:
                //we will never receive such a message from the client
                break;

            case MqttsMessage.WILLTOPIC:
                if (data.length < 2) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts WILLTOPIC message. Wrong packet length (length = " + data.length + "). The packet cannot be processed.");
                    return;
                }
                mqttsMsg = new MqttsWillTopic(data);
                break;

            case MqttsMessage.WILLMSGREQ:
                //we will never receive such a message from the client
                break;

            case MqttsMessage.WILLMSG:
                if (data.length < 3) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts WILLMSG message. Wrong packet length (length = " + data.length + "). The packet cannot be processed.");
                    return;
                }
                mqttsMsg = new MqttsWillMsg(data);
                break;

            case MqttsMessage.REGISTER:
                if (data.length < 7) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts REGISTER message. Wrong packet length (length = " + data.length + "). The packet cannot be processed.");
                    return;
                }
                mqttsMsg = new MqttsRegister(data);
                break;

            case MqttsMessage.REGACK:
                if (data.length != 7) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts REGACK message. Wrong packet length (length = " + data.length + "). The packet cannot be processed.");
                    return;
                }
                mqttsMsg = new MqttsRegack(data);
                break;

            case MqttsMessage.PUBLISH:
                if (data.length < 8) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts PUBLISH message. Wrong packet length (length = " + data.length + "). The packet cannot be processed.");
                    return;
                }
                mqttsMsg = new MqttsPublish(data);
                break;

            case MqttsMessage.PUBACK:
                if (data.length != 7) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts PUBACK message. Wrong packet length (length = " + data.length + "). The packet cannot be processed.");
                    return;
                }
                mqttsMsg = new MqttsPuback(data);
                break;

            case MqttsMessage.PUBCOMP:
                if (data.length != 4) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts PUBCOMP message. Wrong packet length (length = " + data.length + "). The packet cannot be processed.");
                    return;
                }
                mqttsMsg = new MqttsPubComp(data);
                break;

            case MqttsMessage.PUBREC:
                if (data.length != 4) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts PUBREC message. Wrong packet length (length = " + data.length + "). The packet cannot be processed.");
                    return;
                }
                mqttsMsg = new MqttsPubRec(data);
                break;

            case MqttsMessage.PUBREL:
                if (data.length != 4) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts PUBREL message. Wrong packet length (length = " + data.length + "). The packet cannot be processed.");
                    return;
                }
                mqttsMsg = new MqttsPubRel(data);
                break;

            case MqttsMessage.SUBSCRIBE:
                if (data.length < 6) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts SUBSCRIBE message. Wrong packet length (length = " + data.length + "). The packet cannot be processed.");
                    return;
                }

                try {
                    mqttsMsg = new MqttsSubscribe(data);
                } catch (MqttsException e) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts SUBSCRIBE message. " + e.getMessage());
                    return;
                }
                break;

            case MqttsMessage.SUBACK:
                //we will never receive such a message from the client
                break;

            case MqttsMessage.UNSUBSCRIBE:
                if (data.length < 6) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts UNSUBSCRIBE message. Wrong packet length (length = " + data.length + "). The packet cannot be processed.");
                    return;
                }

                try {
                    mqttsMsg = new MqttsUnsubscribe(data);
                } catch (MqttsException e) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts UNSUBSCRIBE message. " + e.getMessage());
                    return;
                }
                break;

            case MqttsMessage.UNSUBACK:
                //we will never receive such a message from the client
                break;

            case MqttsMessage.PINGREQ:
                mqttsMsg = new MqttsPingReq(data);
                break;

            case MqttsMessage.PINGRESP:
                mqttsMsg = new MqttsPingResp(data);
                break;

            case MqttsMessage.DISCONNECT:
                mqttsMsg = new MqttsDisconnect(data);
                break;

            case MqttsMessage.WILLTOPICUPD:
                if (data.length < 2) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts WILLTOPICUPD message. Wrong packet length (length = " + data.length + "). The packet cannot be processed.");
                    return;
                }

                mqttsMsg = new MqttsWillTopicUpd(data);
                break;

            case MqttsMessage.WILLTOPICRESP:
                //we will never receive such a message from the client
                break;

            case MqttsMessage.WILLMSGUPD:
                if (data.length < 3) {
                    GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Not a valid Mqtts WILLMSGUPD message. Wrong packet length (length = " + data.length + "). The packet cannot be processed.");
                    return;
                }

                mqttsMsg = new MqttsWillMsgUpd(data);
                break;

            case MqttsMessage.WILLMSGRESP:
                //we will never receive such a message from the client
                break;

            default:
                GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - Mqtts message of unknown type \"" + msgType + "\" received.");
                return;
        }

        //construct an "internal" message and put it to mDispatcher's queue
        Message msg = new Message(address);
        msg.setType(Message.MQTTS_MSG);
        msg.setMqttsMessage(mqttsMsg);
        msg.setClientConnection(this);
        this.mDispatcher.putMessage(msg);
    }

    /* (non-Javadoc)
     * @see org.eclipse.paho.mqttsn.gateway.client.ClientInterface#sendMsg(org.eclipse.paho.mqttsn.gateway.utils.SAaddress, org.eclipse.paho.mqttsn.gateway.messages.mqtts.MqttsMessage)
     */
    public void sendMsg(ClientAddress address, MqttsMessage msg) {
        //		GatewayLogger.log(GatewayLogger.INFO, "UDPClientInterface - Sending Mqtts \"" + Utils.hexString(msg.toBytes())+ "\" message to the client with address \"" +Utils.hexString(address.getAddress())+"\".");

        if (address == null) {
            GatewayLogger.log(GatewayLogger.WARN, "UDPClientInterface - The address of the receiver is null.The Mqtts message " + Utils.hexString(msg.toBytes()) + " cannot be sent.");
            return;
        }

        if (!mRunning || mUdpSocket == null)
            return;

        try {
            byte[] addr = address.getAddress();
            byte[] wireMsg = msg.toBytes();

            //old encaps v1.1
//			byte[] data = new byte[wireMsg.length + addr.length + 2];
//			data[0] = (byte)0x00;
//			data[1] = (byte)addr.length;
//			System.arraycopy(addr, 0, data, 2, addr.length);
//			System.arraycopy(wireMsg,  0, data, addr.length + 2, wireMsg.length);
            //end old encaps v1.1

            //new encaps v1.2
            byte[] data = null;
            if (address.isEncaps()) {
//				byte[] encaps = new byte[3+addr.length];
//				encaps[0] = (byte)(addr.length + 3);
//				encaps[1] = (byte) MqttsMessage.ENCAPSMSG;
//				encaps[2] = 0x00;
                byte[] encaps = address.getEncaps();
                System.arraycopy(addr, 0, encaps, 0, addr.length);
                data = new byte[encaps.length + wireMsg.length];
                System.arraycopy(encaps, 0, data, 0, encaps.length);
                System.arraycopy(wireMsg, 0, data, encaps.length, wireMsg.length);
            } else {
                data = wireMsg;
            }
            //end new encaps v1.2

            GatewayLogger.log(GatewayLogger.INFO, "[ " + address.getIPaddress().getHostAddress() + "] ===> send udp packet:" + Utils.hexString(data));

            DatagramPacket packet = new DatagramPacket(data, data.length, address.getIPaddress(), address.getPort());
            mUdpSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
            GatewayLogger.log(GatewayLogger.ERROR, "UDPClientInterface - Error while writing on the UDP socket.");
        }
    }


    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        while (mRunning) {
            readMsg();
        }
    }


    /**
     * This class represents a forwarder which is defined in the specifications of the
     * Mqtts protocol.
     */
    public static class Forwarder {
        private InetAddress addr = null;
        private int port = 0;
        private long timeout = 0;

        public boolean equals(Object o) {
            boolean same = false;
            if (o == null) {
                same = false;
            } else if (o instanceof Forwarder) {
                Forwarder fr = (Forwarder) o;
                if (addr != null && addr.equals(fr.addr) && fr.port == port) {
                    same = true;
                }
            }
            return same;
        }
    }
}