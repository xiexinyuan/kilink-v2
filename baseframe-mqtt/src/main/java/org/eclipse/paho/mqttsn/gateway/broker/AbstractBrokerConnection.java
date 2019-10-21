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

package org.eclipse.paho.mqttsn.gateway.broker;

import org.eclipse.paho.mqttsn.gateway.Gateway;
import org.eclipse.paho.mqttsn.gateway.core.Dispatcher;
import org.eclipse.paho.mqttsn.gateway.exceptions.MqttsException;
import org.eclipse.paho.mqttsn.gateway.messages.mqtt.MqttMessage;
import org.eclipse.paho.mqttsn.gateway.utils.Address;

/**
 * This class represents the interface over which the gateway send
 * Mqtt messages to the the broker and vice versa.
 */
public abstract class AbstractBrokerConnection {
    private Address mAddress;
    private String mBorkerIp;
    private int mBorkerPort;

    private Dispatcher mDispatcher;

    private BrokerStateListener mListener;

    protected AbstractBrokerConnection(Address address) {
        this.mAddress = address;
    }

    public void initialize(Gateway gateway, BrokerStateListener listener) {
        this.mDispatcher = gateway.getDispatcher();
        this.mBorkerIp = gateway.getParameters().getBrokerURL();
        this.mBorkerPort = gateway.getParameters().getBrokerTcpPort();
        this.mListener = listener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractBrokerConnection)) return false;

        AbstractBrokerConnection that = (AbstractBrokerConnection) o;

        return mAddress != null ? mAddress.equals(that.mAddress) : that.mAddress == null;

    }

    public Address getAddress() {
        return mAddress;
    }

    public String getBorkerIp() {
        return mBorkerIp;
    }

    public int getBorkerPort() {
        return mBorkerPort;
    }

    public Dispatcher getDispatcher() {
        return mDispatcher;
    }

    public BrokerStateListener getListener() {
        return mListener;
    }

    @Override
    public int hashCode() {
        return mAddress != null ? mAddress.hashCode() : 0;
    }

    public abstract void shutdown();

    /**
     * The method that sends a Mqtt message to the broker.
     *
     * @param msg The Mqtt message to be sent.
     */
    public abstract void sendMqttMessage(MqttMessage msg) throws MqttsException;

    /**
     * The method that connect the broker interface.
     */
    public abstract void connect() throws MqttsException;

    /**
     * The method that disconnects from the broker.
     */
    public abstract void disconnect();
}