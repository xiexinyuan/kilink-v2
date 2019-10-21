package org.eclipse.paho.mqttsn.gateway.core;

/**
 * Created by legendmohe on 2017/3/28.
 */

public interface ClientMsgHandlerStateListener {
    void onClientConnected(ClientMsgHandler msgHandler);

    void onClientDisconnected(ClientMsgHandler msgHandler);
}
