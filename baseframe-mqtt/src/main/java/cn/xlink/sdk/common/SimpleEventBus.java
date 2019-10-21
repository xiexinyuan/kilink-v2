package cn.xlink.sdk.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by legendmohe on 2017/5/25.
 */

public class SimpleEventBus {

    private ExecutorService mExecutorService;

    private Map<String, List<EventListener>> mEventListeners = new ConcurrentHashMap<>();

    private static class LazyHolder {
        private static final SimpleEventBus INSTANCE = new SimpleEventBus();
    }

    public static SimpleEventBus getInstance() {
        return LazyHolder.INSTANCE;
    }

    private SimpleEventBus() {
        mExecutorService = Executors.newCachedThreadPool();
    }

    public void post(String event) {
        post(event, null);
    }

    public synchronized void subscribeEvent(String event, EventListener listener) {
        List<EventListener> eventListeners = mEventListeners.get(event);
        if (eventListeners == null) {
            eventListeners = new ArrayList<>();
            mEventListeners.put(event, eventListeners);
        }
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }

    public synchronized void unsubscribeEvent(String event, EventListener listener) {
        List<EventListener> eventListeners = mEventListeners.get(event);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    public <T> void post(final String event, final T data) {
        if (CommonUtil.isEmpty(mEventListeners.get(event))) {
            return;
        }
        List<EventListener> eventListeners = new ArrayList<>(mEventListeners.get(event));
        for (final EventListener listener : eventListeners) {
            if (mExecutorService != null) {
                mExecutorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onEvent(event, data);
                        }
                    }
                });
            }
        }
    }

    public interface EventListener {
        void onEvent(String event, Object data);
    }
}
