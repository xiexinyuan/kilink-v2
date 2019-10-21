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

package org.eclipse.paho.mqttsn.gateway.core;


import org.eclipse.paho.mqttsn.gateway.Gateway;
import org.eclipse.paho.mqttsn.gateway.broker.AbstractBrokerConnection;
import org.eclipse.paho.mqttsn.gateway.broker.BrokerStateListener;
import org.eclipse.paho.mqttsn.gateway.client.ClientConnection;
import org.eclipse.paho.mqttsn.gateway.exceptions.MqttsException;
import org.eclipse.paho.mqttsn.gateway.messages.Message;
import org.eclipse.paho.mqttsn.gateway.messages.control.ControlMessage;
import org.eclipse.paho.mqttsn.gateway.messages.mqtt.*;
import org.eclipse.paho.mqttsn.gateway.messages.mqtts.*;
import org.eclipse.paho.mqttsn.gateway.timer.TimerService;
import org.eclipse.paho.mqttsn.gateway.utils.ClientAddress;
import org.eclipse.paho.mqttsn.gateway.utils.GatewayAddress;
import org.eclipse.paho.mqttsn.gateway.utils.GatewayLogger;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This object implements the core functions of the protocol translation.
 * For each client there is one instance of this object.Every message (Mqtt,
 * Mqtts or Control)
 * that corresponds to a certain client, is handled by this object.
 */
public class ClientMsgHandler extends MsgHandler {
    //TODO:重点这里的类型并不是MQTT本身真的有的,这里只是一个辅助使用的类型!!
    public static final int EXTRA_MQTT_REGISTER = Integer.MAX_VALUE - 1;

    //the unique address of the client that distinguishes this object
    private ClientAddress mClientAddress = null;

    //the clientId of the client(the one that is sent in Mqtts CONNECT message)
    private String mClientId = "...";

    //the ClientInterface (IP, Serial, etc.) in which this object should
    //respond in case of sending a Mqtts message to the client
    private ClientConnection mClientConnection = null;

    //the BrokerInterface which represents an interface for communication with the broker
    private AbstractBrokerConnection mBrokerConnection = null;

    //a timer service which is used for timeouts
    private TimerService mTimerService = null;

    //a table that is used for mapping topic Ids with topic names
    private TopicMappingTable mTopicMappingTable = null;

    //a reference to Dispatcher object
    private Dispatcher mDispatcher = null;

    //class that represents the state of the client at any given time
    private ClientState mClientState = null;

    //class that represents the state of the mGatewayState (actually this handler's state) at any given time
    private GatewayState mGatewayState = null;

    private ClientMsgHandlerStateListener mStateListener;

    //variable for checking the time of inactivity of this object
    //in order to remove it from Dispatcher's mapping table
    private long mTimeout;

    //messages for storing the information while on a connection procedure
    private MqttsConnect mqttsConnect = null;
    private MqttsWillTopic mqttsWillTopic = null;

    //messages for storing information while on a subscribe/unsubscribe procedure
    private HashMap<Integer, MqttsSubscribe> mPendingMqttsSubscribe = new HashMap<>();
    private HashMap<Integer, MqttsUnsubscribe> mPendingMqttsUnSubscribe = new HashMap<>();
//    private MqttsSubscribe mqttsSubscribe = null;
//    private MqttsUnsubscribe mqttsUnsubscribe = null;

    //message for storing information while on registration procedure initiated by the mGatewayState
    private HashMap<Integer, MqttsRegister> mPendingMqttsRegister = new HashMap<>();
//    private MqttsRegister mqttsRegister = null;

    //message for storing information (if necessary) when we receive a Mqtt PUBLISH message
    private HashMap<Integer, MqttsPublish> mPendingMqttsPublish = new HashMap<>();
//    private MqttPublish mqttPublish = null;

    //message for storing information (if necessary) when we receive a Mqtts PUBLISH message
    private HashMap<Integer, MqttPublish> mPendingMqttPublish = new HashMap<>();
//    private MqttsPublish mqttsPublish = null;


    //variables for handling Mqtts messages WILLTOPICUPD and WILLMSGUPD
    //private String willtopic = "";
    //private String willMessage = "";

    //variables for storing the msgId and topicId that are issued by the mGatewayState
    private AtomicInteger msgId;
    private AtomicInteger topicId;

    private Gateway mGateway;


    /**
     * Constructor of the ClientMsgHandler.
     *
     * @param addr The address of the client.
     */
    public ClientMsgHandler(ClientAddress addr) {
        this.mClientAddress = addr;
    }


    /**
     * get a id for the first topic in topics group
     *
     * @param increment topic size for topic increasing
     * @return
     */
    public int getInitTopicId(int increment) {
        if (increment <= 0) {
            return -1;
        }
        int id = getNewTopicId();
        updateTopicId(increment);
        GatewayLogger.log(GatewayLogger.INFO, "init topic ID = " + id);
        return id;
    }

    /**
     * auto subscribe topics and create the init topicId
     *
     * @param initId the base topicId for topics to calculate each union topicId map to each topic
     * @param topics the topics for subscribing with the increment
     * @return return entry&ltinitId,lastId> if subscribed success otherwise return false
     */
    public Map.Entry<Integer, Integer> subscribeDynamicTopics(int initId, Map<Integer, String> topics) {
        if (mTopicMappingTable != null && topics != null && topics.size() > 0) {
            if (initId <= 0) {
                initId = getNewTopicId();
            }
            GatewayLogger.log(GatewayLogger.INFO, "subscribe dynamic topics size = " + topics.size() + " and init topic id = " + initId);
            int maxIncrement = 0, oldId = 0, newId = 0;
            GatewayLogger.log(GatewayLogger.INFO, "-------------subscribe dynamic topics------------");
            for (Map.Entry<Integer, String> entry : topics.entrySet()) {
                //这里的ID只是提供了一个增量值,需要基于初始值的基础上进行增量值的叠加从而得到对应的短ID
                int increment = entry.getKey() != null ? entry.getKey() : 0;
                String topic = entry.getValue();
                if (topic != null) {
                    newId = initId + increment;
                    //移除掉旧的topicId
                    oldId = mTopicMappingTable.getTopicId(topic);
                    if (oldId > 0) {
                        mTopicMappingTable.removeTopicId(oldId);
                        GatewayLogger.log(GatewayLogger.INFO, "remove old topic Id = " + oldId);
                    }

                    mTopicMappingTable.assignTopicId(newId, topic);
                    //获取当前最大值的ID
                    maxIncrement = Math.max(maxIncrement, increment);
                    GatewayLogger.log(GatewayLogger.INFO, "[" + topic + "] tid = " + newId);
                }
            }
            GatewayLogger.log(GatewayLogger.INFO, "-------------subscribe end------------");
            //更新当前的ID值
            int lastId = initId + maxIncrement;
            GatewayLogger.log(GatewayLogger.INFO, "subscribe dynamic topics last topic id = " + lastId);
            return new AbstractMap.SimpleEntry<Integer, Integer>(initId, lastId);
        }
        return null;
    }

    /**
     * 订阅固定topicsId的topics
     *
     * @param topics id与topic信息的映射表
     */
    public void subscribePreTopics(Map<Integer, String> topics) {
        if (mTopicMappingTable != null && topics != null) {
            GatewayLogger.log(GatewayLogger.INFO, "-------------subscribe pre topics------------");
            int oldId = 0;
            for (Map.Entry<Integer, String> entry : topics.entrySet()) {
                int id = entry.getKey() != null ? entry.getKey() : -1;
                String topic = entry.getValue();
                if (id != -1 && topic != null) {
                    //移除掉旧的topicId
                    oldId = mTopicMappingTable.getTopicId(topic);
                    if (oldId > 0) {
                        mTopicMappingTable.removeTopicId(oldId);
                        GatewayLogger.log(GatewayLogger.INFO, "remove old topic Id = " + oldId);
                    }
                    //这里的id使用的是提供的值直接做为短ID,与普通流程注册是相同的
                    mTopicMappingTable.assignTopicId(id, topic);
                    GatewayLogger.log(GatewayLogger.INFO, "[" + topic + "] tid = " + id);
                }
            }
            GatewayLogger.log(GatewayLogger.INFO, "-------------subscribe end------------");
        }
    }

    /**
     * 取消预订阅的topics
     *
     * @param topicIds topics的固定ID
     */
    public void unsubscribePreTopicsByIds(Collection<Integer> topicIds) {
        if (mTopicMappingTable != null && topicIds != null) {
            for (Integer key : topicIds) {
                mTopicMappingTable.removeTopicId(key);
            }
        }
    }

    /**
     * 取消预订阅的topics
     *
     * @param topics topics
     */
    public void unsubscribePreTopicsByTopics(Collection<String> topics) {
        if (mTopicMappingTable != null && topics != null) {
            GatewayLogger.log(GatewayLogger.INFO, "-------------unsubscribe pre topics------------");
            for (String topic : topics) {
                GatewayLogger.log(GatewayLogger.INFO, topic);
                mTopicMappingTable.removeTopicId(topic);
            }
            GatewayLogger.log(GatewayLogger.INFO, "----------------unsubscribe end----------------");
        }
    }


    /* (non-Javadoc)
     * @see org.eclipse.paho.mqttsn.mGatewayState.core.MsgHandler#initialize()
	 */
    public void initialize(Gateway gateway) {
        mGateway = gateway;
        mBrokerConnection = gateway.getBrokerFactory().createBroker(gateway, this.mClientAddress);
        mBrokerConnection.initialize(mGateway, new BrokerStateListener() {
            @Override
            public void onConnected(String ip, int port) {
                if (mStateListener != null) {
                    mStateListener.onClientConnected(ClientMsgHandler.this);
                }
            }

            @Override
            public void onDisconnected(String ip, int port) {
                if (mStateListener != null) {
                    mStateListener.onClientDisconnected(ClientMsgHandler.this);
                }
            }
        });

        mTimerService = gateway.getTimerService();
        mDispatcher = gateway.getDispatcher();
        mTopicMappingTable = new TopicMappingTable();
        mTopicMappingTable.initialize(gateway.getParameters());
        mTimeout = 0;
        mClientState = new ClientState();
        mGatewayState = new GatewayState();
        msgId = new AtomicInteger(1);
        topicId = new AtomicInteger(mGateway.getParameters().getPredfTopicIdSize() + 1);
    }


    /******************************************************************************************/
    /**                      HANDLING OF MQTTS MESSAGES FROM THE CLIENT                     **/
    /****************************************************************************************/

