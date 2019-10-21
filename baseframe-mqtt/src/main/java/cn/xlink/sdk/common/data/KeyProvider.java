package cn.xlink.sdk.common.data;

import org.jetbrains.annotations.Nullable;

/**
 * K,V的提供者
 * Created by taro on 2018/4/19.
 */
public interface KeyProvider<K, V> {
    /**
     * 从vlaue中获取到key的值
     *
     * @param value
     * @return
     */
    @Nullable
    public K getKeyFromValue(@Nullable V value);
}
