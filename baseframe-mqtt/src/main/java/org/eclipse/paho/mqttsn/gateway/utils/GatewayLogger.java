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


import cn.xlink.sdk.common.XLog;

public class GatewayLogger {
    private static final String TAG = "GatewayLogger";

    public final static int INFO = 1;
    public final static int WARN = 2;
    public final static int ERROR = 3;

    private static int LOG_LEVEL = WARN;

    private static LogImpl sLogImpl = new LogImpl() {
        @Override
        public void info(String msg) {
            XLog.d(TAG, msg);
        }

        @Override
        public void warn(String msg) {
            XLog.w(TAG, msg);
        }

        @Override
        public void error(String msg) {
            XLog.e(TAG, msg);
        }
    };

    private static void info(String msg) {
        if (sLogImpl != null) {
            sLogImpl.info(msg);
        }
    }

    private static void warn(String msg) {
        if (sLogImpl != null) {
            sLogImpl.warn(msg);
        }
    }

    private static void error(String msg) {
        if (sLogImpl != null) {
            sLogImpl.error(msg);
        }
    }

    public static void setLogImpl(LogImpl logImpl) {
        GatewayLogger.sLogImpl = logImpl;
    }

    public static void log(int logLevel, String msg) {
        if (logLevel >= LOG_LEVEL) {
            switch (logLevel) {
                case INFO:
                    info(msg);
                    break;
                case WARN:
                    warn(msg);
                    break;
                case ERROR:
                    error(msg);
                    break;
                default:
            }
        }
    }

    public static void setLogLevel(int logLevel) {
        LOG_LEVEL = logLevel;
    }

    public interface LogImpl {
        void info(String msg);

        void warn(String msg);

        void error(String msg);
    }

}