	/* (non-Javadoc)
     * @see org.eclipse.paho.mqttsn.mGatewayState.core.MsgHandler#handleMqttsMessage(org.eclipse.paho.mqttsn.mGatewayState.messages.mqtts.MqttsMessage)
	 */
    public void handleMqttsMessage(MqttsMessage receivedMsg) {
        //update this handler's timeout
        mTimeout = System.currentTimeMillis() + mGateway.getParameters().getHandlerTimeout() * 1000;

//        boolean isGatewayIntercept = mBrokerConnection.interceptMqttsMessage(receivedMsg);
//        if (isGatewayIntercept) {
//            GatewayLogger.log(GatewayLogger.WARN, "gateway intercept message to handle,ignore this message, msgType = " + MqttsMessage.readableMsgType(receivedMsg.getMsgType()));
//            return;
//        }

        //get the type of the Mqtts message and handle the message according to that type
        switch (receivedMsg.getMsgType()) {
            case MqttsMessage.ADVERTISE:
                //we will never receive such a message from the client
                break;

            case MqttsMessage.SEARCHGW:
                handleMqttsSearchGW((MqttsSearchGW) receivedMsg);
                break;

            case MqttsMessage.GWINFO:
                //we will never receive such a message from the client
                break;

            case MqttsMessage.CONNECT:
                handleMqttsConnect((MqttsConnect) receivedMsg);
                break;

            case MqttsMessage.CONNACK:
                //we will never receive such a message from the client
                break;

            case MqttsMessage.WILLTOPICREQ:
                //we will never receive such a message from the client
                break;

            case MqttsMessage.WILLTOPIC:
                handleMqttsWillTopic((MqttsWillTopic) receivedMsg);
                break;

            case MqttsMessage.WILLMSGREQ:
                //we will never receive such a message from the client
                break;

            case MqttsMessage.WILLMSG:
                handleMqttsWillMsg((MqttsWillMsg) receivedMsg);
                break;

            case MqttsMessage.REGISTER:
                handleMqttsRegister((MqttsRegister) receivedMsg);
                break;

            case MqttsMessage.REGACK:
                handleMqttsRegack((MqttsRegack) receivedMsg);
                break;

            case MqttsMessage.PUBLISH:
                handleMqttsPublish((MqttsPublish) receivedMsg);
                break;

            case MqttsMessage.PUBACK:
                handleMqttsPuback((MqttsPuback) receivedMsg);
                break;

            case MqttsMessage.PUBCOMP:
                handleMqttsPubComp((MqttsPubComp) receivedMsg);
                break;

            case MqttsMessage.PUBREC:
                handleMqttsPubRec((MqttsPubRec) receivedMsg);
                break;

            case MqttsMessage.PUBREL:
                handleMqttsPubRel((MqttsPubRel) receivedMsg);
                break;

            case MqttsMessage.SUBSCRIBE:
                handleMqttsSubscribe((MqttsSubscribe) receivedMsg);
                break;

            case MqttsMessage.SUBACK:
                //we will never receive such a message from the client
                break;

            case MqttsMessage.UNSUBSCRIBE:
                handleMqttsUnsubscribe((MqttsUnsubscribe) receivedMsg);
                break;

            case MqttsMessage.UNSUBACK:
                //we will never receive such a message from the client
                break;

            case MqttsMessage.PINGREQ:
                handleMqttsPingReq((MqttsPingReq) receivedMsg);
                break;

            case MqttsMessage.PINGRESP:
                handleMqttsPingResp((MqttsPingResp) receivedMsg);
                break;

            case MqttsMessage.DISCONNECT:
                handleMqttsDisconnect((MqttsDisconnect) receivedMsg);
                break;

            case MqttsMessage.WILLTOPICUPD:
                handleMqttsWillTopicUpd((MqttsWillTopicUpd) receivedMsg);
                break;

            case MqttsMessage.WILLTOPICRESP:
                //we will never receive such a message from the client
                break;

            case MqttsMessage.WILLMSGUPD:
                handleMqttsWillMsgUpd((MqttsWillMsgUpd) receivedMsg);
                break;

            case MqttsMessage.WILLMSGRESP:
                //we will never receive such a message from the client
                break;

            default:
                GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts message of unknown type \"" + receivedMsg.getMsgType() + "\" received.");
                break;
        }
    }


    /**
     * The method that handles a Mqtts SEARCHGW message.
     *
     * @param receivedMsg The received MqttsSearchGW message.
     */
    private void handleMqttsSearchGW(MqttsSearchGW receivedMsg) {
        //		GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler ["+this.mClientAddress.getIpAddressInfo()+"]/["+mClientId+"] - Mqtts SEARCHGW message with \"Radius\" = \""+receivedMsg.getRadius()+"\" received. The message will be handled by GatewayMsgHandler.");

        //construct an "internal" message (see org.eclipse.paho.mqttsn.mGatewayState.messages.Message)
        //for the GatewayMsgHandler and put it to the mDispatcher's queue
        GatewayAddress gwAddress = mGateway.getParameters().getGatewayAddress();
        Message msg = new Message(gwAddress);

        msg.setType(Message.MQTTS_MSG);
        msg.setMqttsMessage(receivedMsg);
        mDispatcher.putMessage(msg);
    }


    /**
     * The method that handles a Mqtts CONNECT message.
     *
     * @param receivedMsg The received MqttsConnect message.
     */
    private void handleMqttsConnect(MqttsConnect receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts CONNECT message with \"Will\" = \"" + receivedMsg.isWill() + "\" and \"CleanSession\" = \"" + receivedMsg.isCleanSession() + "\" received.");

        this.mClientId = receivedMsg.getClientId();

        //if the client is already connected return a Mqtts CONNACK
        if (mClientState.isConnected()) {
            // TODO - diff from protocol
//            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is already connected. Mqtts DISCONNECT message will be send to the mClientState.");
//            sendClientDisconnect();

            // 透传CONNECT，由broker决定重复CONNECT的行为
            MqttConnect mqttConnect = new MqttConnect();
            mqttConnect.setProtocolName(receivedMsg.getProtocolName());
            mqttConnect.setProtocolVersion(receivedMsg.getProtocolVersion());
            mqttConnect.setWill(receivedMsg.isWill());
            mqttConnect.setCleanStart(receivedMsg.isCleanSession());
            mqttConnect.setKeepAlive(receivedMsg.getDuration());
            mqttConnect.setClientId(receivedMsg.getClientId());
            //send the Mqtt CONNECT message to the broker
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtt CONNECT message to the broker.");
            try {
                mBrokerConnection.sendMqttMessage(mqttConnect);
            } catch (MqttsException e) {
                e.printStackTrace();
                GatewayLogger.log(GatewayLogger.ERROR, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Failed sending Mqtt CONNECT message to the broker.");
                return;
            }

//            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is already connected. Mqtts CONNACK message will be send to the mClientState.");
//            MqttsConnack connack = new MqttsConnack();
//            connack.setReturnCode(MqttsMessage.RETURN_CODE_ACCEPTED);
//            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts CONNACK message to the mClientState.");
//            mClientConnection.sendMsg(this.mClientAddress, connack);
            return;
        }

        //if the mGatewayState is already in process of establishing a connection with the client, drop the message
        if (mGatewayState.isEstablishingConnection()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is already establishing a connection. The received Mqtts CONNECT message cannot be processed.");
            return;
        }

        //if the will flag of the Mqtts CONNECT message is not set,
        //construct a Mqtt CONNECT message, send it to the broker and return
        if (!receivedMsg.isWill()) {
            MqttConnect mqttConnect = new MqttConnect();
            mqttConnect.setProtocolName(receivedMsg.getProtocolName());
            mqttConnect.setProtocolVersion(receivedMsg.getProtocolVersion());
            mqttConnect.setWill(receivedMsg.isWill());
            mqttConnect.setCleanStart(receivedMsg.isCleanSession());
            mqttConnect.setKeepAlive(receivedMsg.getDuration());
            mqttConnect.setClientId(receivedMsg.getClientId());
            //open a new TCP/IP connection with the broker
            try {

                mBrokerConnection.connect();
            } catch (MqttsException e) {
                e.printStackTrace();
                GatewayLogger.log(GatewayLogger.ERROR, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - An error occurred while TCP/IP connection setup with the broker.");
                return;
            }

            //send the Mqtt CONNECT message to the broker
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtt CONNECT message to the broker.");
            try {
                mBrokerConnection.sendMqttMessage(mqttConnect);
            } catch (MqttsException e) {
                e.printStackTrace();
                GatewayLogger.log(GatewayLogger.ERROR, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Failed sending Mqtt CONNECT message to the broker.");
                return;
            }

            //set the state of the client as "Connected"
            mClientState.setConnected();
            return;
        }

        //if the will flag is set, store the received Mqtts CONNECT message, construct a
        //Mqtts WILTOPICREQ message, and send it to the client
        this.mqttsConnect = receivedMsg;
        MqttsWillTopicReq willTopicReq = new MqttsWillTopicReq();

        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts WILLTOPICREQ message to the mClientState.");
        mClientConnection.sendMsg(this.mClientAddress, willTopicReq);

        //set the mGatewayState on "waitingWillTopic" state and increase
        //the tries of sending Mqtts WILLTOPICREQ message to the client
        mGatewayState.setWaitingWillTopic();
        mGatewayState.increaseTriesSendingWillTopicReq();

        //set a timeout for waiting a Mqtts WILLTOPIC message from the client by registering to the timer
        mTimerService.register(this.mClientAddress, ControlMessage.WAITING_WILLTOPIC_TIMEOUT, mGateway.getParameters().getWaitingTime());
    }


    /**
     * The method that handles a Mqtts WILLTOPIC message.
     *
     * @param receivedMsg The received MqttsWillTopic message.
     */
    private void handleMqttsWillTopic(MqttsWillTopic receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts WILLTOPIC message with \"WillTopic\" = \"" + receivedMsg.getWillTopic() + "\" received.");
        //if the mGatewayState is not expecting a Mqtts WILLTOPIC at this time, drop the received message and return
        if (!mGatewayState.isWaitingWillTopic()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Gateway is not waiting a Mqtts WILLTOPIC message from the mClientState. The received message cannot be processed.");
            return;
        }

        //"reset" the "waitingWillTopic" state of the mGatewayState, "reset" the tries of sending
        //Mqtts WILLTOPICREQ message to the client and unregister from the timer
        mGatewayState.resetWaitingWillTopic();
        mGatewayState.resetTriesSendingWillTopicReq();
        mTimerService.unregister(this.mClientAddress, ControlMessage.WAITING_WILLTOPIC_TIMEOUT);

        //store the received Mqtts WILLTOPIC message, construct a Mqtts
        //WILLMSGREQ message, and send it to the client
        this.mqttsWillTopic = receivedMsg;
        MqttsWillMsgReq willMsgReq = new MqttsWillMsgReq();

        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts WILLMSGREQ message to the mClientState.");
        mClientConnection.sendMsg(this.mClientAddress, willMsgReq);

        //set the mGatewayState on "waitingWillMsg" state and increase
        //the tries of sending Mqtts WILLMSGREQ message to the client
        mGatewayState.setWaitingWillMsg();
        mGatewayState.increaseTriesSendingWillMsgReq();

        //set a timeout for waiting a Mqtts WILLMSG message from the client by registering to the timer
        mTimerService.register(this.mClientAddress, ControlMessage.WAITING_WILLMSG_TIMEOUT, mGateway.getParameters().getWaitingTime());
    }

    /**
     * The method that handles a Mqtts WILLMSG message.
     *
     * @param receivedMsg The received MqttsWillMsg message.
     */
    private void handleMqttsWillMsg(MqttsWillMsg receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts WILLMSG message with \"WillMsg\" = \"" + receivedMsg.getWillMsg() + "\" received.");
        //if the mGatewayState is not expecting a Mqtts WILLMSG at this time, drop the received message and return
        if (!mGatewayState.isWaitingWillMsg()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Gateway is not waiting a Mqtts WILLMSG message from the mClientState.The received message cannot be processed.");
            return;
        }

        //"reset" the "waitingWillMsg" state of the mGatewayState, "reset" the tries of sending
        //Mqtts WILLMSGREQ message to the client and unregister from the timer
        mGatewayState.resetWaitingWillMsg();
        mGatewayState.resetTriesSendingWillMsgReq();
        mTimerService.unregister(this.mClientAddress, ControlMessage.WAITING_WILLMSG_TIMEOUT);

        //assure that the stored Mqtts CONNECT and Mqtts WILLTOPIC messages that we received before are not null
        //if one of them is null delete the other and return (debugging checks)
        if (this.mqttsConnect == null) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - The stored Mqtts CONNECT message is null. The received Mqtts WILLMSG message cannot be processed.");
            this.mqttsWillTopic = null;
            return;
        }
        if (this.mqttsWillTopic == null) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - The stored Mqtts WILLTOPIC message is null. The received Mqtts WILLMSG message cannot be processed.");
            this.mqttsConnect = null;
            return;
        }

        //construct a Mqtt CONNECT message
        MqttConnect mqttConnect = new MqttConnect();

        //populate the Mqtt CONNECT message with the information of the stored Mqtts CONNECT
        //and WILLTOPIC messages and the information of the received Mqtts WILLMSG message
        mqttConnect.setProtocolName(this.mqttsConnect.getProtocolName());
        mqttConnect.setProtocolVersion(this.mqttsConnect.getProtocolVersion());
        mqttConnect.setWillRetain(this.mqttsWillTopic.isRetain());
        mqttConnect.setWillQoS(this.mqttsWillTopic.getQos());
        mqttConnect.setWill(this.mqttsConnect.isWill());
        mqttConnect.setCleanStart(this.mqttsConnect.isCleanSession());
        mqttConnect.setKeepAlive(this.mqttsConnect.getDuration());
        mqttConnect.setClientId(this.mqttsConnect.getClientId());
        mqttConnect.setWillTopic(this.mqttsWillTopic.getWillTopic());
        mqttConnect.setWillMessage(receivedMsg.getWillMsg());

        try {
            //open a new TCP/IP connection with the broker
            mBrokerConnection.connect();
        } catch (MqttsException e) {
            e.printStackTrace();
            GatewayLogger.log(GatewayLogger.ERROR, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - An error occurred while TCP/IP connection setup with the broker.");
            return;
        }

        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtt CONNECT message to the broker.");
        //send the Mqtt CONNECT message to the broker
        try {
            mBrokerConnection.sendMqttMessage(mqttConnect);
        } catch (MqttsException e) {
            e.printStackTrace();
            GatewayLogger.log(GatewayLogger.ERROR, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Failed sending Mqtt CONNECT message to the broker.");
            return;
        }

        //set the state of the client as "Connected"
        mClientState.setConnected();

        //delete the stored Mqtts CONNECT and Mqtts WILLTOPIC messages
        this.mqttsConnect = null;
        this.mqttsWillTopic = null;
    }


    /**
     * The method that handles a Mqtts REGISTER message.
     *
     * @param receivedMsg The received MqttsRegister message.
     */
    private void handleMqttsRegister(MqttsRegister receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts REGISTER message with \"TopicName\" = \"" + receivedMsg.getTopicName() + "\" received.");

        //if the client is not in state "Connected" send to it a Mqtts DISCONNECT message and return
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtts REGISTER message cannot be processed.");
            sendClientDisconnect();
            return;
        }

        int topicId = mTopicMappingTable.getTopicId(receivedMsg.getTopicName());
        if (topicId == 0) {
            //assign a topicID to the received topicName
            topicId = getNewTopicId();
            mTopicMappingTable.assignTopicId(topicId, receivedMsg.getTopicName());
            GatewayLogger.log(GatewayLogger.INFO, "REG save topic with ID [" + receivedMsg.getTopicName() + "]/tid = " + topicId);
        }

        try {
            MqttSubscribe subscribe = new MqttSubscribe();
            subscribe.setTopicName(receivedMsg.getTopicName());
            subscribe.setMsgId(receivedMsg.getMsgId());
            subscribe.setMsgType(EXTRA_MQTT_REGISTER);
            mBrokerConnection.sendMqttMessage(subscribe);
        } catch (MqttsException e) {
            e.printStackTrace();
            GatewayLogger.log(GatewayLogger.ERROR, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Failed handle MQTT register message to the broker.");
            return;
        }

        GatewayLogger.log(GatewayLogger.INFO, " <===> topicid:" + topicId + " " + receivedMsg.getTopicName());

        //construct a Mqtts REGACK message
        MqttsRegack regack = new MqttsRegack();
        regack.setTopicId(topicId);
        regack.setMsgId(receivedMsg.getMsgId());
        regack.setReturnCode(MqttsMessage.RETURN_CODE_ACCEPTED);

        //send the Mqtts REGACK message to the client
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts REGACK message with \"TopicId\" = \"" + topicId + "\" to the mClientState.");
        mClientConnection.sendMsg(this.mClientAddress, regack);
    }

    /**
     * The method that handles a Mqtts REGACK message.
     *
     * @param receivedMsg The received MqttsRegack message.
     */
    private void handleMqttsRegack(MqttsRegack receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts REGACK message with \"TopicId\" = \"" + receivedMsg.getTopicId() + "\" received.");

        //if the client is not in state "Connected" send to it a Mqtts DISCONNECT message and return
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtts REGACK message cannot be processed.");
            sendClientDisconnect();
            return;
        }

        //if the mGatewayState is not expecting a Mqtts REGACK at this time, drop the received message and return
        if (!mGatewayState.isWaitingRegack(receivedMsg.getTopicId())) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Gateway is not waiting a Mqtts REGACK message from the mClientState. The received message cannot be processed.");
            return;
        }

        MqttsRegister pendingRegister = mPendingMqttsRegister.get(receivedMsg.getTopicId());

        //assure that the stored Mqtts REGISTER and Mqtt PUBLISH messages are not null
        //if one of them is null delete the other and return (debugging checks)
        if (pendingRegister == null) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - The stored Mqtts REGISTER message is null. The received Mqtts REGACK message cannot be processed.");

            //"reset" the "waitingRegack" state of the mGatewayState, "reset" the tries of sending
            //Mqtts REGISTER message to the client and unregister from the timer
            mGatewayState.resetWaitingRegack(receivedMsg.getTopicId());
            mGatewayState.resetTriesSendingRegister(receivedMsg.getTopicId());
            mTimerService.unregister(this.mClientAddress, ControlMessage.WAITING_REGACK_TIMEOUT);

