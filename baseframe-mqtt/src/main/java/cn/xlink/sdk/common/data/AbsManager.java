package cn.xlink.sdk.common.data;

import cn.xlink.sdk.common.CommonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 统一的管理操作.读都是线程不安全的,写都是线程安全的.
 * Created by taro on 2018/1/2.
 */
public abstract class AbsManager<K, V> {
    final Collection<OnDataChangedListener<K, V>> mDataChangedListener;
    final Map<K, V> mDataMap;

    protected AbsManager() {
        mDataMap = new HashMap<>();
        mDataChangedListener = new CopyOnWriteArrayList<>();
    }

    /**
     * 判断ITEM是否为有效的ITEM,若不是,则不会进行任何处理或者缓存
     *
     * @param value
     * @return
     */
    protected boolean checkIfValueValid(@Nullable V value) {
        return true;
    }

    /**
     * 注册数据变化监听回调
     *
     * @param listener
     */
    public void registerDataChangedListener(OnDataChangedListener<K, V> listener) {
        if (listener != null && !mDataChangedListener.contains(listener)) {
            mDataChangedListener.add(listener);
        }
    }

    /**
     * 取消数据变化监听回调
     *
     * @param listener
     */
    public void unregisterDataChangedListener(OnDataChangedListener<K, V> listener) {
        if (listener != null && mDataChangedListener.contains(listener)) {
            mDataChangedListener.remove(listener);
        }
    }

    /**
     * 取消所有数据变化监听回调
     */
    public void unregisterAllDataChangedListener() {
        mDataChangedListener.clear();
    }

    /**
     * 批量添加数据
     *
     * @param values   数据集合
     * @param provider 通过value获取到数据的key的接口
     * @return
     */
    @NotNull
    public Collection<Map.Entry<K, V>> putAll(@NotNull Collection<V> values, @NotNull KeyProvider<K, V> provider) {
        Collection<Map.Entry<K, V>> entries = new ArrayList<>(values.size());
        synchronized (mDataMap) {
            K key = null;
            for (V value : values) {
                key = provider.getKeyFromValue(value);
                //若当前key不为null且当前value是有效值value
                if (key != null && checkIfValueValid(value)) {
                    mDataMap.put(key, value);
                    //保存当前的key与对象
                    Map.Entry<K, V> entry = new AbstractMap.SimpleEntry<K, V>(key, value);
                    entries.add(entry);
                }
            }
        }
        if (mDataChangedListener.size() > 0 && entries.size() > 0) {
            for (OnDataChangedListener<K, V> listener : mDataChangedListener) {
                listener.onDataBatchAdd(entries);
            }
        }
        return entries;
    }

    @Nullable
    public V put(K key, V value) {
        if (key != null && checkIfValueValid(value)) {
            V oldValue = null;
            synchronized (mDataMap) {
                oldValue = mDataMap.put(key, value);
            }
            if (mDataChangedListener.size() > 0) {
                if (oldValue != null) {
                    //当旧的数据存在时,则是数据的更新
                    for (OnDataChangedListener<K, V> listener : mDataChangedListener) {
                        listener.onDataChanged(key, value);
                    }
                } else {
                    //否则为新数据添加
                    for (OnDataChangedListener<K, V> listener : mDataChangedListener) {
                        listener.onDataAdd(key, value);
                    }
                }
            }
            return oldValue;
        } else {
            return null;
        }
    }

    @Nullable
    public V get(@Nullable K key) {
        return key == null ? null : mDataMap.get(key);
    }

    /**
     * 通过key移除第一个匹配的数据
     *
     * @param key
     * @return
     */
    public V removeByKey(@Nullable K key) {
        if (key != null) {
            V value = null;
            synchronized (mDataMap) {
                value = mDataMap.remove(key);
            }
            if (mDataChangedListener.size() > 0) {
                for (OnDataChangedListener<K, V> listener : mDataChangedListener) {
                    listener.onDataRemove(key, value);
                }
            }
            return value;
        } else {
            return null;
        }
    }

