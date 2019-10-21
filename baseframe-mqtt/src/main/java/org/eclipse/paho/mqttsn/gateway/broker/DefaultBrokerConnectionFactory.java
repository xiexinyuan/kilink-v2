package org.eclipse.paho.mqttsn.gateway.broker;

import org.eclipse.paho.mqttsn.gateway.Gateway;
import org.eclipse.paho.mqttsn.gateway.broker.tcp.TCPBrokerConnection;
import org.eclipse.paho.mqttsn.gateway.utils.Address;

/**
 * Created by legendmohe on 2017/4/25.
 */

public class DefaultBrokerConnectionFactory extends AbstractBrokerConnectionFactory {
    @Override
    public AbstractBrokerConnection createBroker(Gateway gateway, Address address) {
        if (gateway == null) {
            return null;
        }
        return new TCPBrokerConnection(address);
    }
}
