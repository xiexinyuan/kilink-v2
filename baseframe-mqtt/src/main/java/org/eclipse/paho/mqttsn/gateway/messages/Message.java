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

package org.eclipse.paho.mqttsn.gateway.messages;

import org.eclipse.paho.mqttsn.gateway.client.ClientConnection;
import org.eclipse.paho.mqttsn.gateway.messages.control.ControlMessage;
import org.eclipse.paho.mqttsn.gateway.messages.mqtt.MqttMessage;
import org.eclipse.paho.mqttsn.gateway.messages.mqtts.MqttsMessage;
import org.eclipse.paho.mqttsn.gateway.utils.Address;


/**
 * This object represents an "internal" message that "wraps" a Mqtts, a Mqtt
 * or a Control message.It is generated by the BroketInterface, any ClientInterface, the TimerService
 * and the MsgHandler (GatewayMsgHandler or ClientMsgHandler) and then is added in Dispatcher's queue.
 * In the case of Mqtts message carries also the client interface in which
 * MsgHandler will respond in the future.In all cases carries the Address field in order to
 * uniquely identify the ClientMsgHandler or GatewayMsgHandler.If this filed does not exist,
 * the encapsulated message (Mqtts, Mqtt or Control) is addressed to all MsgHandlers.
 *
 * @see org.eclipse.paho.mqttsn.gateway.core.Dispatcher
 * @see org.eclipse.paho.mqttsn.gateway.core.MsgHandler
 * @see org.eclipse.paho.mqttsn.gateway.core.ClientMsgHandler
 * @see org.eclipse.paho.mqttsn.gateway.core.GatewayMsgHandler
 */
public class Message {

    public static final int MQTTS_MSG = 1;
    public static final int MQTT_MSG = 2;
    public static final int CONTROL_MSG = 3;
    public static final int SHUT_DOWN_MSG = 4;


    private final Address address;
    private int type;

    private MqttsMessage mqttsMessage = null;
    private MqttMessage mqttMessage = null;
    private ControlMessage controlMessage = null;

    private ClientConnection mClientConnection = null;


    public Message(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public MqttsMessage getMqttsMessage() {
        return mqttsMessage;
    }

    public void setMqttsMessage(MqttsMessage mqttsMessage) {
        this.mqttsMessage = mqttsMessage;
    }

    public MqttMessage getMqttMessage() {
        return mqttMessage;
    }

    public void setMqttMessage(MqttMessage mqttMessage) {
        this.mqttMessage = mqttMessage;
    }

    public ControlMessage getControlMessage() {
        return controlMessage;
    }

    public void setControlMessage(ControlMessage controlMessage) {
        this.controlMessage = controlMessage;
    }

    public ClientConnection getClientConnection() {
        return mClientConnection;
    }

    public void setClientConnection(ClientConnection clientConnection) {
        this.mClientConnection = clientConnection;
    }
}