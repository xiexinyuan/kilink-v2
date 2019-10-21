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

package org.eclipse.paho.mqttsn.gateway.utils;

import java.net.InetAddress;


/**
 * This class represents the address of a client.It includes also the IP address and the
 * port of the forwarder with which this client is connected.
 * <p>
 * Parts of this code were imported from com.ibm.zurich.mqttz_gw.utils.SAaddress.java
 */
public class ClientAddress extends Address {

    private boolean isEncaps;  //whether fw-encapsulation is used by this client or not
    private byte[] encaps;


    public ClientAddress(byte[] addr) {
        super(addr);
        this.isEncaps = true;
    }

    public ClientAddress(byte[] addr, InetAddress ipAddr, int port, boolean isencaps, byte[] encaps) {
        super(addr, ipAddr, port);
        this.isEncaps = isencaps;
        this.encaps = encaps;
    }

    public boolean isEncaps() {
        return isEncaps;
    }

    public byte[] getEncaps() {
        return encaps;
    }

    @Override
    public String toString() {
        return "ClientAddress:" + super.toString();
    }
}