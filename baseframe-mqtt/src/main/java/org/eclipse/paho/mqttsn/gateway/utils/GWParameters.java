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

package org.eclipse.paho.mqttsn.gateway.utils;

import org.eclipse.paho.mqttsn.gateway.client.ClientConnection;
import org.eclipse.paho.mqttsn.gateway.client.udp.UDPClientConnection;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

public class GWParameters {

    //the ID of the gateway (from 0 up to 255)
    private int gwId;

    //the address of the gateway
    private GatewayAddress gatewayAddress;

    //the period (in seconds) of broadcasting the Mqtts ADVERTISE message to the network
    private int advPeriod;

    //the period (in seconds) of sending the Mqtt PINGRESP message to the broker
    private int keepAlivePeriod;

    //maximum retries of sending a message to the client
    private int maxRetries;

    //maximum time (in seconds) waiting for a message from the client
    private int waitingTime;

    //the maximum number of predefined topic ids
    private int predfTopicIdSize;

    //the maximum length of the Mqtts message
    private int maxMqttsLength;

    //the minimum length of the Mqtts message
    private int minMqttsLength;

    //the IP address of the gateway
    private InetAddress ipAddress;

    //the URL of the gateway
    private String gatewayURL;

    //the UDP port that will be used for the UDP socket of the ClientIPInterface
    private int udpPort;

    //the UDP port that send broadcast
    private int broadcastUdpPort;

    //the URL of the broker
    private String brokerURL;

    //the TCP port where broker listens
    private int brokerTcpPort;

    //serial port parameters
    private String serialPortURL;

    //the time (in seconds) that a ClientMsgHandler can remain inactive
    private long handlerTimeout;

    //the time (in seconds) that a Forwarder can remain inactive (we don't have any message)
    private long forwarderTimeout;

    //the period (in seconds) that a control message is sent to all ClientMsgHandlers for removing
    //themselves from Dispatcher's mapping table if they are inactive for at least handlerTimeout seconds
    private long ckeckingPeriod;

    //a String for storing the names of all available client interfaces
    private String clientIntString;

    //a vector for storing all available client interfaces
    private Vector<ClientConnection> mClientInterfacesVector = new Vector<>();

    //a hashtable for storing predefined topic ids
    private Map<Integer, String> predefTopicIdTable = new HashMap<>();


    //other parameters of the Mqtt CONNECT message that GatewayMsgHandler sends to the broker
    private String protocolName;
    private int protocolVersion;
    private boolean retain;
    private int willQoS;
    private boolean willFlag;
    private boolean cleanSession;
    private String willTopic;
    private String willMessage;

