package org.eclipse.paho.mqttsn.gateway.client;

/**
 * Created by legendmohe on 2017/3/28.
 */

public interface ClientStateListener {

    void onConnected(ClientConnection clientConnection);

    void onDisconnected(ClientConnection clientConnection);
}
