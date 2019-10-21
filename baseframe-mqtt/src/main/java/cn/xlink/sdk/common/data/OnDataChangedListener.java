package cn.xlink.sdk.common.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * 数据变动时的监听回调
 * Created by taro on 2018/4/19.
 */
public interface OnDataChangedListener<K, V> {
    /**
     * 数据添加时回调
     *
     * @param key
     * @param value
     */
    public void onDataAdd(@NotNull K key, @Nullable V value);

    /**
     * 数据变更时添加的回调
     *
     * @param key
     * @param value
     */
    public void onDataChanged(@NotNull K key, @Nullable V value);

    /**
     * 数据移除时的回调
     *
     * @param key
     * @param value
     */
    public void onDataRemove(@NotNull K key, @Nullable V value);

    /**
     * 数据指添加时的回调
     *
     * @param entries
     */
    public void onDataBatchAdd(@NotNull Collection<Map.Entry<K, V>> entries);

    /**
     * 数据批量移除时的回调
     *
     * @param entries
     */
    public void onDataBatchRemove(@NotNull Collection<Map.Entry<K, V>> entries);
}