//            this.mqttPublish = null;
            mPendingMqttPublish.remove(receivedMsg.getTopicId());
            return;
        }
        if (mPendingMqttPublish.get(receivedMsg.getTopicId()) == null) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - The stored Mqtt PUBLISH message is null. The received Mqtts REGACK message cannot be processed.");

            //"reset" the "waitingRegack" state of the mGatewayState, "reset" the tries of sending
            //Mqtts REGISTER message to the client and unregister from the timer
            mGatewayState.resetWaitingRegack(receivedMsg.getTopicId());
            mGatewayState.resetTriesSendingRegister(receivedMsg.getTopicId());
            mTimerService.unregister(this.mClientAddress, ControlMessage.WAITING_REGACK_TIMEOUT);

//            this.mqttsRegister = null;
            mPendingMqttsRegister.remove(receivedMsg.getTopicId());
            return;
        }

        //if the MsgId of the received Mqtt REGACK is not the same with MsgId of the stored Mqtts
        //REGISTER message drop the received message and return (don't delete any stored message)
        if (receivedMsg.getMsgId() != pendingRegister.getMsgId()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - MsgId (\"" + receivedMsg.getMsgId() + "\") of the received Mqtts REGACK message does not match the MsgId (\"" + pendingRegister.getMsgId() + "\") of the stored Mqtts REGISTER message. The message cannot be processed.");
            return;
        }

        //assign the topicId of the Mqtts REGACK message to the received topicName of
        //the Mqtt PUBLISH message (topicId is the same as in the stored Mqtts REGISTER message)

        MqttPublish pendingMqttPublish = mPendingMqttPublish.get(receivedMsg.getTopicId());

        //移除掉旧的topicId
        int oldId = mTopicMappingTable.getTopicId(pendingMqttPublish.getTopicName());
        if (oldId > 0) {
            mTopicMappingTable.removeTopicId(oldId);
            GatewayLogger.log(GatewayLogger.INFO, "remove old topic Id = " + oldId);
        }
        mTopicMappingTable.assignTopicId(receivedMsg.getTopicId(), pendingMqttPublish.getTopicName());
        GatewayLogger.log(GatewayLogger.INFO, "REGACK save topic with ID \n[" + pendingMqttPublish.getTopicName() + "]/tid = " + topicId);


        //now we have a topicId, so construct a Mqtts PUBLISH message
        MqttsPublish publish = new MqttsPublish();

        //populate the Mqtts PUBLISH message with the information of the stored Mqtt PUBLISH message
        publish.setDup(pendingMqttPublish.isDup());
        publish.setQos(pendingMqttPublish.getQos());
        publish.setRetain(pendingMqttPublish.isRetain());
        publish.setTopicIdType(MqttsMessage.NORMAL_TOPIC_ID);
        publish.setTopicId(receivedMsg.getTopicId());
        publish.setMsgId(pendingMqttPublish.getMsgId());
        publish.setData(pendingMqttPublish.getPayload());

        //send the Mqtts PUBLISH message to the client
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts PUBLISH message with \"QoS\" = \"" + pendingMqttPublish.getQos() + "\" and \"TopicId\" = \"" + receivedMsg.getTopicId() + "\" to the mClientState.");
        mClientConnection.sendMsg(this.mClientAddress, publish);

        //"reset" the "waitingRegack" state of the mGatewayState, "reset" the tries of sending
        //Mqtts REGISTER message to the client, unregister from the timer and delete
        //the stored Mqtts REGISTER and Mqtt PUBLISH messages
        mGatewayState.resetWaitingRegack(receivedMsg.getTopicId());
        mGatewayState.resetTriesSendingRegister(receivedMsg.getTopicId());
        mTimerService.unregister(this.mClientAddress, ControlMessage.WAITING_REGACK_TIMEOUT);
//        this.mqttsRegister = null;
        mPendingMqttsRegister.remove(receivedMsg.getTopicId());
