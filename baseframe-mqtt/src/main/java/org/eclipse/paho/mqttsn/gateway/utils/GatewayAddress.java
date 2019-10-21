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
 * This class represents the address of the gateway.It includes also the IP address of the
 * gateway.
 * <p>
 * Parts of this code were imported from com.ibm.zurich.mqttz_gw.utils.SAaddress.java
 */
public class GatewayAddress extends Address {
    public GatewayAddress(byte[] addr) {
        super(addr);
    }

    public GatewayAddress(byte[] addr, InetAddress ipAddr, int port) {
        super(addr, ipAddr, port);
    }

    @Override
    public String toString() {
        return "GatewayAddress:" + super.toString();
    }
}