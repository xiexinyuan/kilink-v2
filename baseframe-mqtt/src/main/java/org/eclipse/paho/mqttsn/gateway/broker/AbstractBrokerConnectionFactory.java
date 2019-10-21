package org.eclipse.paho.mqttsn.gateway.broker;

import org.eclipse.paho.mqttsn.gateway.Gateway;
import org.eclipse.paho.mqttsn.gateway.utils.Address;

/**
 * Created by legendmohe on 2017/4/25.
 */

public abstract class AbstractBrokerConnectionFactory {

    public abstract AbstractBrokerConnection createBroker(Gateway gateway, Address address);
}
