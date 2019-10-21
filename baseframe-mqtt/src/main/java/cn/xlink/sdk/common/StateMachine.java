/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.xlink.sdk.common;


import cn.xlink.sdk.common.handler.XHandlerable;
import cn.xlink.sdk.common.handler.XLinkHandlerHelper;
import cn.xlink.sdk.common.handler.XLooperable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * 状态机
 * <p>
 * Created by legendmohe on 15/12/5.
 */
public class StateMachine {

    Set<State> mStates = new HashSet<>();
    private State mInitState;
    private State mCurrentState;
    private final Object mHandleLock = new Object();

    XHandlerable mHandler;

    private Logger mLogger;

    public StateMachine(XLooperable looper) {
        mHandler = XLinkHandlerHelper.getInstance().getHandlerable(looper);
        XLinkHandlerHelper.getInstance().prepareLooperable(mHandler,mHandler.getXLooper());
    }

    public StateMachine(XHandlerable handler) {
        mHandler = handler;
    }

    public void setInitState(State initState) {
        mInitState = initState;
    }

    public void addState(State state) {
        synchronized (this) {
            mStates.add(state);
            state.setStateMachine(this);
        }
    }

    public void addStates(State... states) {
        synchronized (this) {
            for (State state :
                    states) {
                mStates.add(state);
                state.setStateMachine(this);
            }
        }
    }

    public void startSync(State initState) {
        synchronized (this) {
            mInitState = initState;
            mCurrentState = mInitState;
            mInitState.onStart();
        }
    }

    public void startSync() {
        if (mInitState == null)
            return;

        synchronized (this) {
            mCurrentState = mInitState;
            mInitState.onStart();
        }
    }

    public void start(State initState) {
        mInitState = initState;
        mCurrentState = mInitState;
        mHandler.postXRunnable(new Runnable() {
            @Override
            public void run() {
                synchronized (mHandleLock) {
                    mInitState.onStart();
                }
            }
        });
    }

    public void start() {
        if (mInitState == null)
            return;

        mCurrentState = mInitState;
        mHandler.postXRunnable(new Runnable() {
            @Override
            public void run() {
                synchronized (mHandleLock) {
                    mInitState.onStart();
                }
            }
        });
    }

    public void stop(int cause) {
        if (mHandler != null) {
            // remove all with null token
            mHandler.removeCallbacksAndXMessages(null);
        }
        synchronized (this) {
            for (State state : mStates) {
                state.onStop(cause);
            }
        }
    }

    public void reset(int cause) {
        synchronized (this) {
            for (State state : mStates) {
                state.onReset(cause);
            }
            mCurrentState = mInitState;
        }
    }

    public void postEvent(Enum<?> event) {
        postEvent(event, null);
    }

    public void postEventDelay(Enum<?> event, int delay) {
        postEventDelay(event, null, delay);
    }

    public void postEvent(final Enum<?> event, final Object data) {
        postEventDelay(event, data, 0);
    }

    public void postEventDelay(final Enum<?> event, final Object data, int delay) {
        if (mHandler == null) {
            return;
        }
        mHandler.postXRunnableDelayed(new Runnable() {
                                 @Override
                                 public void run() {
                                     postEventSync(event, data);
                                 }
                             },
                delay);
    }

    public void cleanDelayPosted() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndXMessages(null);
        }
    }

    public void postEventSync(Enum<?> event) {
        postEventSync(event, null);
    }

    public void postEventSync(final Enum<?> event, final Object data) {
        synchronized (mHandleLock) {
            State prevState = mCurrentState;
            State nextState = mCurrentState.mToStates.get(event);
            if (nextState == null) {
                prevState.onUnhandleEvent(event, data);
                return;
            }
            if (mLogger != null) {
                mLogger.log("from " + mCurrentState + " to " + nextState + " for event " + event);
            }
            mCurrentState = nextState;
            prevState.onLeave(nextState, event, data);
            nextState.onEnter(prevState, event, data);
        }
    }

    public boolean canMoveTo(State toState) {
        if (toState == null) {
            return false;
        }
        synchronized (this) {
            HashMap<Enum<?>, State> states = mCurrentState.mToStates;
            for (Enum<?> event : states.keySet()) {
                if (states.get(event).equals(toState)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean canAccept(Enum<?> event) {
        synchronized (this) {
            return mCurrentState.mToStates.containsKey(event);
        }
    }

    public State getCurrentState() {
        return mCurrentState;
    }

    public boolean inState(Class<? extends State> stateClass) {
        return mCurrentState.getClass().equals(stateClass);
    }

    public void setLogger(Logger logger) {
        mLogger = logger;
    }

    ///////////////////////////////////State///////////////////////////////////

    public interface Logger {
        void log(String msg);
    }

    public static abstract class State {

        HashMap<Enum<?>, State> mToStates = new HashMap<>();
        private StateMachine mStateMachine;

        @SuppressWarnings("unused")
        private String mName = "UNKNOWN";

        public State(String name) {
            mName = name;
        }

        public void linkTo(State toState, Enum<?> event) {
            if (toState == null) {
                throw new IllegalArgumentException("toState cannot be null");
            }
            mToStates.put(event, toState);
        }

        public boolean isEqualState(Class<? extends State> targetState) {
            return targetState != null && targetState.equals(this.getClass());
        }

        public void onStart() {
        }

        public void onStop(int cause) {
        }

        public void onReset(int cause) {
        }

        public void onUnhandleEvent(Enum<?> event, Object data) {
        }

        public void onEnter(State fromState, Enum<?> event, Object data) {
        }

        public void onLeave(State toState, Enum<?> event, Object data) {
        }

        protected StateMachine getStateMachine() {
            return mStateMachine;
        }

        protected void setStateMachine(StateMachine stateMachine) {
            mStateMachine = stateMachine;
        }

        @Override
        public String toString() {
            return "State{" +
                    "mName='" + mName + '\'' +
                    '}';
        }
    }
}