//        this.mqttPublish = null;
        mPendingMqttPublish.remove(receivedMsg.getTopicId());
    }


    /**
     * The method that handles a Mqtts PUBLISH message.
     *
     * @param receivedMsg The received MqttsPublish message.
     */
    private void handleMqttsPublish(MqttsPublish receivedMsg) {
        if (receivedMsg.getTopicIdType() == MqttsMessage.NORMAL_TOPIC_ID)
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts PUBLISH message with \"QoS\" = \"" + receivedMsg.getQos() + "\" and \"TopicId\" = \"" + receivedMsg.getTopicId() + "\" received.");
        else if (receivedMsg.getTopicIdType() == MqttsMessage.PREDIFINED_TOPIC_ID)
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts PUBLISH message with \"QoS\" = \"" + receivedMsg.getQos() + "\" and \"TopicId\" = \"" + receivedMsg.getTopicId() + "\" (predefined topid Id) received.");
        else if (receivedMsg.getTopicIdType() == MqttsMessage.SHORT_TOPIC_NAME)
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts PUBLISH message with \"QoS\" = \"" + receivedMsg.getQos() + "\" and \"TopicId\" = \"" + receivedMsg.getShortTopicName() + "\" (short topic name) received.");
        else {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts PUBLISH message with unknown topicIdType (\"" + receivedMsg.getTopicIdType() + "\") received. The message cannot be processed.");
            return;
        }


        //if Mqtts PUBLISH message has QoS = -1, construct an "internal" message (see org.eclipse.paho.mqttsn.mGatewayState.core.Message)
        //for the GatewayMsgHandler and put it to the mDispatcher's queue
        if (receivedMsg.getQos() == -1) {
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - The received Mqtts PUBLISH message with \"QoS\" = \"-1\" will be handled by GatewayMsgHandler.");

            Message msg = new Message(mGateway.getParameters().getGatewayAddress());

            msg.setType(Message.MQTTS_MSG);
            msg.setMqttsMessage(receivedMsg);
            mDispatcher.putMessage(msg);
            return;
        }

        //if the client is not in state "Connected" send to it a Mqtts DISCONNECT message and return
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtts PUBLISH message cannot be processed.");
            sendClientDisconnect();
            return;
        }

        //if there is already a publish procedure from the client with QoS 1 and the
        //mGatewayState is expecting a Mqtt PUBACK from the broker, then drop the message if it has QoS 1
        if (mGatewayState.isWaitingPuback(receivedMsg.getMsgId()) && receivedMsg.getQos() == 1) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is already in a publish procedure with \"QoS\" = \"1\". The received Mqtts PUBLISH message with \"QoS\" = \"" + receivedMsg.getQos() + "\" cannot be processed.");
            return;
        }

        //else construct a Mqtt PUBLISH message
        MqttPublish publish = new MqttPublish();

        //check the TopicIdType in the received Mqtts PUBLISH message
        switch (receivedMsg.getTopicIdType()) {

            //if the TopicIdType is a normal TopicId
            case MqttsMessage.NORMAL_TOPIC_ID:
                if (receivedMsg.getTopicId() <= mGateway.getParameters().getPredfTopicIdSize()) {
                    GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - TopicId (\"" + receivedMsg.getTopicId() + "\") of the received Mqtts PUBLISH message is in the range of predefined topic Ids [1," + mGateway.getParameters().getPredfTopicIdSize() + "]. The message cannot be processed. Mqtts PUBACK with rejection reason will be sent to the mClientState.");

                    //construct a Mqtts PUBACK message with ReturnCode = "Rejected:Invalid TopicId"
                    MqttsPuback puback = new MqttsPuback();
                    puback.setTopicId(receivedMsg.getTopicId());
                    puback.setMsgId(receivedMsg.getMsgId());
                    puback.setReturnCode(MqttsMessage.RETURN_CODE_INVALID_TOPIC_ID);

                    //send the Mqtts PUBACK message to the client
                    GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts PUBACK message with \"TopicId\" = \"" + receivedMsg.getTopicId() + "\" and \"ReturnCode\" = \"Rejected: invalid TopicId\" to the mClientState.");
                    mClientConnection.sendMsg(this.mClientAddress, puback);
                    return;
                }

                //get the TopicName by TopicId
                String topicName = mTopicMappingTable.getTopicName(receivedMsg.getTopicId());

                //if there is no such an entry
                if (topicName == null) {
                    GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - TopicId (\"" + receivedMsg.getTopicId() + "\") of the received Mqtts PUBLISH message does not exist. The message cannot be processed. Mqtts PUBACK with rejection reason will be sent to the mClientState.");

                    //construct a Mqtts PUBACK message with ReturnCode = "Rejected:Invalid TopicId"
                    MqttsPuback puback = new MqttsPuback();
                    puback.setTopicId(receivedMsg.getTopicId());
                    puback.setMsgId(receivedMsg.getMsgId());
                    puback.setReturnCode(MqttsMessage.RETURN_CODE_INVALID_TOPIC_ID);

                    //send the Mqtts PUBACK message to the client
                    GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts PUBACK message with \"TopicId\" = \"" + receivedMsg.getTopicId() + "\" and \"ReturnCode\" = \"Rejected: invalid TopicId\" to the mClientState.");
                    mClientConnection.sendMsg(this.mClientAddress, puback);
                    return;
                }

                //we found a topicName corresponding to the received topicId
                publish.setTopicName(topicName);
                break;

            //if the TopicIdType is a shortTopicName then simply copy it to the topicName field of the Mqtt PUBLISH message
            case MqttsMessage.SHORT_TOPIC_NAME:
                publish.setTopicName(receivedMsg.getShortTopicName());
                break;

            //if the TopicIdType is a predifinedTopiId
            case MqttsMessage.PREDIFINED_TOPIC_ID:
                if (receivedMsg.getTopicId() > mGateway.getParameters().getPredfTopicIdSize()) {
                    GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Predefined topicId (\"" + receivedMsg.getTopicId() + "\") of the received Mqtts PUBLISH message is out of the range of predefined topic Ids [1," + mGateway.getParameters().getPredfTopicIdSize() + "]. The message cannot be processed. Mqtts PUBACK with rejection reason will be sent to the mClientState.");

                    //construct a Mqtts PUBACK message with ReturnCode = "Rejected:Invalid TopicId"
                    MqttsPuback puback = new MqttsPuback();
                    puback.setTopicId(receivedMsg.getTopicId());
                    puback.setMsgId(receivedMsg.getMsgId());
                    puback.setReturnCode(MqttsMessage.RETURN_CODE_INVALID_TOPIC_ID);

                    //send the Mqtts PUBACK message to the client
                    GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts PUBACK message with \"TopicId\" = \"" + receivedMsg.getTopicId() + "\" and \"ReturnCode\" = \"Rejected: invalid TopicId\" to the mClientState.");
                    mClientConnection.sendMsg(this.mClientAddress, puback);
                    return;
                }

                //get the predefined topic name that corresponds to the received predefined topicId
                topicName = mTopicMappingTable.getTopicName(receivedMsg.getTopicId());

                //this should not happen as predefined topic ids are already stored
                if (topicName == null) {
                    GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Predefined topicId (\"" + receivedMsg.getTopicId() + "\") of the received Mqtts PUBLISH message does not exist. The message cannot be processed. Mqtts PUBACK with rejection reason will be sent to the mClientState.");

                    //construct a Mqtts PUBACK message with ReturnCode = "Rejected:Invalid TopicId"
                    MqttsPuback puback = new MqttsPuback();
                    puback.setTopicId(receivedMsg.getTopicId());
                    puback.setMsgId(receivedMsg.getMsgId());
                    puback.setReturnCode(MqttsMessage.RETURN_CODE_INVALID_TOPIC_ID);

                    //send the Mqtts PUBACK message to the client
                    GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts PUBACK message with \"TopicId\" = \"" + receivedMsg.getTopicId() + "\" and \"ReturnCode\" = \"Rejected: invalid TopicId\" to the mClientState.");
                    mClientConnection.sendMsg(this.mClientAddress, puback);
                    return;
                }

                publish.setTopicName(topicName);
                break;

            default:
                GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Unknown topicIdType (\"" + receivedMsg.getTopicIdType() + "\"). The received Mqtts PUBLISH message cannot be processed.");
                return;
        }

        //populate the Mqtt PUBLISH message with the remaining information from Mqtts PUBLISH message
        publish.setDup(receivedMsg.isDup());
        publish.setQos(receivedMsg.getQos());
        publish.setRetain(receivedMsg.isRetain());
        publish.setMsgId(receivedMsg.getMsgId());
        publish.setPayload(receivedMsg.getData());

        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtt PUBLISH message with \"QoS\" = \"" + receivedMsg.getQos() + "\" and \"TopicName\" = \"" + publish.getTopicName() + "\" to the broker.");
        //send the Mqtt PUBLISH message to the broker
        try {
            mBrokerConnection.sendMqttMessage(publish);
        } catch (MqttsException e) {
            e.printStackTrace();
            GatewayLogger.log(GatewayLogger.ERROR, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Failed sending Mqtt PUBLISH message to the broker.");
            connectionLost();
            return;
        }

        //TODO handle qos 2
        if (receivedMsg.getQos() == 1) {
            mGatewayState.setWaitingPuback(publish.getMsgId());
//            this.mqttsPublish = receivedMsg;
            mPendingMqttsPublish.put(publish.getMsgId(), receivedMsg);
        }
        //if(receivedMsg.getQos() == 2)
    }


    /**
     * The method that handles a Mqtts PUBACK message.
     *
     * @param receivedMsg The received MqttsPuback message.
     */
    private void handleMqttsPuback(MqttsPuback receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts PUBACK message received.");

        //if the client is not in state "Connected" send to it a Mqtts DISCONNECT message and return
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtts PUBACK message cannot be processed.");
            sendClientDisconnect();
            return;
        }

        //if the return code of the Mqtts PUBACK message is "Rejected: Invalid topic ID", then
        //delete this topicId(and the associate topic name)from the mapping table
        if (receivedMsg.getReturnCode() == MqttsMessage.RETURN_CODE_INVALID_TOPIC_ID) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - The received Mqtts PUBACK has \"ReturnCode\" = \"Rejected: invalid TopicId\". TopicId \"" + receivedMsg.getTopicId() + "\" will be deleted from mapping table.");
            mTopicMappingTable.removeTopicId(receivedMsg.getTopicId());
            return;
        }

        //else if everything is ok, construct a Mqtt PUBACK message
        MqttPuback puback = new MqttPuback();
        puback.setMsgId(receivedMsg.getMsgId());

        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtt PUBACK message to the broker.");
        //send the Mqtt PUBACK message to the broker
        try {
            mBrokerConnection.sendMqttMessage(puback);
        } catch (MqttsException e) {
            e.printStackTrace();
            GatewayLogger.log(GatewayLogger.ERROR, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Failed sending Mqtt PUBACK message to the broker.");
            connectionLost();
        }
    }


    /**
     * The method that handles a Mqtts PUBCOMP message.
     *
     * @param receivedMsg The received MqttsPubComp message.
     */
    private void handleMqttsPubComp(MqttsPubComp receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts PUBCOMP message received.");

        //if the client is not in state "Connected" send to it a Mqtts DISCONNECT message and return
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtts PUBCOMP message cannot be processed.");
            sendClientDisconnect();
            return;
        }

        //else construct a Mqtt PUBCOMP message
        MqttPubComp pubcomp = new MqttPubComp();
        pubcomp.setMsgId(receivedMsg.getMsgId());

        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtt PUBCOMP message to the broker.");
        //send the Mqtt PUBCOMP message to the broker
        try {
            mBrokerConnection.sendMqttMessage(pubcomp);
        } catch (MqttsException e) {
            e.printStackTrace();
            //if failed sending the message
            GatewayLogger.log(GatewayLogger.ERROR, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Failed sending Mqtt PUBCOMP message to the broker.");
            connectionLost();
        }
    }


    /**
     * The method that handles a Mqtts PUBREC message.
     *
     * @param receivedMsg The received MqttsPubRec message.
     */
    private void handleMqttsPubRec(MqttsPubRec receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts PUBREC message received.");

        //if the client is not in state "Connected" send to it a Mqtts DISCONNECT message and return
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtts PUBREC message cannot be processed.");
            sendClientDisconnect();
            return;
        }

        //construct a Mqtt PUBREC message
        MqttPubRec pubrec = new MqttPubRec();
        pubrec.setMsgId(receivedMsg.getMsgId());

        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtt PUBREC message to the broker.");
        //send the Mqtt PUBREC message to the broker
        try {
            mBrokerConnection.sendMqttMessage(pubrec);
        } catch (MqttsException e) {
            e.printStackTrace();
            //if failed sending the message
            GatewayLogger.log(GatewayLogger.ERROR, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Failed sending Mqtt PUBREC message to the broker.");
            connectionLost();
        }
    }


    /**
     * The method that handles a Mqtts PUBREL message.
     *
     * @param receivedMsg The received MqttsPubRel message.
     */
    private void handleMqttsPubRel(MqttsPubRel receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts PUBREL message received.");

        //if the client is not in state "Connected" send to it a Mqtts DISCONNECT message and return
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtts PUBREL message cannot be processed.");
            sendClientDisconnect();
            return;
        }

        //construct a Mqtt PUBREL message
        MqttPubRel pubrel = new MqttPubRel();
        pubrel.setMsgId(receivedMsg.getMsgId());

        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtt PUBREL message to the broker.");
        //send the Mqtt PUBREL message to the broker
        try {
            mBrokerConnection.sendMqttMessage(pubrel);
        } catch (MqttsException e) {
            e.printStackTrace();
            //if failed sending the message
            GatewayLogger.log(GatewayLogger.ERROR, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Failed sending Mqtt PUBREL message to the broker.");
            connectionLost();
        }
    }


    /**
     * The method that handles a Mqtts SUBSCRIBE message.
     *
     * @param receivedMsg The received MqttsSubscribe message.
     */
    private void handleMqttsSubscribe(MqttsSubscribe receivedMsg) {
        if (receivedMsg.getTopicIdType() == MqttsMessage.TOPIC_NAME)
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts SUBSCRIBE message with \"TopicName\" = \"" + receivedMsg.getTopicName() + "\" received.");
        else if (receivedMsg.getTopicIdType() == MqttsMessage.PREDIFINED_TOPIC_ID)
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts SUBSCRIBE message with \"TopicId\" = \"" + receivedMsg.getPredefinedTopicId() + "\" (predefined topid Id) received.");
        else if (receivedMsg.getTopicIdType() == MqttsMessage.SHORT_TOPIC_NAME)
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts SUBSCRIBE message with \"TopicId\" = \"" + receivedMsg.getShortTopicName() + "\" (short topic name) received.");
        else {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts SUBSCRIBE message with unknown topicIdType (\"" + receivedMsg.getTopicIdType() + "\") received. The message cannot be processed.");
            return;
        }

        //if the client is not in state "Connected" send to it a Mqtts DISCONNECT message and return
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtts SUBSCRIBE message cannot be processed.");
            sendClientDisconnect();
            return;
        }

        //if we are already in a subscription process, drop the received message and return
        if (mGatewayState.isWaitingSuback(receivedMsg.getMsgId())) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is already in a subscription procedure. The received Mqtts SUBSCRIBE message cannot be processed.");
            return;
        }

        //else construct a Mqtt SUBSCRIBE message
        MqttSubscribe mqttSubscribe = new MqttSubscribe();

        //check the TopicIdType in the received Mqtts SUBSCRIBE message
        switch (receivedMsg.getTopicIdType()) {

            //if the TopicIdType is a TopicName
            case MqttsMessage.TOPIC_NAME:
                mqttSubscribe.setTopicName(receivedMsg.getTopicName());
                break;

            //if the TopicIdType is a shortTopicName
            case MqttsMessage.SHORT_TOPIC_NAME:
                mqttSubscribe.setTopicName(receivedMsg.getShortTopicName());
                break;

            //if the TopicIdType is a predifinedTopiId
            case MqttsMessage.PREDIFINED_TOPIC_ID:
                if (receivedMsg.getPredefinedTopicId() > mGateway.getParameters().getPredfTopicIdSize()) {
                    GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Predefined topicId (\"" + +receivedMsg.getPredefinedTopicId() + "\") of the received Mqtts SUBSCRIBE message is out of the range of predefined topic Ids [1," + mGateway.getParameters().getPredfTopicIdSize() + "]. The message cannot be processed. Mqtts SUBACK with rejection reason will be sent to the mClientState.");

                    //construct a Mqtts SUBACK message with ReturnCode = "Rejected:Invalid TopicId"
                    MqttsSuback suback = new MqttsSuback();
                    suback.setTopicIdType(MqttsMessage.PREDIFINED_TOPIC_ID);
                    suback.setPredefinedTopicId(receivedMsg.getPredefinedTopicId());
                    suback.setMsgId(receivedMsg.getMsgId());
                    suback.setReturnCode(MqttsMessage.RETURN_CODE_INVALID_TOPIC_ID);

                    //send the Mqtts SUBACK message to the client
                    GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts SUBACK message with \"TopicId\" = \"" + receivedMsg.getPredefinedTopicId() + "\" and \"ReturnCode\" = \"Rejected: invalid TopicId\" to the mClientState.");

                    mClientConnection.sendMsg(this.mClientAddress, suback);

                    return;
                }

                //get the predefined topic name that corresponds to the predefined topicId
                String topicName = mTopicMappingTable.getTopicName(receivedMsg.getPredefinedTopicId());

                //this should not happen as predefined topic ids are already stored
                if (topicName == null) {
                    GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Predefined topicId (\"" + receivedMsg.getPredefinedTopicId() + "\") of the received Mqtts SUBSCRIBE message does not exist. The message cannot be processed. Mqtts SUBACK with rejection reason will be sent to the mClientState.");

                    //construct a Mqtts SUBACK message with ReturnCode = "Rejected:Invalid TopicId"
                    MqttsSuback suback = new MqttsSuback();
                    suback.setTopicIdType(MqttsMessage.PREDIFINED_TOPIC_ID);
                    suback.setPredefinedTopicId(receivedMsg.getPredefinedTopicId());
                    suback.setMsgId(receivedMsg.getMsgId());
                    suback.setReturnCode(MqttsMessage.RETURN_CODE_INVALID_TOPIC_ID);

                    //send the Mqtts SUBACK message to the client
                    GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts SUBACK message with \"TopicId\" = \"" + receivedMsg.getPredefinedTopicId() + "\" and \"ReturnCode\" = \"Rejected: invalid TopicId\" to the mClientState.");

                    mClientConnection.sendMsg(this.mClientAddress, suback);
                    return;
                }
                mqttSubscribe.setTopicName(topicName);
                break;

            default:
                GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Unknown topicIdType (\"" + receivedMsg.getTopicIdType() + "\"). The received Mqtts SUBSCRIBE message cannot be processed.");
                return;
        }

        //store the received Mqtts SUBSCRIBE message (for handling Mqtt SUBACK from the broker)
