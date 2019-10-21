package cn.xlink.sdk.common;


import cn.xlink.sdk.common.handler.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据容器。实现了观察者模式。
 * <p>
 * Created by legendmohe on 16/4/27.
 */
public abstract class DataManager<K, V> {

    private final int MSG_ITEM_ADD = 0;
    private final int MSG_ITEM_REMOVE = 1;
    private final int MSG_ITEM_UPDATE = 2;

    private XHandlerable mNotifyThreadHandler;

    private ConcurrentHashMap<K, V> mDataMap = new ConcurrentHashMap<>();

    private DataObservable<V> mDataObservable = new DataObservableImpl<>();

    public void init(XLooperable looper) {
        mNotifyThreadHandler = XLinkHandlerHelper.getInstance().getHandlerable(looper);
        mNotifyThreadHandler.setXHandleMsgAction(new XMsgHandleAction() {
            @Override
            public boolean handleMessage(@NotNull XHandlerable handlerable, @NotNull XMessageable msg) {
                switch (msg.getMsgId()) {
                    case MSG_ITEM_ADD:
                        mDataObservable.notifyDataAdded((V) msg.getObj());
                        break;
                    case MSG_ITEM_REMOVE:
                        mDataObservable.notifyDataRemove((V) msg.getObj());
                        break;
                    case MSG_ITEM_UPDATE:
                        mDataObservable.notifyDataUpdated((V) msg.getObj());
                        break;
                }
                return true;
            }
        });
        XLinkHandlerHelper.getInstance().prepareLooperable(mNotifyThreadHandler, mNotifyThreadHandler.getXLooper());
    }

    public boolean contains(V item) {
        if (item == null) {
            return false;
        }
        return mDataMap.containsKey(getIdFromItem(item));
    }

    public boolean containsKey(K itemId) {
        return mDataMap.containsKey(itemId);
    }

    public void addItem(V item) {
        if (item == null || !checkIfItemValid(item)) {
            return;
        }
        mDataMap.put(getIdFromItem(item), item);
        sendNotifyMsg(MSG_ITEM_ADD, item, 0);
    }

    public void addItem(K itemId, V item) {
        if (item == null || !checkIfItemValid(item)) {
            return;
        }
        mDataMap.put(itemId, item);
        sendNotifyMsg(MSG_ITEM_ADD, item, 0);
    }

    public void addItems(Collection<V> items) {
        if (CommonUtil.isEmpty(items))
            return;

        for (V item :
                items) {
            if (checkIfItemValid(item)) {
                mDataMap.put(getIdFromItem(item), item);
                sendNotifyMsg(MSG_ITEM_ADD, item, 0);
            }
        }
    }

    public V removeItem(V item) {
        if (item == null) {
            return null;
        }
        return removeItemByKey(getIdFromItem(item));
    }

    public V removeItemByKey(K itemId) {
        if (itemId == null) {
            return null;
        }
        V item = mDataMap.remove(itemId);
        if (item != null) {
            sendNotifyMsg(MSG_ITEM_REMOVE, item, 0);
        }
        return item;
    }

    public void removeItems(Collection<K> itemIds) {
        if (CommonUtil.isEmpty(itemIds))
            return;

        for (K itemId :
                itemIds) {
            V item = mDataMap.get(itemId);
            if (item != null) {
                mDataMap.remove(itemId);
                sendNotifyMsg(MSG_ITEM_REMOVE, item, 0);
            }
        }
    }

    public void clear() {
        if (mDataMap.size() == 0) {
            return;
        }
        Collection<V> items = mDataMap.values();
        if (items.size() != 0) {
            mDataMap.clear();
            for (V item :
                    items) {
                sendNotifyMsg(MSG_ITEM_REMOVE, item, 0);
            }
        }
    }

    public void updateItem(V item) {
        updateItem(getIdFromItem(item), item);
    }

    public void updateItem(K itemId, V item) {
        if (containsKey(itemId)) {
            mDataMap.put(itemId, item);
            mNotifyThreadHandler.removeXMessages(MSG_ITEM_UPDATE);
            sendNotifyMsg(MSG_ITEM_UPDATE, item, 300);
        }
    }

    private void sendNotifyMsg(int what, V obj, int delay) {
        if (mDataObservable != null && mDataObservable.getObserverCount() != 0 && mNotifyThreadHandler != null) {
            XMessageable msg = XLinkHandlerHelper.getInstance().getMessageable(what, obj);
            if (delay > 0) {
                mNotifyThreadHandler.sendXMessageDelayed(msg, delay);
            } else {
                mNotifyThreadHandler.sendXMessage(msg);
            }
        }

        onDataChanged(obj);
    }

    public Collection<V> getAllItems() {
        return mDataMap.values();
    }

    public Set<K> getAllItemIds() {
        return mDataMap.keySet();
    }

    public V getItem(K itemId) {
        if (itemId == null) {
            return null;
        } else {
            return mDataMap.get(itemId);
        }
    }

    public int size() {
        return mDataMap.size();
    }

    public void registerDataObserver(DataObserver<V> observer) {
        mDataObservable.register(observer);
    }

    public void unregisterDataObserver(DataObserver<V> observer) {
        mDataObservable.unregister(observer);
    }

    public void postRunnableInNotificationThread(Runnable runnable) {
        if (mNotifyThreadHandler != null)
            mNotifyThreadHandler.postXRunnable(runnable);
    }

    protected Map<K, V> getDatas() {
        return mDataMap;
    }

    /**
     * 判断ITEM是否为有效的ITEM,若不是,则不会进行任何处理或者缓存
     *
     * @param item
     * @return
     */
    protected boolean checkIfItemValid(V item) {
        return true;
    }

    /**
     * invoked when item has been added, removed or updated.
     *
     * @param item
     */
    protected void onDataChanged(V item) {

    }

    abstract public K getIdFromItem(V item);
}
