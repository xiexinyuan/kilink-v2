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

package org.eclipse.paho.mqttsn.gateway.core;

import org.eclipse.paho.mqttsn.gateway.Gateway;
import org.eclipse.paho.mqttsn.gateway.messages.Message;
import org.eclipse.paho.mqttsn.gateway.messages.control.ControlMessage;
import org.eclipse.paho.mqttsn.gateway.messages.mqtt.MqttMessage;
import org.eclipse.paho.mqttsn.gateway.messages.mqtts.MqttsMessage;
import org.eclipse.paho.mqttsn.gateway.utils.Address;
import org.eclipse.paho.mqttsn.gateway.utils.ClientAddress;
import org.eclipse.paho.mqttsn.gateway.utils.GatewayLogger;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

//import org.eclipse.paho.mqttsn.gateway.Gateway;
//import org.eclipse.paho.mqttsn.gateway.utils.GWParameters;

/**
 * This object dispatches messages to the appropriate MsgHandler according to the
 * client address they carry.
 */
public class Dispatcher implements Runnable {

    private BlockingDeque<Message> mDataQueue;
    private Map<Address, MsgHandler> mHandlerTable;
    private volatile boolean mRunning;
    private Thread mReadingThread;

    private Gateway mGateway;

    /**
     * Initialization method.
     */
    public void initialize(Gateway gateway) {
        mDataQueue = new LinkedBlockingDeque<>();
        mHandlerTable = new ConcurrentHashMap<>();
        this.mRunning = true;
        this.mReadingThread = new Thread(this, "Dispatcher");
        this.mReadingThread.start();
        mGateway = gateway;
    }

