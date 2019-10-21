package cn.xlink.sdk.common.data;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

/**
 * 携带迭代器的遍历操作
 * Created by taro on 2018/4/8.
 */
public interface TraversalIteratorAction<K, V> {
    /**
     * 进行遍历操作,可利用迭代器进行一些操作,如移除对象等
     *
     * @param it    遍历过程的迭代器,
     * @param key
     * @param value
     * @return
     */
    public boolean doAction(@NotNull Iterator<Map.Entry<K, V>> it, @NotNull K key, @NotNull V value);
}
