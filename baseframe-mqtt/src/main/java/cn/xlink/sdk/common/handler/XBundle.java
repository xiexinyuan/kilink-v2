package cn.xlink.sdk.common.handler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Iterator;

/**
 * 使用json进行缓存数据的bundle
 * Created by taro on 2017/12/6.
 */
public class XBundle {
    //数据存储对象
    private JSONObject mMap;

    /**
     * 将字符串数据转成 bundle 对象
     *
     * @param json json字符串,若非有效的json字符串,则无法进行转换,并且推荐只包含基本类型数据的json
     * @return
     */
    public static XBundle fromString(String json) {
        if (json != null) {
            try {
                JSONObject obj = new JSONObject(json);
                return new XBundle(obj);
            } catch (JSONException ex) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 来自json对象的转存
     *
     * @param obj
     */
    public XBundle(JSONObject obj) {
        if (obj != null) {
            mMap = obj;
        } else {
            mMap = new JSONObject();
        }
    }

    /**
     * 来自同类型数据的转存
     *
     * @param bundle
     */
    public XBundle(XBundle bundle) {
        this();
        if (bundle != null) {
            Iterator<String> keys = bundle.mMap.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                mMap.put(key, bundle.mMap.get(key));
            }
        }
    }

    public XBundle() {
        mMap = new JSONObject();
    }

    public XBundle putInt(@NotNull String key, int value) {
        try {
            mMap.put(key, value);
        } catch (Exception ex) {
            //ignore
        }
        return this;
    }

    public XBundle putIntArray(@NotNull String key, int[] values) {
        try {
            mMap.put(key, values);
        } catch (Exception ex) {
            //ignore
        }
        return this;
    }

    public XBundle putLong(@NotNull String key, long value) {
        try {
            mMap.put(key, value);
        } catch (Exception ex) {
            //ignore
        }
        return this;
    }

    public XBundle putLongArray(@NotNull String key, long[] values) {
        try {
            mMap.put(key, values);
        } catch (Exception ex) {
            //ignore
        }
        return this;
    }

    public XBundle putFloat(@NotNull String key, float value) {
        try {
            mMap.put(key, value);
        } catch (Exception ex) {
            //ignore
        }
        return this;
    }

    public XBundle putFloatArray(@NotNull String key, float[] values) {
        try {
            mMap.put(key, values);
        } catch (Exception ex) {
            //ignore
        }
        return this;
    }

    public XBundle putDouble(@NotNull String key, double value) {
        try {
            mMap.put(key, value);
        } catch (Exception ex) {
            //ignore
        }
        return this;
    }

    public XBundle putDoubleArray(@NotNull String key, double[] values) {
        try {
            mMap.put(key, values);
        } catch (Exception ex) {
            //ignore
        }
        return this;
    }

    public XBundle putString(@NotNull String key, String value) {
        try {
            mMap.put(key, value);
        } catch (Exception ex) {
            //ignore
        }
        return this;
    }

    public XBundle putStringArray(@NotNull String key, String[] values) {
        try {
            mMap.put(key, values);
        } catch (Exception ex) {
            //ignore
        }
        return this;
    }

    public XBundle putSerializable(@NotNull String key, Serializable value) {
        try {
            mMap.put(key, value);
        } catch (Exception ex) {
            //ignore
        }
        return this;
    }

    public XBundle putByte(@NotNull String key, byte value) {
        try {
            mMap.put(key, value);
        } catch (Exception ex) {
            //ignore
        }
        return this;
    }

    public XBundle putByteArray(@NotNull String key, byte[] value) {
        try {
            mMap.put(key, value);
        } catch (Exception ex) {
            //ignore
        }
        return this;
    }

    public XBundle put(@NotNull String key, Object value) {
        try {
            mMap.put(key, value);
        } catch (Exception ex) {
            //ignore
        }
        return this;
    }

    public int getInt(String key) {
        return getValue(int.class, key);
    }

    public float getFloat(String key) {
        return getValue(float.class, key);
    }

    public long getLong(String key) {
        return getValue(long.class, key);
    }

    public double getDouble(String key) {
        return getValue(double.class, key);
    }

    public byte getByte(String key) {
        return getValue(byte.class, key);
    }

    public String getString(String key) {
        return getValue(String.class, key);
    }

    public Serializable getSerializable(String key) {
        return getValue(Serializable.class, key);
    }

    public int[] getIntArray(String key) {
        return getValue(int[].class, key);
    }

    public float[] getFloatArray(String key) {
        return getValue(float[].class, key);
    }

    public long[] getLongArray(String key) {
        return getValue(long[].class, key);
    }

    public double[] getDoubleArray(String key) {
        return getValue(double[].class, key);
    }

    public String[] getStringArray(String key) {
        return getValue(String[].class, key);
    }

    public byte[] getByteArray(String key) {
        return getValue(byte[].class, key);
    }

    public Object get(String key) {
        if (key != null && mMap.has(key)) {
            return mMap.get(key);
        }
        return null;
    }

    /**
     * 将对象转成json输出
     *
     * @param indentfactor
     * @return
     */
    public String toJson(int indentfactor) {
        if (indentfactor < 0 || indentfactor > 10) {
            indentfactor = 0;
        }
        return mMap.toString(indentfactor);
    }

    /**
     * 获取当前数据存储的数量,若存在多层数据,只返回第一层数据的数量
     *
     * @return
     */
    public int size() {
        return mMap.length();
    }

    @Override
    public String toString() {
        return mMap.toString();
    }

    private <T> T getValue(Class<T> clazz, String key) {
        if (key != null && mMap.has(key)) {
            Object obj = mMap.get(key);
            if (obj != null && obj.getClass() == clazz) {
                return (T) obj;
            }
        }
        return null;
    }
}