//        this.mqttsSubscribe = receivedMsg;
        mPendingMqttsSubscribe.put(receivedMsg.getMsgId(), receivedMsg);

        // populate the Mqtt SUBSCRIBE message with the remaining information from Mqtts SUBSCRIBE message
        mqttSubscribe.setDup(receivedMsg.isDup());

        mqttSubscribe.setMsgId(receivedMsg.getMsgId());

        //set the requested QoS for the specific topic name
        mqttSubscribe.setRequestedQoS(receivedMsg.getQos());

        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtt SUBSCRIBE message with \"TopicName\" = \"" + mqttSubscribe.getTopicName() + "\" to the broker.");
        //send the Mqtt SUBSCRIBE message to the broker
        try {
            mBrokerConnection.sendMqttMessage(mqttSubscribe);
        } catch (MqttsException e) {
            e.printStackTrace();
            //if failed sending the message
            GatewayLogger.log(GatewayLogger.ERROR, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Failed sending Mqtt SUBSCRIBE message to the broker.");
            connectionLost();
            return;
        }

        //set the mGatewayState on "waitingSuback" state
        mGatewayState.setWaitingSuback(receivedMsg.getMsgId());
    }


    /**
     * The method that handles a Mqtts UNSUBSCRIBE message.
     *
     * @param receivedMsg The received MqttsUnsubscribe message.
     */
    private void handleMqttsUnsubscribe(MqttsUnsubscribe receivedMsg) {
        if (receivedMsg.getTopicIdType() == MqttsMessage.TOPIC_NAME)
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts UNSUBSCRIBE message with \"TopicName\" = \"" + receivedMsg.getTopicName() + "\" received.");
        else if (receivedMsg.getTopicIdType() == MqttsMessage.PREDIFINED_TOPIC_ID)
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts UNSUBSCRIBE message with \"TopicId\" = \"" + receivedMsg.getPredefinedTopicId() + "\" (predefined topid Id) received.");
        else if (receivedMsg.getTopicIdType() == MqttsMessage.SHORT_TOPIC_NAME)
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts UNSUBSCRIBE message with \"TopicId\" = \"" + receivedMsg.getShortTopicName() + "\" (short topic name) received.");
        else {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts UNSUBSCRIBE message with unknown topicIdType (\"" + receivedMsg.getTopicIdType() + "\") received. The message cannot be processed.");
            return;
        }


        //if the client is not in state "Connected" send to it a Mqtts DISCONNECT message and return
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtts UNSUBSCRIBE message cannot be processed.");
            sendClientDisconnect();
            return;
        }

        //if we are already in an un-subscription process, drop the received message and return
        if (mGatewayState.isWaitingUnsuback(receivedMsg.getMsgId())) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is already in a un-subscription procedure. The received Mqtts UNSUBSCRIBE message cannot be processed.");
            return;
        }

        //else construct a Mqtt UNSUBSCRIBE message
        MqttUnsubscribe mqttUnsubscribe = new MqttUnsubscribe();

        //check the TopicIdType in the received Mqtts UNSUBSCRIBE message
        switch (receivedMsg.getTopicIdType()) {

            //if the TopicIdType is a TopicName
            case MqttsMessage.TOPIC_NAME:
                mqttUnsubscribe.setTopicName(receivedMsg.getTopicName());
                break;

            //if the TopicIdType is a shortTopicName
            case MqttsMessage.SHORT_TOPIC_NAME:
                mqttUnsubscribe.setTopicName(receivedMsg.getShortTopicName());
                break;

            //if the TopicIdType is a predifinedTopiId
            case MqttsMessage.PREDIFINED_TOPIC_ID:
                if (receivedMsg.getPredefinedTopicId() > mGateway.getParameters().getPredfTopicIdSize()) {
                    GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Predefined topicId (\"" + +receivedMsg.getPredefinedTopicId() + "\") of the received Mqtts UNSUBSCRIBE message is out of the range of predefined topic Ids [1," + mGateway.getParameters().getPredfTopicIdSize() + "]. The message cannot be processed.");
                    return;
                }

                String topicName = mTopicMappingTable.getTopicName(receivedMsg.getPredefinedTopicId());

                //this should not happen
                if (topicName == null) {
                    GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Predefined topicId (\"" + +receivedMsg.getPredefinedTopicId() + "\") does not exist. The received Mqtts UNSUBSCRIBE message cannot be processed.");
                    return;
                }
                mqttUnsubscribe.setTopicName(topicName);
                break;

            default:
                GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Unknown topicIdType (\"" + receivedMsg.getTopicIdType() + "\"). The received Mqtts UNSUBSCRIBE message cannot be processed.");
                return;
        }

        //store the received Mqtts UNSUBSCRIBE message (for handling Mqtt UNSUBACK)
//        this.mqttsUnsubscribe = receivedMsg;
        mPendingMqttsUnSubscribe.put(receivedMsg.getMsgId(), receivedMsg);

        // populate the Mqtt UNSUBSCRIBE message with the remaining information from Mqtts UNSUBSCRIBE message
        mqttUnsubscribe.setDup(receivedMsg.isDup());

        mqttUnsubscribe.setMsgId(receivedMsg.getMsgId());

        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtt UNSUBSCRIBE message with \"TopicName\" = \"" + mqttUnsubscribe.getTopicName() + "\" to the broker.");
        //send the Mqtt UNSUBSCRIBE message to the broker
        try {
            mBrokerConnection.sendMqttMessage(mqttUnsubscribe);
        } catch (MqttsException e) {
            e.printStackTrace();
            //if failed sending the message
            GatewayLogger.log(GatewayLogger.ERROR, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Failed sending Mqtt UNSUBSCRIBE message to the broker.");
            connectionLost();
            return;
        }

        //set the mGatewayState on "waitingUnsuback" state
        mGatewayState.setWaitingUnsuback(receivedMsg.getMsgId());
    }


    /**
     * The method that handles a Mqtts PINGREQ message.
     *
     * @param receivedMsg The received MqttsPingReq message.
     */
    private void handleMqttsPingReq(MqttsPingReq receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts PINGREQ message received.");

        //if the client is not in state "Connected" send to it a Mqtts DISCONNECT message and return
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtts PINGREQ message cannot be processed.");
            sendClientDisconnect();
            return;
        }

        //construct a Mqtt PINGREQ message
        MqttPingReq pingreq = new MqttPingReq();

        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtt PINGREQ message to the broker.");
        //send the Mqtt PINGREQ message to the broker
        try {
            mBrokerConnection.sendMqttMessage(pingreq);
        } catch (MqttsException e) {
            e.printStackTrace();
            //if failed sending the message
            GatewayLogger.log(GatewayLogger.ERROR, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Failed sending Mqtt PINGREQ message to the broker.");
            connectionLost();
        }
    }


    /**
     * The method that handles a Mqtts PINGRESP message.
     *
     * @param receivedMsg The received MqttsPingResp message.
     */
    private void handleMqttsPingResp(MqttsPingResp receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts PINGRESP message received.");

        //if the client is not in state "Connected" send to it a Mqtts DISCONNECT message and return
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtts PINGRESP message cannot be processed.");
            sendClientDisconnect();
            return;
        }

        //construct a Mqtt PINGRESP message
        MqttPingResp pingresp = new MqttPingResp();

        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtt PINGRESP message to the broker.");
        //send the Mqtt PINGRESP message to the broker
        try {
            mBrokerConnection.sendMqttMessage(pingresp);
        } catch (MqttsException e) {
            e.printStackTrace();
            //if failed sending the message
            GatewayLogger.log(GatewayLogger.ERROR, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Failed sending Mqtt PINGRESP message to the broker.");
            connectionLost();
        }
    }


    /**
     * The method that handles a Mqtts DISCONNECT message.
     *
     * @param receivedMsg The received MqttsDisconnect message.
     */
    private void handleMqttsDisconnect(MqttsDisconnect receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts DISCONNECT message received.");

        //if the client is not in state "Connected" return
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtts DISCONNECT message cannot be processed.");
            return;
        }

        //else, stop the reading thread of the BrokerInterface
        //(this does not have any effect to the input and output streams which remain active)
        mBrokerConnection.shutdown();

        //construct a Mqtt DISCONNECT message
        MqttDisconnect mqttDisconnect = new MqttDisconnect();

        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtt DISCONNECT message to the broker.");
        //send the Mqtt DISCONNECT message to the broker
        //(no checks - don't bother if the sending of Mqtt DISCONNECT message to the broker was successful or not)
        try {
            mBrokerConnection.sendMqttMessage(mqttDisconnect);
        } catch (MqttsException e) {
            e.printStackTrace();
        }

        //call sendClientDisconnect method of this handler
        sendClientDisconnect();
    }


    /**
     * The method that handles a Mqtts WILLTOPICUPD message.
     *
     * @param receivedMsg The received MqttsWillTopicUpd message.
     */
    private void handleMqttsWillTopicUpd(MqttsWillTopicUpd receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts WILLTOPICUPD message received.");
    }


    /**
     * The method that handles a Mqtts WILLMSGUPD message.
     *
     * @param receivedMsg The received MqttsWillMsgUpd message.
     */
    private void handleMqttsWillMsgUpd(MqttsWillMsgUpd receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtts WILLMSGUPD received.");
    }


    /******************************************************************************************/
    /**                      HANDLING OF MQTT MESSAGES FROM THE BROKER                      **/
    /****************************************************************************************/

	/* (non-Javadoc)
     * @see org.eclipse.paho.mqttsn.mGatewayState.core.MsgHandler#handleMqttMessage(org.eclipse.paho.mqttsn.mGatewayState.messages.mqtt.MqttMessage)
	 */
    public void handleMqttMessage(MqttMessage receivedMsg) {
        //update this handler's timeout
        mTimeout = System.currentTimeMillis() + mGateway.getParameters().getHandlerTimeout() * 1000;

        //get the type of the Mqtt message and handle the message according to that type
        switch (receivedMsg.getMsgType()) {
            case MqttMessage.CONNECT:
                //we will never receive such a message from the broker
                break;

            case MqttMessage.CONNACK:
                handleMqttConnack((MqttConnack) receivedMsg);
                break;

            case MqttMessage.PUBLISH:
                handleMqttPublish((MqttPublish) receivedMsg);
                break;

            case MqttMessage.PUBACK:
                handleMqttPuback((MqttPuback) receivedMsg);
                break;

            case MqttMessage.PUBREC:
                handleMqttPubRec((MqttPubRec) receivedMsg);
                break;

            case MqttMessage.PUBREL:
                handleMqttPubRel((MqttPubRel) receivedMsg);
                break;

            case MqttMessage.PUBCOMP:
                handleMqttPubComp((MqttPubComp) receivedMsg);
                break;

            case MqttMessage.SUBSCRIBE:
                //we will never receive such a message from the broker
                break;

            case MqttMessage.SUBACK:
                handleMqttSuback((MqttSuback) receivedMsg);
                break;

            case MqttMessage.UNSUBSCRIBE:
                //we will never receive such a message from the broker
                break;

            case MqttMessage.UNSUBACK:
                handleMqttUnsuback((MqttUnsuback) receivedMsg);
                break;

            case MqttMessage.PINGREQ:
                handleMqttPingReq((MqttPingReq) receivedMsg);
                break;

            case MqttMessage.PINGRESP:
                handleMqttPingResp((MqttPingResp) receivedMsg);
                break;

            case MqttMessage.DISCONNECT:
                //we will never receive such a message from the broker
                break;

            default:
                GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtt message of unknown type \"" + receivedMsg.getMsgType() + "\" received.");
                break;
        }
    }


    /**
     * The method that handles a Mqtt CONNACK message.
     *
     * @param receivedMsg The received MqttConnack message.
     */
    private void handleMqttConnack(MqttConnack receivedMsg) {
        GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtt CONNACK message received.");

        //if the client is not in state "Connected" drop the received message
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtt CONNACK message cannot be processed.");
            return;
        }

        //if the return code of the Mqtt CONNACK message is not "Connection Accepted", drop the message
        if (receivedMsg.getReturnCode() != MqttMessage.RETURN_CODE_CONNECTION_ACCEPTED) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Return Code of Mqtt CONNACK message it is not \"Connection Accepted\". The received Mqtt CONNACK message cannot be processed.");
            sendClientDisconnect();
            return;
        }

        //else construct a Mqtts CONNACK message
        MqttsConnack msg = new MqttsConnack();
        msg.setReturnCode(MqttsMessage.RETURN_CODE_ACCEPTED);

        //send the Mqtts CONNACK message to the client
        GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts CONNACK message to the mClientState.");
        mClientConnection.sendMsg(this.mClientAddress, msg);
    }


    /**
     * The method that handles a Mqtt PUBLISH message.
     *
     * @param receivedMsg The received MqttPublish message
     */
    private void handleMqttPublish(MqttPublish receivedMsg) {
        GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtt PUBLISH message with \"QoS\" = \"" + receivedMsg.getQos() + "\" and \"TopicName\" = \"" + receivedMsg.getTopicName() + "\" received.");

        //if the client is not in state "Connected" drop the received message
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtt PUBLISH message cannot be processed.");
            return;
        }

        //if the data is too long to fit into a Mqtts PUBLISH message or the topic name is too
        //long to fit into a Mqtts REGISTER message then drop the received message
        if (receivedMsg.getPayload().length > mGateway.getParameters().getMaxMqttsLength() - 7) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - The payload in the received Mqtt PUBLISH message does not fit into a Mqtts PUBLISH message (payload length = " + receivedMsg.getPayload().length + ". The message cannot be processed.");
            return;
        }

        if (receivedMsg.getTopicName().length() > mGateway.getParameters().getMaxMqttsLength() - 6) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - The topic name in the received Mqtt PUBLISH message does not fit into a Mqtts REGISTER message (topic name length = " + receivedMsg.getTopicName().length() + ". The message cannot be processed.");
            return;
        }


        //get the corresponding topicId from the topicName of the Mqtt PUBLISH message
        int topicId = mTopicMappingTable.getTopicId(receivedMsg.getTopicName());

        GatewayLogger.log(GatewayLogger.WARN, " ======= topicId:" + topicId + " type:" + receivedMsg.getTopicName());

        //construct a Mqtts PUBLISH message
        MqttsPublish publish = new MqttsPublish();

        //if topicId exists then accept the Mqtt PUBLISH and send a Mqtts PUBLISH to the client
        if (topicId != 0) {
            //populate the Mqtts PUBLISH message with the information from the Mqtt PUBLISH message
            publish.setDup(receivedMsg.isDup());
            publish.setQos(receivedMsg.getQos());
            publish.setRetain(receivedMsg.isRetain());
            publish.setMsgId(receivedMsg.getMsgId());
            publish.setData(receivedMsg.getPayload());


            //check if the retrieved topicID is associated with a normal topicName
            if (topicId > mGateway.getParameters().getPredfTopicIdSize()) {
                publish.setTopicIdType(MqttsMessage.NORMAL_TOPIC_ID);
                publish.setTopicId(topicId);
                GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts PUBLISH message with \"QoS\" = \"" + receivedMsg.getQos() + "\" and \"TopicId\" = \"" + topicId + "\" to the mClientState.");

            }
            //or a predefined topic Id
            else if (topicId > 0 && topicId <= mGateway.getParameters().getPredfTopicIdSize()) {
                publish.setTopicIdType(MqttsMessage.PREDIFINED_TOPIC_ID);
                publish.setTopicId(topicId);
                GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts PUBLISH message with \"QoS\" = \"" + receivedMsg.getQos() + "\" and \"TopicId\" = \"" + topicId + "\" to the mClientState.");

            }

            mClientConnection.sendMsg(this.mClientAddress, publish);
            return;
        }

        //handle the case of short topic names
        if (topicId == 0 && receivedMsg.getTopicName().length() == 2) {
            publish.setTopicIdType(MqttsMessage.SHORT_TOPIC_NAME);
            publish.setShortTopicName(receivedMsg.getTopicName());
            publish.setDup(receivedMsg.isDup());
            publish.setQos(receivedMsg.getQos());
            publish.setRetain(receivedMsg.isRetain());
            publish.setMsgId(receivedMsg.getMsgId());
            publish.setData(receivedMsg.getPayload());
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts PUBLISH message with \"QoS\" = \"" + receivedMsg.getQos() + "\" and \"TopicId\" = \"" + receivedMsg.getTopicName() + "\" (short topic name) to the mClientState.");
            return;
        }

        //if topicId doesn't exist and we are not already in a register procedure initiated by
        //the mGatewayState, then store the Mqtts PUBLISH and send a Mqtts REGISTER to the client
        //TODO:取消掉MQTT的反注册说法,以下全部代码到retrun