    private GWParameters(Builder builder) {
        setGwId(builder.gwId);
        setGatewayAddress(builder.gatewayAddress);
        setAdvPeriod(builder.advPeriod);
        setKeepAlivePeriod(builder.keepAlivePeriod);
        setMaxRetries(builder.maxRetries);
        setWaitingTime(builder.waitingTime);
        setPredfTopicIdSize(builder.predfTopicIdSize);
        setMaxMqttsLength(builder.maxMqttsLength);
        setMinMqttsLength(builder.minMqttsLength);
        setIpAddress(builder.ipAddress);
        setGatewayURL(builder.gatewayURL);
        if (builder.udpPort <= 0) {
            setUdpPort(new Random().nextInt(65535 - 1000) + 1000);
        } else {
            setUdpPort(builder.udpPort);
        }
        setBroadcastUdpPort(builder.broadcastUdpPort);
        setBrokerURL(builder.brokerURL);
        setBrokerTcpPort(builder.brokerTcpPort);
        setSerialPortURL(builder.serialPortURL);
        setHandlerTimeout(builder.handlerTimeout);
        setForwarderTimeout(builder.forwarderTimeout);
        setCkeckingPeriod(builder.checkingPeriod);
        setClientIntString(builder.clientIntString);
        setClientInterfacesVector(builder.mClientInterfacesVector);
        setPredefTopicIdTable(builder.predefTopicIdTable);
        setProtocolName(builder.protocolName);
        setProtocolVersion(builder.protocolVersion);
        setRetain(builder.retain);
        setWillQoS(builder.willQoS);
        setWillFlag(builder.willFlag);
        setCleanSession(builder.cleanSession);
        setWillTopic(builder.willTopic);
        setWillMessage(builder.willMessage);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     *
     */

    public int getGwId() {
        return gwId;
    }

    public void setGwId(int gwId) {
        this.gwId = gwId;
    }

    public int getAdvPeriod() {
        return advPeriod;
    }

    public void setAdvPeriod(int advPeriod) {
        this.advPeriod = advPeriod;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public int getPredfTopicIdSize() {
        return predfTopicIdSize;
    }

    public void setPredfTopicIdSize(int predfTopicIdSize) {
        this.predfTopicIdSize = predfTopicIdSize;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public int getBroadcastUdpPort() {
        return broadcastUdpPort;
    }

    public void setBroadcastUdpPort(int broadcastUdpPort) {
        this.broadcastUdpPort = broadcastUdpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public String getBrokerURL() {
        return brokerURL;
    }

    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }

    public int getBrokerTcpPort() {
        return brokerTcpPort;
    }

    public void setBrokerTcpPort(int brokerTcpPort) {
        this.brokerTcpPort = brokerTcpPort;
    }

    public long getHandlerTimeout() {
        return handlerTimeout;
    }

    public void setHandlerTimeout(long handlerTimeout) {
        this.handlerTimeout = handlerTimeout;
    }

    public int getKeepAlivePeriod() {
        return keepAlivePeriod;
    }

    public void setKeepAlivePeriod(int keepAlivePeriod) {
        this.keepAlivePeriod = keepAlivePeriod;
    }

    public long getCkeckingPeriod() {
        return ckeckingPeriod;
    }

    public void setCkeckingPeriod(long ckeckingPeriod) {
        this.ckeckingPeriod = ckeckingPeriod;
    }

    public GatewayAddress getGatewayAddress() {
        return gatewayAddress;
    }

    public void setGatewayAddress(GatewayAddress gatewayAddress) {
        this.gatewayAddress = gatewayAddress;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(InetAddress ipAddr) {
        ipAddress = ipAddr;
    }

    public String getGatewayURL() {
        return gatewayURL;
    }

    public void setGatewayURL(String gatewayURL) {
        this.gatewayURL = gatewayURL;
    }

    public String getClientIntString() {
        return clientIntString;
    }

    public void setClientIntString(String clientIntString) {
        this.clientIntString = clientIntString;
    }

    public Vector<ClientConnection> getClientInterfaces() {
        return mClientInterfacesVector;
    }

    public void setClientInterfacesVector(Vector<ClientConnection> clientInterfacesVector) {
        this.mClientInterfacesVector = clientInterfacesVector;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public boolean isRetain() {
        return retain;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public int getWillQoS() {
        return willQoS;
    }

    public void setWillQoS(int willQoS) {
        this.willQoS = willQoS;
    }

    public boolean isWillFlag() {
        return willFlag;
    }

    public void setWillFlag(boolean willFlag) {
        this.willFlag = willFlag;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public String getWillTopic() {
        return willTopic;
    }

    public void setWillTopic(String willTopic) {
        this.willTopic = willTopic;
    }

    public String getWillMessage() {
        return willMessage;
    }

    public void setWillMessage(String willMessage) {
        this.willMessage = willMessage;
    }

    public long getForwarderTimeout() {
        return forwarderTimeout;
    }

    public void setForwarderTimeout(long forwarderTimeout) {
        this.forwarderTimeout = forwarderTimeout;
    }

    public int getMaxMqttsLength() {
        return maxMqttsLength;
    }

    public void setMaxMqttsLength(int maxMqttsLength) {
        this.maxMqttsLength = maxMqttsLength;
    }

    public int getMinMqttsLength() {
        return minMqttsLength;
    }

    public void setMinMqttsLength(int minMqttsLength) {
        this.minMqttsLength = minMqttsLength;
    }

    public Map<Integer, String> getPredefTopicIdTable() {
        return predefTopicIdTable;
    }

    public void setPredefTopicIdTable(Map<Integer, String> predefTopicIdTable) {
        this.predefTopicIdTable = predefTopicIdTable;
    }

    public String getSerialPortURL() {
        return serialPortURL;
    }

    public void setSerialPortURL(String serialPortURL) {
        this.serialPortURL = serialPortURL;
    }

    public static final class Builder {
        /**
         * default value
         */
        private int gwId = 24;
        private GatewayAddress gatewayAddress;
        private int advPeriod = 10; // in sec
        private int keepAlivePeriod = 65535; // in sec
        private int maxRetries = 3;
        private int waitingTime = 10; // in sec
        private int predfTopicIdSize = 30;
        private int maxMqttsLength = 65535;
        private int minMqttsLength = 2;
        private InetAddress ipAddress;
        private String gatewayURL;
        private int udpPort = -1;
        private String brokerURL = "localhost";
        private int brokerTcpPort = 1883;
        private String serialPortURL = "serial@COM1:57600";
        private long handlerTimeout = 864000; // in sec
        private long forwarderTimeout = 300; // in sec
        private long checkingPeriod = 864000; // in sec
        private String clientIntString = "<" + UDPClientConnection.class.getCanonicalName() + ">";
        private Vector<ClientConnection> mClientInterfacesVector;
        private Map<Integer, String> predefTopicIdTable = new HashMap<>();
        private String protocolName = "MQIsdp";
        private int protocolVersion = 3;
        private boolean retain = false;
        private int willQoS = 0;
        private boolean willFlag = false;
        private boolean cleanSession = true;
        private String willTopic = "";
        private String willMessage = "";
        private int broadcastUdpPort = 10000;

        private Builder() {
        }

        public Builder withGwId(int val) {
            gwId = val;
            return this;
        }

        public Builder withGatewayAddress(GatewayAddress val) {
            gatewayAddress = val;
            return this;
        }

        public Builder withAdvPeriod(int val) {
            advPeriod = val;
            return this;
        }

        public Builder withKeepAlivePeriod(int val) {
            keepAlivePeriod = val;
            return this;
        }

        public Builder withMaxRetries(int val) {
            maxRetries = val;
            return this;
        }

        public Builder withWaitingTime(int val) {
            waitingTime = val;
            return this;
        }

        public Builder withPredfTopicIdSize(int val) {
            predfTopicIdSize = val;
            return this;
        }

        public Builder withMaxMqttsLength(int val) {
            maxMqttsLength = val;
            return this;
        }

        public Builder withMinMqttsLength(int val) {
            minMqttsLength = val;
            return this;
        }

        public Builder withIpAddress(InetAddress val) {
            ipAddress = val;
            return this;
        }

        public Builder withGatewayURL(String val) {
            gatewayURL = val;
            return this;
        }

        public Builder withUdpPort(int val) {
            udpPort = val;
            return this;
        }

        public Builder withBrokerURL(String val) {
            brokerURL = val;
            return this;
        }

        public Builder withBrokerTcpPort(int val) {
            brokerTcpPort = val;
            return this;
        }

        public Builder withSerialPortURL(String val) {
            serialPortURL = val;
            return this;
        }

        public Builder withHandlerTimeout(long val) {
            handlerTimeout = val;
            return this;
        }

        public Builder withForwarderTimeout(long val) {
            forwarderTimeout = val;
            return this;
        }

        public Builder withCkeckingPeriod(long val) {
            checkingPeriod = val;
            return this;
        }

        public Builder withClientIntString(String val) {
            clientIntString = val;
            return this;
        }

        public Builder withClientInterfacesVector(Vector<ClientConnection> val) {
            mClientInterfacesVector = val;
            return this;
        }

        public Builder withPredefTopicIdTable(Map<Integer, String> val) {
            predefTopicIdTable = val;
            return this;
        }

        public Builder withProtocolName(String val) {
            protocolName = val;
            return this;
        }

        public Builder withProtocolVersion(int val) {
            protocolVersion = val;
            return this;
        }

        public Builder withRetain(boolean val) {
            retain = val;
            return this;
        }

        public Builder withWillQoS(int val) {
            willQoS = val;
            return this;
        }

        public Builder withWillFlag(boolean val) {
            willFlag = val;
            return this;
        }

        public Builder withCleanSession(boolean val) {
            cleanSession = val;
            return this;
        }

        public Builder withWillTopic(String val) {
            willTopic = val;
            return this;
        }

        public Builder withWillMessage(String val) {
            willMessage = val;
            return this;
        }

        public Builder withBroadcastUdpPort(int val) {
            broadcastUdpPort = val;
            return this;
        }

        public GWParameters build() {
            return new GWParameters(this);
        }
    }
}