    /**
     * 通过value移除第一个匹配的数据
     *
     * @param value
     * @return
     */
    @Nullable
    public K removeFirstByValue(V value) {
        K key = null;
        synchronized (mDataMap) {
            Iterator<Map.Entry<K, V>> it = mDataMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                if (CommonUtil.isObjEquals(entry.getValue(), value)) {
                    key = entry.getKey();
                    value = entry.getValue();
                    it.remove();
                    break;
                }
            }
        }
        if (mDataChangedListener.size() > 0 && key != null) {
            for (OnDataChangedListener<K, V> listener : mDataChangedListener) {
                listener.onDataRemove(key, value);
            }
        }
        return key;
    }

    /**
     * 通过key移除所有匹配的数据
     *
     * @param keys
     * @return
     */
    @NotNull
    public Collection<Map.Entry<K, V>> removeAllByKeys(@NotNull Collection<K> keys) {
        Collection<Map.Entry<K, V>> entryCol = new ArrayList<>();
        synchronized (mDataMap) {
            for (K key : keys) {
                if (key != null) {
                    V value = mDataMap.remove(key);
                    if (value != null) {
                        Map.Entry<K, V> entry = new AbstractMap.SimpleEntry<K, V>(key, value);
                        entryCol.add(entry);
                    }
                }
            }
        }
        if (mDataChangedListener.size() > 0 && entryCol.size() > 0) {
            for (OnDataChangedListener<K, V> listener : mDataChangedListener) {
                listener.onDataBatchRemove(entryCol);
            }
        }
        return entryCol;
    }

    /**
     * 通过Value移除所有匹配的数据
     *
     * @param value
     * @return
     */
    @NotNull
    public Collection<Map.Entry<K, V>> removeAllByValue(V value) {
        Collection<Map.Entry<K, V>> entryCol = new ArrayList<>();
        synchronized (mDataMap) {
            Iterator<Map.Entry<K, V>> it = mDataMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                if (CommonUtil.isObjEquals(value, entry.getValue())) {
                    it.remove();
                    entryCol.add(entry);
                }
            }
        }
        if (mDataChangedListener.size() > 0 && entryCol.size() > 0) {
            for (OnDataChangedListener<K, V> listener : mDataChangedListener) {
                listener.onDataBatchRemove(entryCol);
            }
        }
        return entryCol;
    }

    public int size() {
        return mDataMap.size();
    }

    /**
     * 清除所有数据
     */
    public void clear() {
        Collection<Map.Entry<K, V>> col = null;
        synchronized (mDataMap) {
            col = mDataMap.entrySet();
            mDataMap.clear();
        }
        if (col != null && col.size() > 0 &&
                mDataChangedListener.size() > 0) {
            for (OnDataChangedListener<K, V> listener : mDataChangedListener) {
                listener.onDataBatchRemove(col);
            }
        }
    }

    /**
     * 是否包含了某个key对象
     *
     * @param key
     * @return
     */
    public boolean containsKey(K key) {
        return key != null && mDataMap.containsKey(key);
    }

    /**
     * 是否包含了某个value的对象
     *
     * @param value
     * @return
     */
    public boolean containsValue(V value) {
        return mDataMap.containsValue(value);
    }

    @NotNull
    public Collection<V> getValues() {
        return mDataMap.values();
    }

    /**
     * 获取所有keys
     *
     * @return
     */
    @NotNull
    public Set<K> getKeys() {
        return mDataMap.keySet();
    }

    /**
     * 根据提供的filter使用key过滤获取第一个匹配的value
     *
     * @param keyFilter
     * @return
     */
    @Nullable
    public V filterFirstValueByKey(@NotNull Filterable<K> keyFilter) {
        synchronized (mDataMap) {
            for (K key : mDataMap.keySet()) {
                if (keyFilter.isMatch(key)) {
                    return mDataMap.get(key);
                }
            }
        }
        return null;
    }

    /**
     * 根据提供的filter使用value过滤获取第一个匹配的Value
     *
     * @param valueFilter
     * @return
     */
    @Nullable
    public V filterFirstValueByValue(@NotNull Filterable<V> valueFilter) {
        synchronized (mDataMap) {
            for (V value : mDataMap.values()) {
                if (valueFilter.isMatch(value)) {
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * 根据提供的filter使用key过滤获取第一个匹配的key
     *
     * @param keyFilter
     * @return
     */
    @Nullable
    public K filterFirstKeyByKey(@NotNull Filterable<K> keyFilter) {
        synchronized (mDataMap) {
            for (K key : mDataMap.keySet()) {
                if (keyFilter.isMatch(key)) {
                    return key;
                }
            }
        }
        return null;
    }

    /**
     * 根据提供的filter使用value过滤获取第一个匹配的key
     *
     * @param valueFilter
     * @return
     */
    @Nullable
    public K filterFirstKeyByValue(@NotNull Filterable<V> valueFilter) {
        synchronized (mDataMap) {
            for (K key : mDataMap.keySet()) {
                if (valueFilter.isMatch(mDataMap.get(key))) {
                    return key;
                }
            }
        }
        return null;
    }

    /**
     * 根据提供的Filter过滤获取第一个匹配的entry
     *
     * @param filter
     * @return
     */
    @Nullable
    public Map.Entry<K, V> filterFirstEntry(@NotNull Filterable<Map.Entry<K, V>> filter) {
        synchronized (mDataMap) {
            for (Map.Entry<K, V> entry : mDataMap.entrySet()) {
                if (filter.isMatch(entry)) {
                    return entry;
                }
            }
        }
        return null;
    }


    /**
     * 根据提供的filter过滤匹配的value,得到所有的符合条件的key列表
     *
     * @param valueFilter
     * @return
     */
    @NotNull
    public Collection<K> filterKeysByValue(@NotNull Filterable<V> valueFilter) {
        ArrayList<K> col = new ArrayList<>(mDataMap.size());
        synchronized (mDataMap) {
            for (Map.Entry<K, V> entry : mDataMap.entrySet()) {
                if (valueFilter.isMatch(entry.getValue())) {
                    col.add(entry.getKey());
                }
            }
        }
        return col;
    }

    /**
     * 根据提供的filter过滤获取到key列表
     *
     * @param keyFilter
     * @return
     */
    @NotNull
    public Collection<V> filterKeys(@NotNull Filterable<K> keyFilter) {
        ArrayList<V> col = new ArrayList<>();
        synchronized (mDataMap) {
            for (K key : mDataMap.keySet()) {
                if (keyFilter.isMatch(key)) {
                    col.add(mDataMap.get(key));
                }
            }
        }
        return col;
    }

    /**
     * 根据提供的filter过滤获取到value列表
     *
     * @param valueFileter
     * @return
     */
    @NotNull
    public Collection<V> filterValues(@NotNull Filterable<V> valueFileter) {
        ArrayList<V> col = new ArrayList<>();
        synchronized (mDataMap) {
            for (V value : mDataMap.values()) {
                if (valueFileter.isMatch(value)) {
                    col.add(value);
                }
            }
        }
        return col;
    }

    /**
     * 根据提供的filter过滤获取到entries列表
     *
     * @param entryFilter
     * @return
     */
    @NotNull
    public Collection<Map.Entry<K, V>> filterEntries(@NotNull Filterable<Map.Entry<K, V>> entryFilter) {
        ArrayList<Map.Entry<K, V>> col = new ArrayList<>();
        synchronized (mDataMap) {
            for (Map.Entry<K, V> entry : mDataMap.entrySet()) {
                if (entryFilter.isMatch(entry)) {
                    col.add(entry);
                }
            }
        }
        return col;
    }

    /**
     * 根据提供的action遍历操作对象,当满足条件时可以中止遍历返回
     *
     * @param action 遍历操作
     * @return true当发生中止遍历事件, false为全部进行过遍历
     */
    public boolean traverseFilterMap(@NotNull TraversalAction<K, V> action) {
        synchronized (mDataMap) {
            for (Map.Entry<K, V> entry : mDataMap.entrySet()) {
                if (action.doAction(entry.getKey(), entry.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 根据提供的action遍历操作对象,当满足条件时可以中止遍历返回,可操作iterator进行数据处理
     *
     * @param action 遍历迭代器的操作
     * @return true当发生中止遍历事件, false为全部进行过遍历
     */
    public boolean traverseIteratorFilterMap(@NotNull TraversalIteratorAction<K, V> action) {
        synchronized (mDataMap) {
            Iterator<Map.Entry<K, V>> it = mDataMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                if (action.doAction(it, entry.getKey(), entry.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 根据提供的action遍历所有对象,无法中止
     *
     * @param action 遍历操作
     */
    public void traverseAllMap(@NotNull TraversalAction<K, V> action) {
        synchronized (mDataMap) {
            for (Map.Entry<K, V> entry : mDataMap.entrySet()) {
                action.doAction(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 根据提供的action遍历所有对象,可操作iterator进行数据的处理(如移除)
     *
     * @param action 遍历迭代器的操作
     */
    public void traverseIteratorAllMap(@NotNull TraversalIteratorAction<K, V> action) {
        synchronized (mDataMap) {
            Iterator<Map.Entry<K, V>> it = mDataMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                action.doAction(it, entry.getKey(), entry.getValue());
            }
        }
    }
}