//        if (topicId == 0 && !mGatewayState.isWaitingRegack(topicId)) {
//            //store the Mqtt PUBLISH message
////            this.mqttPublish = receivedMsg;
//            mPendingMqttPublish.put(topicId, receivedMsg);
//
//            //get a new topicId (don't assign it until we get the REGACK message!)
//            topicId = getNewTopicId();
//
//            //construct a Mqtts REGISTER message and store it (for comparing later the MsgId)
//            MqttsRegister pendingRegister = new MqttsRegister();
//            pendingRegister.setTopicId(topicId);
//            pendingRegister.setMsgId(getNewMsgId());
//            pendingRegister.setTopicName(receivedMsg.getTopicName());
//
//            //send the Mqtts REGISTER message to the client
//            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts REGISTER message with \"TopicId\" = \"" + topicId + "\"  and \"TopicName\" = \"" + receivedMsg.getTopicName() + "\" to the mClientState.");
//            mClientConnection.sendMsg(this.mClientAddress, pendingRegister);
//
//            //set the mGatewayState on "waitingRegack" state and increase
//            //the tries of sending Mqtts REGISTER message to the client
//            mGatewayState.setWaitingRegack(topicId);
//            mGatewayState.increaseTriesSendingRegister(topicId);
//
//            //set a timeout for waiting a Mqtts REGACK message from the client by registering to the timer
//            mTimerService.register(this.mClientAddress, ControlMessage.WAITING_REGACK_TIMEOUT, mGateway.getParameters().getWaitingTime(), topicId);
//            return;
//        }

        //if topicId doesn't exist and we are already in a register procedure initiated by
        //the mGatewayState, then drop the received Mqtt PUBLISH message
        if (topicId == 0 && mGatewayState.isWaitingRegack(topicId)) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Topic name (\"" + receivedMsg.getTopicName() + "\") does not exist in the mapping table and the mGatewayState is waiting a Mqtts REGACK message from the mClientState. The received Mqtt PUBLISH message cannot be processed.");
            return;
        }
    }


    /**
     * The method that handles a Mqtt PUBACK message.
     *
     * @param receivedMsg The received MqttPuback message.
     */
    private void handleMqttPuback(MqttPuback receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtt PUBACK message received.");

        //if the client is not in state "Connected" drop the received message
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtt PUBACK message cannot be processed.");
            return;
        }

        //if the mGatewayState is not expecting a Mqtt PUBACK at this time, drop the received message and return
        if (!mGatewayState.isWaitingPuback(receivedMsg.getMsgId())) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Gateway is not waiting a Mqtt PUBACK message from the broker.The received message cannot be processed.");
            return;
        }

        //else, assure that the stored Mqtts PUBLISH is not null (debugging checks)
        if (mPendingMqttsPublish.get(receivedMsg.getMsgId()) == null) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - The stored Mqtts PUBLISH message is null.The received Mqtt PUBACK message cannot be processed.");

            //"reset" the "waitingPuback" state of the mGatewayState
            mGatewayState.resetWaitingPuback(receivedMsg.getMsgId());
            return;
        }

        MqttsPublish pendingMqttsPublish = mPendingMqttsPublish.get(receivedMsg.getMsgId());

        //if the MsgId of the received Mqtt PUBACK is not the same with MsgId of the stored
        //Mqtts PUBLISH message, drop the received message and return (don't delete any stored message)
        if (receivedMsg.getMsgId() != pendingMqttsPublish.getMsgId()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Message ID of the received Mqtt PUBACK does not match the message ID of the stored Mqtts PUBLISH message.The message cannot be processed.");
            return;
        }

        //construct a Mqtts PUBACK message
        MqttsPuback puback = new MqttsPuback();

        puback.setMsgId(receivedMsg.getMsgId());
        puback.setReturnCode(MqttsMessage.RETURN_CODE_ACCEPTED);


        //check the TopicIdType in the stored Mqtts PUBLISH message
        switch (pendingMqttsPublish.getTopicIdType()) {

            //if the TopicIdType is a normal TopicId
            case MqttsMessage.NORMAL_TOPIC_ID:
                puback.setTopicId(pendingMqttsPublish.getTopicId());
                GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts PUBACK message with \"TopicId\" = \"" + puback.getTopicId() + "\" to the mClientState.");
                break;

            //if the TopicIdType is a shortTopicName
            case MqttsMessage.SHORT_TOPIC_NAME:
                puback.setShortTopicName(pendingMqttsPublish.getShortTopicName());
                GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts PUBACK message with \"TopicId\" = \"" + puback.getShortTopicName() + "\" (short topic name) to the mClientState.");

                break;

            //if the TopicIdType is a predifinedTopiId
            case MqttsMessage.PREDIFINED_TOPIC_ID:
                puback.setTopicId(pendingMqttsPublish.getTopicId());
                GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts PUBACK message with \"TopicId\" = \"" + puback.getTopicId() + "\" to the mClientState.");

                break;

            //should never reach here because topicIdType was checked
            //already when we received the Mqtts PUBLISH message
            default:
                GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Unknown topicIdType of the stored Mqtts PUBLISH message: " + pendingMqttsPublish.getTopicIdType() + ". The received Mqtt PUBACK message cannot be processed.");
                return;
        }

        //send the Mqtts PUBACK message to the client
        mClientConnection.sendMsg(this.mClientAddress, puback);

        //"reset" the "waitingPuback" state of the mGatewayState and delete Mqtts PUBLISH message
        mGatewayState.resetWaitingPuback(pendingMqttsPublish.getMsgId());
        mPendingMqttsPublish.remove(pendingMqttsPublish.getMsgId());
