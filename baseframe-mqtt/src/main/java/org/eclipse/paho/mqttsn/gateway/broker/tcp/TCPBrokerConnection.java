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

package org.eclipse.paho.mqttsn.gateway.broker.tcp;

import org.eclipse.paho.mqttsn.gateway.Gateway;
import org.eclipse.paho.mqttsn.gateway.broker.AbstractBrokerConnection;
import org.eclipse.paho.mqttsn.gateway.broker.BrokerStateListener;
import org.eclipse.paho.mqttsn.gateway.exceptions.MqttsException;
import org.eclipse.paho.mqttsn.gateway.messages.Message;
import org.eclipse.paho.mqttsn.gateway.messages.control.ControlMessage;
import org.eclipse.paho.mqttsn.gateway.messages.mqtt.*;
import org.eclipse.paho.mqttsn.gateway.utils.Address;
import org.eclipse.paho.mqttsn.gateway.utils.GatewayLogger;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This class represents the interface to the broker and is instantiated by the
 * MessageHandler.Is is used for opening a TCP/IP connection with the broker
 * and sending/receiving Mqtt Messages.
 * For the reading functionality a reading thread is created.
 * For every client there is one instance of this class.
 * <p>
 * <p>
 * Parts of this code were imported from com.ibm.mqttdirect.modules.common.StreamDeframer.java
 */
public class TCPBrokerConnection extends AbstractBrokerConnection implements Runnable {

    private DataInputStream mStreamIn = null;
    private DataOutputStream mStreamOut = null;
    private Socket mSocket;

    private volatile boolean mRunning;

    private Thread mReadThread;

    //the maximum length of a Mqtt fixed header
    public static final int MAX_HDR_LENGTH = 5;

    //the maximum length of the remaining part of a Mqtt message
    public static final int MAX_MSG_LENGTH = 268435455;


    /**
     * Constructor of the broker interface.
     */
    public TCPBrokerConnection(Address address) {
        super(address);
        this.mRunning = false;
        this.mReadThread = null;
    }

    @Override
    public void connect() throws MqttsException {
        try {
            mSocket = new Socket(getBorkerIp(), getBorkerPort());
            mStreamIn = new DataInputStream(mSocket.getInputStream());
            mStreamOut = new DataOutputStream(mSocket.getOutputStream());

            if (getListener() != null) {
                getListener().onConnected(getBorkerIp(), getBorkerPort());
            }
        } catch (UnknownHostException e) {
            disconnect();
            throw new MqttsException(e.getMessage());
        } catch (IOException e) {
            disconnect();
            throw new MqttsException(e.getMessage());
        }

        //create thread for reading
        this.mReadThread = new Thread(this, "BrokerInterface");
        this.mRunning = true;
        this.mReadThread.start();
    }

    /**
     * This method opens the TCP/IP connection with the broker and creates
     * a new thread for reading from the mSocket.
     *
     * @param gateway
     * @throws MqttsException
     */
    @Override
    public void initialize(Gateway gateway, BrokerStateListener listener) {
        super.initialize(gateway, listener);
    }

    /**
     * This method sends a Mqtt message to the broker over the already established
     * TCP/IP connection.Before that, converts the message to byte array calling
     * the method {@link org.eclipse.paho.mqttsn.gateway.messages.mqtt.MqttMessage#toBytes()}.
     *
     * @param message The MqttMessage to be send to the broker.
     * @throws MqttsException
     */
    public void sendMqttMessage(MqttMessage message) throws MqttsException {
        GatewayLogger.log(GatewayLogger.INFO, "TCPBrokerInterface - sendMqttMessage MqttMessage:" + message.getMsgType());
        // send the message over the TCP/IP mSocket
        if (this.mStreamOut != null) {
            try {
                //System.out.println(">> sending msg: " + Utils.hexString(message.toBytes()));
                this.mStreamOut.write(message.toBytes());
                this.mStreamOut.flush();
            } catch (IOException e) {
                disconnect();
                throw new MqttsException(e.getMessage());
            }
        } else {
            disconnect();
            throw new MqttsException("Writing stream is null!");
        }
    }

