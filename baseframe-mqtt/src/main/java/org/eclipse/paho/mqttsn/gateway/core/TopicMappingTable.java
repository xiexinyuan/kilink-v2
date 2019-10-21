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

package org.eclipse.paho.mqttsn.gateway.core;

import org.eclipse.paho.mqttsn.gateway.utils.GWParameters;
import org.eclipse.paho.mqttsn.gateway.utils.GatewayLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TopicMappingTable {

    private Map<Integer, String> topicIdTable;

    private GWParameters mParameters;

    public TopicMappingTable() {
        topicIdTable = new ConcurrentHashMap<>(32);
    }


    public void initialize(GWParameters parameters) {
        mParameters = parameters;
        topicIdTable.putAll(mParameters.getPredefTopicIdTable());
    }

    /**
     * @param topicId
     * @param topicName
     */
    public void assignTopicId(int topicId, String topicName) {
        topicIdTable.put(topicId, topicName);
    }

    public String getTopicName(int topicId) {
        return topicIdTable.get(topicId);
    }

    /**
     * @param topicName
     * @return
     */
    public int getTopicId(String topicName) {
        for (Integer id :
                topicIdTable.keySet()) {
            if (topicIdTable.get(id).equals(topicName)) {
                return id;
            }
        }
        return 0;
    }

    /**
     * 重置订阅的topic
     */
    public void reset() {
        Collection<String> topics = topicIdTable.values();
        for (String topic : topics) {
            GatewayLogger.log(GatewayLogger.INFO, String.valueOf(topic));
        }
        //清除消息
        topicIdTable.clear();
        //重新初始化存储预订阅topic
        initialize(mParameters);
    }

    /**
     * @param topicId
     */
    public void removeTopicId(int topicId) {
        topicIdTable.remove(topicId);
    }


    /**
     * @param topicName
     */
    public void removeTopicId(String topicName) {
        Map<Integer, String> temp = new HashMap<>(topicIdTable.size());
        for (Integer topicId :
                temp.keySet()) {
            if (temp.get(topicId).equals(topicName) && topicId > mParameters.getPredfTopicIdSize()) {
                GatewayLogger.log(GatewayLogger.INFO, "topic table remove topic = [" + temp.get(topicId) + "]tid = " + topicId);
                topicIdTable.remove(topicId);
                break;
            }
        }
    }


    /**
     * Utility method. Prints the content of this mapping table
     */
    public void printContent() {
        Iterator<Integer> iter = topicIdTable.keySet().iterator();
        Iterator<String> iterVal = topicIdTable.values().iterator();
        while (iter.hasNext()) {
            Integer topicId = (Integer) iter.next();
            String tname = (String) (iterVal.next());
            System.out.println(topicId + " = " + tname);
        }
    }
}