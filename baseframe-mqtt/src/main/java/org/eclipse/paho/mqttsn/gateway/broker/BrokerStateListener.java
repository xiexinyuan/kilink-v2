package org.eclipse.paho.mqttsn.gateway.broker;

/**
 * Created by legendmohe on 2017/3/27.
 */

public interface BrokerStateListener {

    void onConnected(String ip, int port);

    void onDisconnected(String ip, int port);
}