    /**
     * This method is used for reading a Mqtt message from the mSocket.It blocks on the
     * reading stream until a message arrives.
     */
    private void readMsg() {
        byte[] body = null;

        // read the header from the input stream
        MqttHeader hdr = new MqttHeader();
        hdr.header = new byte[MAX_HDR_LENGTH];

        if (this.mStreamIn == null) {
            return;
        }

        try {
            int res = mStreamIn.read();
            hdr.header[0] = (byte) res;
            hdr.headerLength = 1;
            if (res == -1) {
                // if EOF detected
                throw new EOFException();
            }
            // read the Mqtt length
            int multiplier = 1;
            hdr.remainingLength = 0;
            do {
                //read MsgLength bytes
                res = mStreamIn.read();
                if (res == -1) {
                    // if EOF detected.
                    throw new EOFException();
                }
                hdr.header[hdr.headerLength++] = (byte) res;
                hdr.remainingLength += (res & 127) * multiplier;
                multiplier *= 128;
            } while ((res & 128) != 0 && hdr.headerLength < MAX_HDR_LENGTH);

            //some checks
            if (hdr.headerLength > MAX_HDR_LENGTH || hdr.remainingLength > MAX_MSG_LENGTH || hdr.remainingLength < 0) {
                GatewayLogger.log(GatewayLogger.WARN, "TCPBrokerInterface " + getAddress().getIpAddressInfo() + " - Not a valid Mqtts message.");
                return;
            }

            body = new byte[hdr.remainingLength + hdr.headerLength];

            for (int i = 0; i < hdr.headerLength; i++) {
                body[i] = hdr.header[i];
            }

            if (hdr.remainingLength >= 0) {
                mStreamIn.readFully(body, hdr.headerLength, hdr.remainingLength);
            }


            //start:just for the testing purposes we simulate here a network delay
//            // TODO This will NOT be included in the final version
//            try {
//                Thread.sleep(20);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
            //end


            if (body != null)
                decodeMsg(body);
        } catch (IOException e) {
            if (e instanceof InterruptedIOException) {
                //do nothing
            } else if (this.mRunning == true) {
                //an error occurred
                //stop the reading thread
                this.mRunning = false;

                //generate a control message
                ControlMessage controlMsg = new ControlMessage();
                controlMsg.setMsgType(ControlMessage.CONNECTION_LOST);

                //construct an "internal" message and put it to mDispatcher's queue
                //@see org.eclipse.paho.mqttsn.gateway.core.Message
                Message msg = new Message(getAddress());
                msg.setType(Message.CONTROL_MSG);
                msg.setControlMessage(controlMsg);
                this.getDispatcher().putMessage(msg);
            }
        }
    }

    /**
     * This method is used for decoding the received Mqtt message from the broker.
     *
     * @param data The Mqtt message as it was received from the mSocket (byte array).
     */
    private void decodeMsg(byte[] data) {
        MqttMessage mqttMsg = null;
        int msgType = (data[0] >>> 4) & 0x0F;
        switch (msgType) {
            case MqttMessage.CONNECT:
                // we will never receive such a message from the broker
                break;

            case MqttMessage.CONNACK:
                mqttMsg = new MqttConnack(data);
                break;

            case MqttMessage.PUBLISH:
                mqttMsg = new MqttPublish(data);
                break;

            case MqttMessage.PUBACK:
                mqttMsg = new MqttPuback(data);
                break;

            case MqttMessage.PUBREC:
                mqttMsg = new MqttPubRec(data);
                break;

            case MqttMessage.PUBREL:
                mqttMsg = new MqttPubRel(data);
                break;

            case MqttMessage.PUBCOMP:
                mqttMsg = new MqttPubComp(data);
                break;

            case MqttMessage.SUBSCRIBE:
                //we will never receive such a message from the broker
                break;

            case MqttMessage.SUBACK:
                mqttMsg = new MqttSuback(data);
                break;

            case MqttMessage.UNSUBSCRIBE:
                //we will never receive such a message from the broker
                break;

            case MqttMessage.UNSUBACK:
                mqttMsg = new MqttUnsuback(data);
                break;

            case MqttMessage.PINGREQ:
                mqttMsg = new MqttPingReq(data);
                break;

            case MqttMessage.PINGRESP:
                mqttMsg = new MqttPingResp(data);
                break;

            case MqttMessage.DISCONNECT:
                //we will never receive such a message from the broker
                break;

            default:
                GatewayLogger.log(GatewayLogger.WARN, "TCPBrokerInterface " + getAddress().getIpAddressInfo() + " - Mqtt message of unknown type \"" + msgType + "\" received.");
                break;
        }

        //construct an "internal" message and put it to mDispatcher's queue
        //@see org.eclipse.paho.mqttsn.gateway.core.Message
        Message msg = new Message(getAddress());
        msg.setType(Message.MQTT_MSG);
        msg.setMqttMessage(mqttMsg);
        this.getDispatcher().putMessage(msg);
    }


    /**
     */
    public void disconnect() {
        //stop the reading thread (if any)
        this.mRunning = false;

        //close the out stream
        if (this.mStreamOut != null) {
            try {
                this.mStreamOut.flush();
                this.mStreamOut.close();
            } catch (IOException e) {
                // ignore it
            }
            this.mStreamOut = null;
        }

        //close the in stream
        if (this.mStreamIn != null) {
            try {
                this.mStreamIn.close();
            } catch (IOException e) {
                // ignore it
            }
            mStreamIn = null;
        }

        //close the mSocket
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
            }
            mSocket = null;
        }
    }


    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        while (mRunning) {
            readMsg();
        }

        if (getListener() != null) {
            getListener().onDisconnected(getBorkerIp(), getBorkerPort());
        }
    }

    @Override
    public void shutdown() {
        setRunning(false);
    }
    /**
     * @param running
     */
    public void setRunning(boolean running) {
        this.mRunning = running;
    }

    /**
     * This class represents a Mqtt header and is used for decoding a Mqtt message
     * from the broker.
     */
    public static class MqttHeader {
        public byte[] header;
        public int remainingLength;
        public int headerLength;
    }
}    	
