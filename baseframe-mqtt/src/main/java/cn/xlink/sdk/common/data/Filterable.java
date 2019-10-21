package cn.xlink.sdk.common.data;

/**
 * Created by taro on 2018/1/2.
 */
public interface Filterable<T> {
    boolean isMatch(T value);
}