//        this.mqttsPublish = null;
    }


    /**
     * The method that handles a Mqtt PUBREC message.
     *
     * @param receivedMsg The received MqttPubRec message.
     */
    private void handleMqttPubRec(MqttPubRec receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtt PUBREC message received.");

        //if the client is not in state "Connected" drop the received message
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtt PUBREC message cannot be processed.");
            return;
        }

        //construct a Mqtts PUBREC message
        MqttsPubRec msg = new MqttsPubRec();
        msg.setMsgId(receivedMsg.getMsgId());

        //send the Mqtts PUBREC message to the client
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts PUBREC message to the mClientState.");
        mClientConnection.sendMsg(this.mClientAddress, msg);
    }


    /**
     * The method that handles a Mqtt PUBREL message.
     *
     * @param receivedMsg The received MqttPubRel message.
     */
    private void handleMqttPubRel(MqttPubRel receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtt PUBREL message received.");

        //if the client is not in state "Connected" drop the received message
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtt PUBREL message cannot be processed.");
            return;
        }

        //construct a Mqtts PUBREL message
        MqttsPubRel msg = new MqttsPubRel();
        msg.setMsgId(receivedMsg.getMsgId());

        //send the Mqtts PUBREL message to the client
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts PUBREL message to the mClientState.");
        mClientConnection.sendMsg(this.mClientAddress, msg);

    }


    /**
     * The method that handles a Mqtt PUBCOMP message.
     *
     * @param receivedMsg The received MqttPubComp message.
     */
    private void handleMqttPubComp(MqttPubComp receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtt PUBCOMP message received.");

        //if the client is not in state "Connected" drop the received message
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtt PUBCOMP message cannot be processed.");
            return;
        }

        //construct a Mqtts PUBCOMP message
        MqttsPubComp msg = new MqttsPubComp();
        msg.setMsgId(receivedMsg.getMsgId());

        //send the Mqtts PUBCOMP message to the client
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts PUBCOMP message to the mClientState.");
        mClientConnection.sendMsg(this.mClientAddress, msg);
    }


    /**
     * The method that handles a Mqtt SUBACK message.
     *
     * @param receivedMsg The received MqttSuback message.
     */
    private void handleMqttSuback(MqttSuback receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtt SUBACK message received.");

        //if the client is not in state "Connected" drop the received message
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtt SUBACK message cannot be processed.");
            return;
        }

        //if the mGatewayState is not expecting a Mqtt SUBACK at this time, drop the received message and return
        if (!mGatewayState.isWaitingSuback(receivedMsg.getMsgId())) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Gateway is not waiting a Mqtt SUBACK message from the broker. The received message cannot be processed.");
            return;
        }

        //else, assure that the stored Mqtts SUBSCRIBE is not null (debugging checks)
        if (mPendingMqttsSubscribe.get(receivedMsg.getMsgId()) == null) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - The stored Mqtts SUBSCRIBE is null. The received Mqtt SUBACK message cannot be processed.");

            //"reset" the "waitingSuback" state of the mGatewayState
            mGatewayState.resetWaitingSuback(receivedMsg.getMsgId());
            return;
        }

        MqttsSubscribe pendingSubscribe = mPendingMqttsSubscribe.get(receivedMsg.getMsgId());

        //if the MsgId of the received Mqtt SUBACK is not the same with MsgId of the stored
        //Mqtts SUBSCRIBE message, drop the received message and return (don't delete any stored message)
        if (receivedMsg.getMsgId() != pendingSubscribe.getMsgId()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - MsgId (\"" + receivedMsg.getMsgId() + "\") of the received Mqtts SUBACK message does not match the MsgId (\"" + pendingSubscribe.getMsgId() + "\") of the stored Mqtts SUBSCRIBE message. The message cannot be processed.");
            return;
        }

        //else construct a Mqtts SUBACK message
        MqttsSuback suback = new MqttsSuback();

        //populate the Mqtts SUBACK message with the information from the Mqtt SUBACK message
        suback.setGrantedQoS(receivedMsg.getGrantedQoS());
        suback.setMsgId(receivedMsg.getMsgId());
        suback.setReturnCode(MqttsMessage.RETURN_CODE_ACCEPTED);


        //check the TopicIdType in the stored Mqtts SUBSCRIBE message
        switch (pendingSubscribe.getTopicIdType()) {

            //if the TopicIdType is a TopicName
            case MqttsMessage.TOPIC_NAME:
                suback.setTopicIdType(MqttsMessage.NORMAL_TOPIC_ID);

                //if contains wildcard characters
                if (pendingSubscribe.getTopicName().equals("#")
                        || pendingSubscribe.getTopicName().equals("+")
                        || pendingSubscribe.getTopicName().contains("/#/")
                        || pendingSubscribe.getTopicName().contains("/+/")
                        || pendingSubscribe.getTopicName().endsWith("/#")
                        || pendingSubscribe.getTopicName().endsWith("/+")
                        || pendingSubscribe.getTopicName().startsWith("#/")
                        || pendingSubscribe.getTopicName().startsWith("+/")) {
                    //set the topicId of the Mqtts SUBACK message to 0x0000
                    suback.setTopicId(0);
                } else if (mTopicMappingTable.getTopicId(pendingSubscribe.getTopicName()) != 0)
                    //if topic id already exists
                    suback.setTopicId(mTopicMappingTable.getTopicId(pendingSubscribe.getTopicName()));
                else {
                    //assign a new topicID to the topic name
                    int topicId = getNewTopicId();
                    mTopicMappingTable.assignTopicId(topicId, pendingSubscribe.getTopicName());
                    suback.setTopicId(topicId);
                }
                GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts SUBACK message with \"TopicId\" = \"" + suback.getTopicId() + "\" to the mClientState.");

                break;

            //if the TopicIdType is a shortTopicName
            case MqttsMessage.SHORT_TOPIC_NAME:
                suback.setTopicIdType(MqttsMessage.SHORT_TOPIC_NAME);
                suback.setShortTopicName(pendingSubscribe.getShortTopicName());
                GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts SUBACK message with \"TopicId\" = \"" + suback.getShortTopicName() + "\" (short topic name) to the mClientState.");

                break;

            //if the TopicIdType is a predifinedTopiId
            case MqttsMessage.PREDIFINED_TOPIC_ID:
                suback.setTopicIdType(MqttsMessage.PREDIFINED_TOPIC_ID);
                suback.setPredefinedTopicId(pendingSubscribe.getPredefinedTopicId());
                GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts SUBACK message with \"TopicId\" = \"" + suback.getPredefinedTopicId() + "\" to the mClientState.");

                break;

            //should never reach here because topicIdType was checked
            //already when we received the Mqtts SUBSCRIBE message
            default:
                GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - UnknownTopicId type of the stored Mqtts SUBSCRIBE message: " + pendingSubscribe.getTopicIdType() + ". The received Mqtt SUBACK message cannot be processed.");
                return;
        }

        GatewayLogger.log(GatewayLogger.INFO, " <!> topicid:" + suback.getTopicId() + " " + pendingSubscribe.getTopicName());

        //send the Mqtts SUBACK message to the client
        mClientConnection.sendMsg(this.mClientAddress, suback);

        //"reset" the "waitingSuback" state of the mGatewayState and delete Mqtts SUBSCRIBE message
        mGatewayState.resetWaitingSuback(receivedMsg.getMsgId());
        mPendingMqttsSubscribe.remove(receivedMsg.getMsgId());
    }


    /**
     * The method that handles a Mqtt UNSUBACK message.
     *
     * @param receivedMsg The received MqttUnsuback message.
     */
    private void handleMqttUnsuback(MqttUnsuback receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtt UNSUBACK message received.");

        //if the client is not in state "Connected" drop the received message
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtt UNSUBACK message cannot be processed.");
            return;
        }

        //if the mGatewayState is not expecting a Mqtt UNSUBACK at this time, drop the received message and return
        if (!mGatewayState.isWaitingUnsuback(receivedMsg.getMsgId())) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Gateway is not waiting a Mqtt UNSUBACK message from the broker.The received message cannot be processed.");
            return;
        }

        //else, assure that the stored Mqtts UNSUBSCRIBE is not null (debugging checks)
        if (mPendingMqttsUnSubscribe.get(receivedMsg.getMsgId()) == null) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - The stored Mqtts UNSUBSCRIBE is null.The received Mqtt UNSUBACK message cannot be processed.");

            //"reset" the "waitingUnsuback" state of the mGatewayState
            mGatewayState.resetWaitingUnsuback(receivedMsg.getMsgId());
            return;
        }

        MqttsUnsubscribe pendingMqttsUnsubscribe = mPendingMqttsUnSubscribe.get(receivedMsg.getMsgId());

        //if the MsgId of the received Mqtt UNSUBACK is not the same with MsgId of the stored
        //Mqtts UNSUBSCRIBE message, drop the received message and return (don't delete any stored message)
        if (receivedMsg.getMsgId() != pendingMqttsUnsubscribe.getMsgId()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - MsgId (\"" + receivedMsg.getMsgId() + "\") of the received Mqtts UNSUBACK message does not match the MsgId (\"" + pendingMqttsUnsubscribe.getMsgId() + "\") of the stored Mqtts UNSUBSCRIBE message. The message cannot be processed.");
            return;
        }

        //else remove the associated topicId from the mapping table
        if (!(pendingMqttsUnsubscribe.getTopicIdType() == MqttsMessage.SHORT_TOPIC_NAME || pendingMqttsUnsubscribe.getTopicIdType() == MqttsMessage.PREDIFINED_TOPIC_ID))
            mTopicMappingTable.removeTopicId(pendingMqttsUnsubscribe.getTopicName());

        //construct a Mqtts UNSUBACK message
        MqttsUnsuback unsuback = new MqttsUnsuback();

        //set the msgId of Mqtts UNSUBACK message with the information from the Mqtt UNSUBACK message
        unsuback.setMsgId(receivedMsg.getMsgId());

        //send the Mqtts SUBACK message to the client
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts UNSUBACK message to the mClientState.");
        mClientConnection.sendMsg(this.mClientAddress, unsuback);

        //"reset" the "waitingUnsuback" state of the mGatewayState and delete Mqtts UNSUBSCRIBE message
        mGatewayState.resetWaitingUnsuback(receivedMsg.getMsgId());
        mPendingMqttsUnSubscribe.remove(receivedMsg.getMsgId());
