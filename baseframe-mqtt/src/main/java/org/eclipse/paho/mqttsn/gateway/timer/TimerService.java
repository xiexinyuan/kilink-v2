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

package org.eclipse.paho.mqttsn.gateway.timer;

import org.eclipse.paho.mqttsn.gateway.Gateway;
import org.eclipse.paho.mqttsn.gateway.core.Dispatcher;
import org.eclipse.paho.mqttsn.gateway.messages.Message;
import org.eclipse.paho.mqttsn.gateway.messages.control.ControlMessage;
import org.eclipse.paho.mqttsn.gateway.utils.Address;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TimerService {

    private Timer timer;
    private Dispatcher dispatcher;
    private Map<Address, TimeoutTimerTask> timeoutTasks;


    /**
     * Constructor.
     */
    public TimerService(Gateway gateway) {
        timer = new Timer();
        dispatcher = gateway.getDispatcher();
        timeoutTasks = new HashMap<>();
    }

    /**
     * This method schedules a TimeoutTimerTask to the timer for future executions and
     * stores it to a list.
     *
     * @param clientAddress The address of the client.
     * @param type          The type of task/timeout (WAITING_WILLTOPIC, WAITING_WILLMESSAGE, KEEP_ALIVE,etc.).
     * @param timeout       Expresses the delay and the period (in seconds) of executing the TimeoutTimerTask.
     * @see TimeoutTimerTask
     */
    public void register(Address clientAddress, int type, int timeout) {
        register(clientAddress, type, timeout, null);
    }

    public void register(Address address, int type, int timeout, Object data) {
        long delay = timeout * 1000;
        long period = timeout * 1000;

        TimeoutTimerTask timeoutTimerTask = new TimeoutTimerTask(address, type, data);

        //put this timeoutTimerTask in a list
        timeoutTasks.put(address, timeoutTimerTask);

        //schedule for future executions
        timer.scheduleAtFixedRate(timeoutTimerTask, delay, period);
    }

    /**
     * This method removes a TimeoutTimerTask from the list and cancels it.
     *
     * @param clientAddress The address of the client.
     * @param type          The type of task/timeout (WAITING_WILLTOPIC, WAITING_WILLMESSAGE, etc.).
     * @see TimeoutTimerTask
     */
    public void unregister(Address address, int type) {
        TimeoutTimerTask task = timeoutTasks.get(address);
        if (task != null) {
            if (task.getType() == type) {
                task.cancel();
                timeoutTasks.remove(address);
            }
        }
    }

    /**
     * This method removes a TimeoutTimerTask from the list and cancels it.
     *
     * @param clientAddress The address of the client.
     * @see TimeoutTimerTask
     */
    public void unregister(Address address) {
        TimeoutTimerTask task = timeoutTasks.get(address);
        if (task != null) {
            task.cancel();
            timeoutTasks.remove(address);
        }
    }

    public Timer getTimer() {
        return timer;
    }

    /**
     * This object represents a TimeoutTimerTask.It is uniquely identified
     * by the clientAddress and the type of task/timeout (WAITING_WILLTOPIC, etc.)
     */
    public class TimeoutTimerTask extends TimerTask {
        Address address;
        int type;
        Object data;

        /**
         * Constructor.
         *
         * @param clientAddress The address of the client.
         * @param type          The type of task/timeout (WAITING_WILLTOPIC, WAITING_WILLMESSAGE,etc.
         */
        public TimeoutTimerTask(Address addr, int type, Object data) {
            this.address = addr;
            this.type = type;
            this.data = data;
        }

        /* (non-Javadoc)
         * @see java.utils.TimerTask#run()
         */
        public void run() {
            //create new control message
            ControlMessage controlMsg = new ControlMessage();
            controlMsg.setMsgType(type);
            controlMsg.setData(data);

            //create an "internal" message
            Message msg = new Message(this.address);
            msg.setType(Message.CONTROL_MSG);
            msg.setControlMessage(controlMsg);

            //put this message to the Dispatcher's queue
            if (dispatcher != null) {
                dispatcher.putMessage(msg);
            } else {
//                Log.d("----", "dispatcher is null");
            }
        }

        public Address getAddress() {
            return address;
        }

        public int getType() {
            return type;
        }
    }
}