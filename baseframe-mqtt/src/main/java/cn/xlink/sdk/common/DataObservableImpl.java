package cn.xlink.sdk.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 观察者模式的一个实现
 * <p>
 * Created by legendmohe on 16/4/27.
 */
public class DataObservableImpl<T> implements DataObservable<T> {

    private List<DataObserver<T>> mObservers = new ArrayList<>();

    @Override
    public void register(DataObserver<T> observer) {
        if (observer != null) {
            mObservers.add(observer);
        }
    }

    @Override
    public void unregister(DataObserver<T> observer) {
        mObservers.remove(observer);
    }

    @Override
    public synchronized void notifyDataUpdated(T item) {
        if (mObservers == null || mObservers.size() == 0)
            return;

        for (DataObserver<T> observer :
                mObservers) {
            observer.onDataUpdated(item);
        }
    }

    @Override
    public void notifyDataAdded(T item) {
        if (mObservers == null || mObservers.size() == 0)
            return;

        for (DataObserver<T> observer :
                mObservers) {
            observer.onDataAdded(item);
        }
    }

    @Override
    public void notifyDataRemove(T item) {
        if (mObservers == null || mObservers.size() == 0)
            return;

        for (DataObserver<T> observer :
                mObservers) {
            observer.onDataRemoved(item);
        }
    }

    @Override
    public int getObserverCount() {
        return mObservers == null ? 0 : mObservers.size();
    }
}