//        mqttsUnsubscribe = null;
    }


    /**
     * The method that handles a Mqtt PINGREQ message.
     *
     * @param receivedMsg The received MqttPingReq message.
     */
    private void handleMqttPingReq(MqttPingReq receivedMsg) {
        GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtt PINGREQ message received.");

        //if the client is not in state "Connected" drop the received message
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtt PINGREQ message cannot be processed.");
            return;
        }

        //construct a Mqtts PINGREQ message
        MqttsPingReq msg = new MqttsPingReq();

        //send the Mqtts PINGREQ message to the client
        GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts PINGREQ message to the mClientState.");
        mClientConnection.sendMsg(this.mClientAddress, msg);
    }


    /**
     * The method that handles a Mqtt PINGRESP message.
     *
     * @param receivedMsg The received MqttPingResp message.
     */
    private void handleMqttPingResp(MqttPingResp receivedMsg) {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Mqtt PINGRESP message received.");

        //if the client is not in state "Connected" drop the received message
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Mqtt PINGRESP message cannot be processed.");
            return;
        }

        //construct a Mqtts PINGRESP message
        MqttsPingResp msg = new MqttsPingResp();

        //send the Mqtts PINGRESP message to the client
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts PINGRESP message to the mClientState.");
        mClientConnection.sendMsg(this.mClientAddress, msg);
    }


    /******************************************************************************************/
    /**                        HANDLING OF CONTROL MESSAGES AND TIMEOUTS	                **/
    /****************************************************************************************/

	/* (non-Javadoc)
     * @see org.eclipse.paho.mqttsn.mGatewayState.core.MsgHandler#handleControlMessage(org.eclipse.paho.mqttsn.mGatewayState.messages.control.ControlMessage)
	 */
    public void handleControlMessage(ControlMessage receivedMsg) {
        //get the type of the Control message and handle the message according to that type
        switch (receivedMsg.getMsgType()) {
            case ControlMessage.CONNECTION_LOST:
                connectionLost();
                break;

            case ControlMessage.WAITING_WILLTOPIC_TIMEOUT:
                handleWaitingWillTopicTimeout();
                break;

            case ControlMessage.WAITING_WILLMSG_TIMEOUT:
                handleWaitingWillMsgTimeout();
                break;

            case ControlMessage.WAITING_REGACK_TIMEOUT:
                handleWaitingRegackTimeout(receivedMsg);
                break;

            case ControlMessage.CHECK_INACTIVITY:
                handleCheckInactivity();
                break;

            case ControlMessage.SEND_KEEP_ALIVE_MSG:
                //we will never receive such a message
                break;

            case ControlMessage.SEND_ADVERTISE_MSG:
                //we will never receive such a message
                break;

            case ControlMessage.SHUT_DOWN:
                shutDown();
                break;

            default:
                GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Control message of unknown type \"" + receivedMsg.getMsgType() + "\" received.");
                break;
        }
    }


    /**
     * The method that is invoked when the TCP/IP connection with the broker was lost.
     */
    private void connectionLost() {
        GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Control CONNECTION_LOST message received.");

        //if the client is not in state "Connected" return
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The call on connectionLost() method has no effect.");
            return;
        }

        GatewayLogger.log(GatewayLogger.ERROR, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - TCP/IP connection with the broker was lost.");

        //call the sendClientDisconnect method of this handler
        sendClientDisconnect();
    }


    /**
     * The method that is invoked when waiting for a Mqtts WILLTOPIC message from
     * the client has timeout.
     */
    private void handleWaitingWillTopicTimeout() {
        GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Control WAITING_WILLTOPIC_TIMEOUT message received.");

        //check if the mGatewayState is still in state of waiting for a WILLTOPIC message from the client
        if (!mGatewayState.isWaitingWillTopic()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Gateway is not waiting a Mqtts WILLTOPIC message from the mClientState. The received control WAITING_WILLTOPIC_TIMEOUT message cannot be processed.");
            return;
        }

        //if we have reached the maximum tries of sending Mqtts WILLTOPICREQ message
        if (mGatewayState.getTriesSendingWillTopicReq() > mGateway.getParameters().getMaxRetries()) {
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Maximum retries of sending Mqtts WILLTOPICREQ message to the mClientState were reached. The message will not be sent again.");

            //"reset" the "waitingWillTopic" state of the mGatewayState, "reset" the tries of sending
            //Mqtts WILLTOPICREQ message to the client, unregister from the timer and and delete
            //the stored Mqtts CONNECT message
            mGatewayState.resetWaitingWillTopic();
            mGatewayState.resetTriesSendingWillTopicReq();
            mTimerService.unregister(this.mClientAddress, ControlMessage.WAITING_WILLTOPIC_TIMEOUT);
            this.mqttsConnect = null;

            //else construct a Mqtts WILTOPICREQ, and send it to the client
        } else {
            MqttsWillTopicReq willTopicReq = new MqttsWillTopicReq();

            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Re-sending Mqtts WILLTOPICREQ message to the mClientState. Retry: " + mGatewayState.getTriesSendingWillTopicReq() + ".");
            mClientConnection.sendMsg(this.mClientAddress, willTopicReq);

            //increase the tries of sending Mqtts WILLTOPICREQ message to the client
            mGatewayState.increaseTriesSendingWillTopicReq();
        }
    }


    /**
     * The method that is invoked when waiting for a Mqtts WILLMSG message from
     * the client has timeout.
     */
    private void handleWaitingWillMsgTimeout() {
        GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Control WAITING_WILLMSG_TIMEOUT message received.");

        //check if the mGatewayState is still in state of waiting for a WILLMSG message from the client
        if (!mGatewayState.isWaitingWillMsg()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Gateway is not waiting a Mqtts WILLMSG message from the mClientState. The received control WAITING_WILLMSG_TIMEOUT message cannot be processed.");
            return;
        }

        //if we have reached the maximum tries of sending Mqtts WILLMSGREQ message
        if (mGatewayState.getTriesSendingWillMsgReq() > mGateway.getParameters().getMaxRetries()) {
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Maximum retries of sending Mqtts WILLMSGREQ message to the mClientState were reached. The message will not be sent again.");
            //"reset" the "waitingWillMsg" state of the mGatewayState, "reset" the tries of sending
            //Mqtts WILLMSGREQ message to the client, unregister from the timer and delete
            //the stored Mqtts CONNECT and Mqtts WILLTOPIC messages
            mGatewayState.resetWaitingWillMsg();
            mGatewayState.resetTriesSendingWillMsgReq();
            mTimerService.unregister(this.mClientAddress, ControlMessage.WAITING_WILLMSG_TIMEOUT);
            this.mqttsConnect = null;
            this.mqttsWillTopic = null;

            //else construct a Mqtts WILMSGREQ and send it to the client
        } else {
            MqttsWillMsgReq willMsgReq = new MqttsWillMsgReq();

            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Re-sending Mqtts WILLMSGREQ message to the mClientState. Retry: " + mGatewayState.getTriesSendingWillMsgReq() + ".");
            mClientConnection.sendMsg(this.mClientAddress, willMsgReq);

            //increase the tries of sending Mqtts WILLMSGREQ message to the client
            mGatewayState.increaseTriesSendingWillMsgReq();
        }
    }


    /**
     * The method that is invoked when waiting for a Mqtts REGACK message from
     * the client has timeout.
     *
     * @param receivedMsg
     */
    private void handleWaitingRegackTimeout(ControlMessage receivedMsg) {
        GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Control WAITING_REGACK_TIMEOUT message received.");

        int topicId = -1;
        if (receivedMsg.getData() instanceof Integer) {
            topicId = (int) receivedMsg.getData();
        }

        //check if the mGatewayState is still in state of waiting for a REGACK message from the client
        if (topicId < 0 || !mGatewayState.isWaitingRegack(topicId)) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Gateway is not in state of waiting a Mqtts REGACK message from the mClientState. The received control REGACK_TIMEOUT message cannot be processed.");
            return;
        }

        MqttsRegister pendingRegister = mPendingMqttsRegister.get(topicId);
        //if we have reached the maximum tries of sending the Mqtts REGISTER message
        if (mGatewayState.getTriesSendingRegister(topicId) > mGateway.getParameters().getMaxRetries()) {
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Maximum retries of sending Mqtts REGISTER message to the mClientState were reached. The message will not be sent again.");
            //"reset" the 'waitingRegack" state of the mGatewayState, "reset" the tries of sending
            //Mqtts REGISTER message to the client, unregister from the timer and delete
            //the stored Mqtt PUBLISH and Mqtts REGISTER messages
            mGatewayState.resetWaitingRegack(topicId);
            mGatewayState.resetTriesSendingRegister(topicId);
            mTimerService.unregister(this.mClientAddress, ControlMessage.WAITING_REGACK_TIMEOUT);
//            this.mqttPublish = null;
            mPendingMqttPublish.remove(topicId);
//            this.mqttsRegister = null;
            mPendingMqttsRegister.remove(topicId);
        }
        //else modify the MsgId (get a new one) of the stored Mqtts
        //REGISTER message, and send it to the client
        else if (pendingRegister != null) {
            pendingRegister.setMsgId(getNewMsgId());

            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Re-sending Mqtts REGISTER message to the mClientState. Retry: " + mGatewayState.getTriesSendingRegister(topicId) + ".");
            mClientConnection.sendMsg(this.mClientAddress, mPendingMqttsRegister.get(topicId));
            mGatewayState.increaseTriesSendingRegister(topicId);
        } else {
            GatewayLogger.log(GatewayLogger.WARN, "null pending topicId:" + topicId);
        }
    }


    /**
     * This method is invoked in regular intervals to check the inactivity of this handler
     * in order to remove it from Dispatcher's mapping table.
     */
    private void handleCheckInactivity() {
        GatewayLogger.log(GatewayLogger.INFO,
                "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() +
                        "]/[" + mClientId + "] - Control CHECK_INACTIVITY message received.");

        if (System.currentTimeMillis() > this.mTimeout) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is inactive for more than " + mGateway.getParameters().getHandlerTimeout() / 60 + " minutes. The associated ClientMsgHandler will be removed from Dispatcher's mapping table.");

            //close broker connection (if any)
            mBrokerConnection.disconnect();

            mDispatcher.removeHandler(this.mClientAddress);
        }
    }

    /**
     * This method is invoked when the mGatewayState is shutting down.
     */
    private void shutDown() {
        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Control SHUT_DOWN message received.");

        //if the client is not in state "Connected" return
        if (!mClientState.isConnected()) {
            GatewayLogger.log(GatewayLogger.WARN, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Client is not connected. The received Control SHUT_DOWN message cannot be processed.");
            return;
        }

        //stop the reading thread of the BrokerInterface
        //(this does not have any effect to the input and output streams which remain active)
        mBrokerConnection.shutdown();

        //construct a Mqtt DISCONNECT message
        MqttDisconnect mqttDisconnect = new MqttDisconnect();

        GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtt DISCONNECT message to the broker.");

        sendClientDisconnect();
    }


    /******************************************************************************************/
    /**                                   OTHER METHODS AND CLASSES       	                **/
    /****************************************************************************************/


    /**
     * The method that sets the Client interface in which this handler should respond in case
     * of sending a Mqtts message to the client.
     *
     * @param clientConnection
     */
    public void setClientConnection(ClientConnection clientConnection) {
        this.mClientConnection = clientConnection;
    }

    public ClientAddress getClientAddress() {
        return mClientAddress;
    }

    /**
     * 重置消息对象,相比于正常的断开,不发送断开连接的消息给设备
     */
    public void resetClientMsgHandler() {
        //不需要断开当前client的连接,此处并不会通知到设备断开连接
        //取消监听事件
        mTimerService.unregister(this.mClientAddress);

        //重置数据信息
        mGatewayState.reset();
        //重置topic数据
        mTopicMappingTable.reset();

        mqttsConnect = null;
        mqttsWillTopic = null;

        mPendingMqttsPublish.clear();
        mPendingMqttsRegister.clear();
        mPendingMqttsSubscribe.clear();
        mPendingMqttsUnSubscribe.clear();
        mPendingMqttPublish.clear();
    }

    /**
     * This method sends a Mqtts DISCONNECT message to the client.
     */
    private void sendClientDisconnect() {
        if (mClientConnection != null) {
            //construct a Mqtts DISCONNECT message
            MqttsDisconnect mqttsDisconnect = new MqttsDisconnect();

            //send the Mqtts DISCONNECT message to the client
            GatewayLogger.log(GatewayLogger.INFO, "ClientMsgHandler [" + this.mClientAddress.getIpAddressInfo() + "]/[" + mClientId + "] - Sending Mqtts DISCONNECT message to the mClientState.");
            mClientConnection.sendMsg(this.mClientAddress, mqttsDisconnect);
        }

        //set the state of the client to "Disconnected"
        mClientState.setDisconnected();

        //remove timer registrations (if any)
        mTimerService.unregister(this.mClientAddress);

        //"reset" all the states and the retry counters of the mGatewayState
        mGatewayState.reset();

        //delete also all stored messages (if any)
        mqttsConnect = null;
        mqttsWillTopic = null;
//        mqttsSubscribe = null;
//        mqttsUnsubscribe = null;
//        mqttsRegister = null;
//        mqttsPublish = null;
//        mqttPublish = null;

        mPendingMqttsPublish.clear();
        mPendingMqttsRegister.clear();
        mPendingMqttsSubscribe.clear();
        mPendingMqttsUnSubscribe.clear();
        mPendingMqttPublish.clear();

        //close the connection with the broker (if any)
        mBrokerConnection.disconnect();
        //mTopicMappingTable.printContent();
    }


    /**
     * This method generates and return a unique message ID.
     *
     * @return The message ID
     */
    private int getNewMsgId() {
        return msgId.getAndIncrement();
    }

    /**
     * This method generates and return a unique topic ID.
     *
     * @return A unique topic ID
     */
    private int getNewTopicId() {
        return topicId.getAndIncrement();
    }

    /**
     * add the increment to the topicId and get the newest number
     *
     * @param increment increment for topicId
     * @return the newest number after increasing
     */
    private int updateTopicId(int increment) {
        return topicId.addAndGet(increment);
    }


    public void setStateListener(ClientMsgHandlerStateListener stateListener) {
        mStateListener = stateListener;
    }

    /**
     * The class that represents the state of the client at any given time.
     */
    private class ClientState {

        private final int NOT_CONNECTED = 1;
        private final int CONNECTED = 2;
        private final int DISCONNECTED = 3;

        private int state;

        public ClientState() {
            state = NOT_CONNECTED;
        }

//		public boolean isNotConnected() {
//			return (state == NOT_CONNECTED);
//		}

//		public void setNotConnected() {
//			state = NOT_CONNECTED;
//		}

        public boolean isConnected() {
            return (state == CONNECTED);
        }

        public void setConnected() {
            state = CONNECTED;
            if (mStateListener != null) {
                mStateListener.onClientConnected(ClientMsgHandler.this);
            }
        }

//		public boolean isDisconnected() {
//			return (state == DISCONNECTED);
//		}

        public void setDisconnected() {
            state = DISCONNECTED;
            if (mStateListener != null) {
                mStateListener.onClientDisconnected(ClientMsgHandler.this);
            }
        }
    }


    /**
     * The class that represents the state of the mGatewayState at any given time.
     */
    private static class GatewayState {

        private HashMap<Integer, Boolean> mWaitingRegAck;
        private HashMap<Integer, Boolean> mWaitingSubAck;
        private HashMap<Integer, Boolean> mWaitingUnSubAck;
        private HashMap<Integer, Boolean> mWaitingPubAck;

        private HashMap<Integer, Integer> mTriesSendingRegister;

        //waiting message from the client
        private boolean waitingWillTopic;
        private boolean waitingWillMsg;

        //counters
        private int triesSendingWillTopicReq;
        private int triesSendingWillMsgReq;

        public GatewayState() {
            mWaitingRegAck = new HashMap<>();

            mWaitingSubAck = new HashMap<>();
            mWaitingUnSubAck = new HashMap<>();
            mWaitingPubAck = new HashMap<>();

            mTriesSendingRegister = new HashMap<>();

            this.waitingWillTopic = false;
            this.waitingWillMsg = false;

            this.triesSendingWillTopicReq = 0;
            this.triesSendingWillMsgReq = 0;
        }


        public void reset() {
            mWaitingRegAck.clear();

            mWaitingSubAck.clear();
            mWaitingUnSubAck.clear();
            mWaitingPubAck.clear();
            mTriesSendingRegister.clear();

            this.waitingWillTopic = false;
            this.waitingWillMsg = false;

            this.triesSendingWillTopicReq = 0;
            this.triesSendingWillMsgReq = 0;
        }


        public boolean isEstablishingConnection() {
            return (isWaitingWillTopic() || isWaitingWillMsg());
        }


        public boolean isWaitingWillTopic() {
            return this.waitingWillTopic;
        }

        public void setWaitingWillTopic() {
            this.waitingWillTopic = true;
        }

        public void resetWaitingWillTopic() {
            this.waitingWillTopic = false;
        }


        public boolean isWaitingWillMsg() {
            return this.waitingWillMsg;
        }

        public void setWaitingWillMsg() {
            this.waitingWillMsg = true;
        }

        public void resetWaitingWillMsg() {
            this.waitingWillMsg = false;
        }


        public boolean isWaitingRegack(int topicId) {
            Boolean value = mWaitingRegAck.get(topicId);
            return value != null && value;
        }

        public void setWaitingRegack(int topicId) {
            mWaitingRegAck.put(topicId, true);
        }

        public void resetWaitingRegack(int topicId) {
            mWaitingRegAck.remove(topicId);
        }


        public boolean isWaitingSuback(int msgId) {
            Boolean value = mWaitingSubAck.get(msgId);
            return value != null && value;
        }

        public void setWaitingSuback(int msgId) {
            mWaitingSubAck.put(msgId, true);
        }

        public void resetWaitingSuback(int msgId) {
            mWaitingSubAck.remove(msgId);
        }


        public boolean isWaitingUnsuback(int msgId) {
            Boolean value = mWaitingUnSubAck.get(msgId);
            return value != null && value;
        }

        public void setWaitingUnsuback(int msgId) {
            mWaitingUnSubAck.put(msgId, true);
        }

        public void resetWaitingUnsuback(int msgId) {
            mWaitingUnSubAck.remove(msgId);
        }


        public boolean isWaitingPuback(int msgId) {
            Boolean value = mWaitingPubAck.get(msgId);
            return value != null && value;
        }

        public void setWaitingPuback(int msgId) {
            mWaitingPubAck.put(msgId, true);
        }

        public void resetWaitingPuback(int msgId) {
            mWaitingPubAck.remove(msgId);
        }


        public int getTriesSendingWillTopicReq() {
            return this.triesSendingWillTopicReq;
        }

        public void increaseTriesSendingWillTopicReq() {
            this.triesSendingWillTopicReq++;
        }

        public void resetTriesSendingWillTopicReq() {
            this.triesSendingWillTopicReq = 0;
        }


        public int getTriesSendingWillMsgReq() {
            return this.triesSendingWillMsgReq;
        }

        public void increaseTriesSendingWillMsgReq() {
            this.triesSendingWillMsgReq++;
        }

        public void resetTriesSendingWillMsgReq() {
            this.triesSendingWillMsgReq = 0;
        }


        public int getTriesSendingRegister(int topicId) {
            Integer value = mTriesSendingRegister.get(topicId);
            return value != null ? value : -1;
        }

        public synchronized void increaseTriesSendingRegister(int topicId) {
            Integer i = mTriesSendingRegister.get(topicId);
            if (i != null) {
                mTriesSendingRegister.put(topicId, i + 1);
            }
        }

        public void resetTriesSendingRegister(int topicId) {
            mTriesSendingRegister.remove(topicId);
        }
    }

    //////////////////////////////////////////////////////////////////////
}