    /**
     * The method that reads a message {@link org.eclipse.paho.mqttsn.gateway.messages.Message} from
     * the queue and dispatches it according to its type (Mqtts, Mqtt or Control message).
     */
    private void dispatch() {
        //read the next available Message from queue

        Message msg = null;
        try {
            msg = mDataQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        //get the type of the message that "internal" message carries
        int type = msg.getType();
        switch (type) {
            case Message.MQTTS_MSG:
                GatewayLogger.log(GatewayLogger.INFO, "Dispatcher - dispatch MQTTS_MSG" + " - " + msg.getMqttsMessage().getMsgType());
                dispatchMqtts(msg);
                break;
            case Message.MQTT_MSG:
                GatewayLogger.log(GatewayLogger.INFO, "Dispatcher - dispatch MQTT_MSG" + " - " + msg.getMqttMessage().getMsgType());
                dispatchMqtt(msg);
                break;
            case Message.CONTROL_MSG:
                GatewayLogger.log(GatewayLogger.INFO, "Dispatcher - dispatch CONTROL_MSG" + " - " + msg.getControlMessage().getMsgType());
                dispatchControl(msg);
                break;
            case Message.SHUT_DOWN_MSG:
                GatewayLogger.log(GatewayLogger.INFO, "Dispatcher - dispatch SHUT_DOWN_MSG");
                break;
            default:
                GatewayLogger.log(GatewayLogger.WARN, "Dispatcher - Message of unknown type \"" + msg.getType() + "\" received.");
                break;
        }
    }


    /**
     * The method that handles a Mqtts message.According to its address is dispatched
     * to the appropriate MsgHandler.
     *
     * @param msg
     */
    private void dispatchMqtts(Message msg) {
        //		GatewayLogger.log(GatewayLogger.INFO, "Dispatcher - New Mqtts message arrived at queue.");

        //get the address of the client from this message
        Address address = msg.getAddress();

        //get the Mqtts message itself
        MqttsMessage mqttsMsg = msg.getMqttsMessage();

        if (mqttsMsg == null) {
            GatewayLogger.log(GatewayLogger.WARN,
                    "Dispatcher - The received Mqtts message is null. The message cannot be processed.");
            return;
        }

        GatewayLogger.log(GatewayLogger.INFO, "dispatching mqtts msg [" + MqttsMessage.readableMsgType(mqttsMsg.getMsgType()) + "] to client [" + address + "]");

        //get the appropriate handler (GatewayMsgHandler or ClientMsgHandler)for this message
        //according to the unique address of the client (or the gateway)
        MsgHandler handler = getHandler(address);

        if (handler == null) {
            //there is no such a handler, create a new one (applies only for the case of ClientMsgHandler
            //because GatewayMsgHandler was inserted in the hashtable at the startup of the gateway)
            handler = initializeClientMsgHandler((ClientAddress) address);
        }

        //update the client interface of the MsgHandler (if the handler is a ClientMsgHandler)
        if (handler instanceof ClientMsgHandler && msg.getClientConnection() != null) {
            ClientMsgHandler clientHandler = (ClientMsgHandler) handler;
            clientHandler.setClientConnection(msg.getClientConnection());
        }

        //pass the message to the handler
        handler.handleMqttsMessage(mqttsMsg);
    }


    /**
     * The method that handles a Mqtt message.According to its address is dispatched
     * to the appropriate MsgHandler.
     *
     * @param msg
     */
    private void dispatchMqtt(Message msg) {
        //		GatewayLogger.log(GatewayLogger.INFO, "Dispatcher - New Mqtt message arrived at queue.");

        //get the address of the client from this message
        Address address = msg.getAddress();

        //get the Mqtt message itself
        MqttMessage mqttMsg = msg.getMqttMessage();

        if (mqttMsg == null) {
            GatewayLogger.log(GatewayLogger.WARN,
                    "Dispatcher - The received Mqtt message is null. The message cannot be processed.");
            return;
        }

        GatewayLogger.log(GatewayLogger.INFO, "dispatching mqtt msg [" + MqttMessage.readableMsgType(mqttMsg.getMsgType()) + "] to client [" + address + "]");

        //get the appropriate handler (GatewayMsgHandler or ClientMsgHandler)for this message
        //according to the unique address of the client (or the gateway)
        MsgHandler handler = getHandler(address);

        if (handler == null) {
            //there is no such a handler, create a new one (applies only for the case of ClientMsgHandler
            //because GatewayMsgHandler was inserted in the hashtable when Dispatcher was created)
            handler = initializeClientMsgHandler((ClientAddress) address);
        }

        //pass the message to the handler
        handler.handleMqttMessage(mqttMsg);
    }

    private MsgHandler initializeClientMsgHandler(ClientAddress clientAddress) {
        ClientMsgHandler clientMsgHandler = new ClientMsgHandler(clientAddress);
        putHandler(clientAddress, clientMsgHandler);
        clientMsgHandler.initialize(mGateway);
        clientMsgHandler.setStateListener(new ClientMsgHandlerStateListener() {
            @Override
            public void onClientConnected(ClientMsgHandler msgHandler) {
                if (mGateway.getConnectionStateListener() != null) {
                    mGateway.getConnectionStateListener().onClientConnected(msgHandler.getClientAddress());
                }
            }

            @Override
            public void onClientDisconnected(ClientMsgHandler msgHandler) {
                if (mGateway.getConnectionStateListener() != null) {
                    mGateway.getConnectionStateListener().onClientDisconnected(msgHandler.getClientAddress());
                }
            }
        });
        return clientMsgHandler;
    }


    /**
     * The method that handles a Control message.According to its address is dispatched
     * to the appropriate MsgHandler.
     *
     * @param msg
     */
    private void dispatchControl(Message msg) {
        //		GatewayLogger.log(GatewayLogger.INFO, "Dispatcher - New Control message arrived at queue.");

        //get the address of the client from this message
        Address address = msg.getAddress();


        //get the Control message itself
        ControlMessage controlMsg = msg.getControlMessage();

        if (controlMsg == null) {
            GatewayLogger.log(GatewayLogger.WARN,
                    "Dispatcher - The received Control message is null. The message cannot be processed.");
            return;
        }

        GatewayLogger.log(GatewayLogger.INFO, "dispatching control msg [" + ControlMessage.readableMsgType(controlMsg.getMsgType()) + "] to client [" + address + "]");

        if (address == null) {
            //this message applies to all message handlers
            //			GatewayLogger.log(GatewayLogger.INFO, "Dispatcher - The received Control message is addressed to all handlers.");
            deliverMessageToAll(controlMsg);
            return;
        }

        //get the appropriate handler (GatewayMsgHandler or ClientMsgHandler)for this message
        //according to the unique address of the client (or the gateway)
        MsgHandler handler = getHandler(address);

        if (handler == null) {
            //there is no such a handler, create a new one (applies only for the case of ClientMsgHandler
            //because GatewayMsgHandler was inserted in the hashtable when Dispatcher was created)
            handler = initializeClientMsgHandler((ClientAddress) address);
        }

        //pass the message to the handler
        handler.handleControlMessage(controlMsg);
    }


    /**
     * This method delivers a message to all MsgHandlers.
     */
    private void deliverMessageToAll(ControlMessage msg) {
        if (msg.getMsgType() == ControlMessage.SHUT_DOWN) {
            GatewayLogger.log(GatewayLogger.INFO, "-------- Mqtts Gateway shutting down --------");
        }
        for (MsgHandler handler :
                mHandlerTable.values()) {
            handler.handleControlMessage(msg);
        }

        if (msg.getMsgType() == ControlMessage.SHUT_DOWN) {
            GatewayLogger.log(GatewayLogger.INFO, "-------- Mqtts Gateway stopped --------");
        }
    }


    /**
     * The method that puts a new created MsgHandler to the mapping table.
     *
     * @param addr    The address of the handler
     * @param handler The new created handler object
     */
    public void putHandler(Address addr, MsgHandler handler) {
        this.mHandlerTable.put(addr, handler);
    }


    /**
     * The method that gets a MsgHandler from the mapping table according to its address.
     *
     * @param addr The address of the handler
     * @return The handler object
     */
    public MsgHandler getHandler(Address addr) {
        if (addr == null) {
            return null;
        }
        return mHandlerTable.get(addr);
//        MsgHandler ret = null;
//        for (Address address:
//             mHandlerTable.keySet()) {
//            if (address.equal(addr) && addr.equal(address)) {
//                address.setIPaddress(addr);
//                ret = mHandlerTable.get(address);
//                break;
//            }
//        }
//        return ret;
    }


    /**
     * The method that removes an MsgHandler from the mapping table.
     *
     * @param address The address of the handler
     */
    public void removeHandler(Address address) {
        mHandlerTable.remove(address);
//        Iterator<Address> iter = mHandlerTable.keySet().iterator();
//        while (iter.hasNext()) {
//            Address currentAddress = (Address) (iter.next());
//            if (currentAddress.equal(address)) {
//                iter.remove();
//                break;
//            }
//        }
    }


    /**
     * The method that puts a message {@link org.eclipse.paho.mqttsn.gateway.messages.Message}
     * to the queue.
     *
     * @param msg
     */
    public void putMessage(Message msg) {
        if (msg.getType() == Message.CONTROL_MSG)
            mDataQueue.addFirst(msg);
        else
            mDataQueue.addLast(msg);
    }

    public void shutdown() {
        this.mRunning = false;
        Message msg = new Message(null);
        msg.setType(Message.SHUT_DOWN_MSG);
        putMessage(msg);
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        while (mRunning) {
            dispatch();
        }

        GatewayLogger.log(GatewayLogger.INFO, "-------- Mqtts Dispatcher stopped --------");
    }
}
