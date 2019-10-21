package cn.xlink.sdk.common.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 遍历操作
 * Created by taro on 2018/1/2.
 */
public interface TraversalAction<K, V> {
    /**
     * 进行遍历行为,可返回值,该值返回的意义由具体的使用场景决定,可能会忽略返回值
     *
     * @param key
     * @param value
     * @return
     */
    boolean doAction(@NotNull K key, @Nullable V value);
}
