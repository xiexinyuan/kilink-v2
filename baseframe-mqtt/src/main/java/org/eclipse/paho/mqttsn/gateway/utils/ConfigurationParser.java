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

package org.eclipse.paho.mqttsn.gateway.utils;

import org.eclipse.paho.mqttsn.gateway.exceptions.MqttsException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class ConfigurationParser {

    public ConfigurationParser() {
    }

    public static GWParameters parseFile(InputStream confStream, InputStream topicStream) throws MqttsException {
        try {
            GWParameters parameters = GWParameters.newBuilder().build();

            Properties pr = new Properties();
            pr.load(confStream);

            //load from configuration file the data related to the gateway

            int logLevel;
            String level = pr.getProperty("logLevel");
            if (level == null)
                throw new MqttsException("There is no log level defined");
            if (level.equalsIgnoreCase("INFO"))
                logLevel = GatewayLogger.INFO;
            else if (level.equalsIgnoreCase("WARN"))
                logLevel = GatewayLogger.WARN;
            else if (level.equalsIgnoreCase("ERROR"))
                logLevel = GatewayLogger.ERROR;
            else
                logLevel = GatewayLogger.INFO;

            GatewayLogger.setLogLevel(logLevel);

            String spr = pr.getProperty("predfTopicIdSize");
            if (spr == null)
                throw new MqttsException("There is no predefined topic id size defined");
            int predfTopicIdSize = 0;
            try {
                predfTopicIdSize = Integer.parseInt(spr);
                if (predfTopicIdSize < 1)
                    throw new MqttsException("Predefined topic id size should be greater than 1");
            } catch (NumberFormatException e) {
                throw new MqttsException("Predefined topic id size - Format error " + e.getMessage());
            }
            parameters.setPredfTopicIdSize(predfTopicIdSize);

            //load the predefined topic ids
            Properties pr1 = new Properties();
            pr1.load(topicStream);

            Iterator<?> iter = pr1.keySet().iterator();
            Iterator<?> iterVal = pr1.values().iterator();
            Map<Integer, String> table = new HashMap<>();
            while (iter.hasNext()) {
                try {
                    Integer topicId = new Integer((String) iter.next());
                    String topicName = (String) iterVal.next();
                    if (topicId.intValue() > 0 && topicId.intValue() <= parameters.getPredfTopicIdSize() && !table.containsValue(topicName)) {
                        table.put(topicId, topicName);
                    }
                } catch (NumberFormatException e) {
                    //do nothing just omit this entry
                }
            }
            parameters.setPredefTopicIdTable(table);


            String sGwId = pr.getProperty("gwId");
            if (sGwId == null)
                throw new MqttsException("There is no gateway id defined");
            int gwId = 0;
            try {
                gwId = Integer.parseInt(sGwId);
            } catch (NumberFormatException e) {
                throw new MqttsException("Gateway Id - Format error " + e.getMessage());
            }
            parameters.setGwId(gwId);


            String sadvper = pr.getProperty("advPeriod");
            if (sadvper == null)
                throw new MqttsException("There is no advertising period defined");
            int advPeriod = 0;
            try {
                advPeriod = Integer.parseInt(sadvper);
                if (advPeriod <= 0)
                    throw new MqttsException("Advertising period should be greater than 0 seconds");
            } catch (NumberFormatException e) {
                throw new MqttsException("Advertising period - Format error " + e.getMessage());
            }
            parameters.setAdvPeriod(advPeriod);


            String skeepalive = pr.getProperty("keepAlivePeriod");
            if (skeepalive == null)
                throw new MqttsException("There is no keep alive period defined");
            int keepAlivePeriod = 0;
            try {
                keepAlivePeriod = Integer.parseInt(skeepalive);
                if (keepAlivePeriod <= 0)
                    throw new MqttsException("Keep alive period should be greater than 0 seconds");
            } catch (NumberFormatException e) {
                throw new MqttsException("Keep alive period - Format error " + e.getMessage());
            }
            parameters.setKeepAlivePeriod(keepAlivePeriod);


            String smret = pr.getProperty("maxRetries");
            if (smret == null)
                throw new MqttsException("There is no maximum retries defined");
            int maxRetries = 0;
            try {
                maxRetries = Integer.parseInt(smret);
                if (maxRetries < 0)
                    throw new MqttsException("Maximum retries cannot be negative");
            } catch (NumberFormatException e) {
                throw new MqttsException("Maximum retries - Format error " + e.getMessage());
            }
            parameters.setMaxRetries(maxRetries);


            String swait = pr.getProperty("waitingTime");
            if (swait == null)
                throw new MqttsException("There is no waiting time defined");
            int waitingTime = 0;
            try {
                waitingTime = Integer.parseInt(swait);
                if (waitingTime <= 0)
                    throw new MqttsException("Waiting time should be greater than 0 seconds");
            } catch (NumberFormatException e) {
                throw new MqttsException("Waiting time - Format error " + e.getMessage());
            }
            parameters.setWaitingTime(waitingTime);


            String smaxlength = pr.getProperty("maxMqttsLength");
            if (smaxlength == null)
                throw new MqttsException("There is no maximum Mqtts length defined");
            int maxMqttsLength = 0;
            try {
                maxMqttsLength = Integer.parseInt(smaxlength);
                if (maxMqttsLength < 2)
                    throw new MqttsException("Maximum Mqtts length should be greater than 1");
            } catch (NumberFormatException e) {
                throw new MqttsException("Maximum Mqtts length - Format error " + e.getMessage());
            }
            parameters.setMaxMqttsLength(maxMqttsLength);


            String sminlength = pr.getProperty("minMqttsLength");
            if (sminlength == null)
                throw new MqttsException("There is no maximum Mqtts length defined");
            int minMqttsLength = 0;
            try {
                minMqttsLength = Integer.parseInt(sminlength);
                if (minMqttsLength < 2)
                    throw new MqttsException("Minimum Mqtts length should be greater than 1");
            } catch (NumberFormatException e) {
                throw new MqttsException("Mimimum Mqtts length - Format error " + e.getMessage());
            }
            parameters.setMinMqttsLength(minMqttsLength);


            String sudp = pr.getProperty("udpPort");
            if (sudp == null)
                throw new MqttsException("There is no UDP port defined");
            int udpPort = 0;
            try {
                udpPort = Integer.parseInt(sudp);
                if (udpPort < 1024 || udpPort > 65535)
                    throw new MqttsException("UDP port number out of range");
            } catch (NumberFormatException e) {
                throw new MqttsException("UDP port number - Format error " + e.getMessage());
            }
            parameters.setUdpPort(udpPort);


            String brokerURL = pr.getProperty("brokerURL");
            if (brokerURL == null)
                throw new MqttsException("There is no broker URL defined");
            parameters.setBrokerURL(brokerURL);


            String stcp = pr.getProperty("brokerTcpPort");
            if (stcp == null)
                throw new MqttsException("There is no broker TCP port defined");
            int brokerTcpPort = 0;
            try {
                brokerTcpPort = Integer.parseInt(stcp);
                if (brokerTcpPort < 1024 || brokerTcpPort > 65535)
                    throw new MqttsException("TCP port number out of range");
            } catch (NumberFormatException e) {
                throw new MqttsException("TCP port number - Format error " + e.getMessage());
            }
            parameters.setBrokerTcpPort(brokerTcpPort);


            String shandtim = pr.getProperty("handlerTimeout");
            if (shandtim == null)
                throw new MqttsException("There is no handler timeout defined");
            long handlerTimeout = 0;
            try {
                handlerTimeout = Long.parseLong(shandtim);
                if (handlerTimeout <= 0)
                    throw new MqttsException("Handler timeout should be greater than 0");
            } catch (NumberFormatException e) {
                throw new MqttsException("Handler timeout - Format error " + e.getMessage());
            }
            parameters.setHandlerTimeout(handlerTimeout);


            String sfortim = pr.getProperty("forwarderTimeout");
            if (sfortim == null)
                throw new MqttsException("There is no forwarder timeout defined");
            long forwarderTimeout = 0;
            try {
                forwarderTimeout = Long.parseLong(sfortim);
                if (forwarderTimeout <= 0)
                    throw new MqttsException("Forwarder timeout should be greater than 0");
            } catch (NumberFormatException e) {
                throw new MqttsException("Forwarder timeout - Format error " + e.getMessage());
            }
            parameters.setForwarderTimeout(forwarderTimeout);


            String scheckper = pr.getProperty("checkingPeriod");
            if (scheckper == null)
                throw new MqttsException("There is no checking period defined");
            long checkingPeriod = 0;
            try {
                checkingPeriod = Long.parseLong(scheckper);
                if (checkingPeriod <= 0)
                    throw new MqttsException("Checking period should be greater than 0");
            } catch (NumberFormatException e) {
                throw new MqttsException("Checking period - Format error " + e.getMessage());
            }
            parameters.setCkeckingPeriod(checkingPeriod);


            String serialUrl = pr.getProperty("serialPortURL");
            if (serialUrl == null)
                throw new MqttsException("There is no serial port url defined");
            parameters.setSerialPortURL(serialUrl);


            String clientIntString = pr.getProperty("clientInterfaces");
            if (clientIntString == null)
                throw new MqttsException("There are no client interfaces defined");
            parameters.setClientIntString(clientIntString);


            String protocolName = pr.getProperty("protocolName");
            parameters.setProtocolName(protocolName);


            int protocolVersion = Integer.parseInt(pr.getProperty("protocolVersion"));
            parameters.setProtocolVersion(protocolVersion);


            boolean retain = false;
            String retainString = pr.getProperty("retain");
            if (retainString != null) {
                if (retainString.trim().equalsIgnoreCase("true")) {
                    retain = true;
                }
            }
            parameters.setRetain(retain);


            int willQoS = Integer.parseInt(pr.getProperty("willQoS"));
            parameters.setWillQoS(willQoS);


            boolean willFlag = false;
            String willFlagString = pr.getProperty("willFlag");
            if (willFlagString != null) {
                if (willFlagString.trim().equalsIgnoreCase("true")) {
                    willFlag = true;
                }
            }
            parameters.setWillFlag(willFlag);
            //System.out.println(">> willFlag= " + parameters.isWillFlag());


            boolean cleanSession = false;
            String cleanSessionString = pr.getProperty("cleanSession");
            if (cleanSessionString != null) {
                if (cleanSessionString.trim().equalsIgnoreCase("true")) {
                    cleanSession = true;
                }
            }
            parameters.setCleanSession(cleanSession);


            String willTopic = pr.getProperty("willTopic");
            parameters.setWillTopic(willTopic);

            String willMessage = pr.getProperty("willMessage");
            parameters.setWillMessage(willMessage);

            return parameters;
        } catch (FileNotFoundException e) {
            throw new MqttsException(e.getMessage());
        } catch (IOException e) {
            throw new MqttsException(e.getMessage());
        }
    }
}