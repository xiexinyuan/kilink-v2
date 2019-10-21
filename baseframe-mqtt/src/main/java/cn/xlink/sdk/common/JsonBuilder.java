package cn.xlink.sdk.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

/**
 * Json字符串输出转换,不使用gson
 * Created by taro on 2017/12/25.
 */
public class JsonBuilder {
    //空数组
    private static final JSONArray EMPTY_ARRAY = new JSONArray();
    //用于存在json的数据,一定会存在
    private JSONObject mJson;
    //用于存放数组的数据,不一定会存在
    private JSONArray mJsonArr;

    public static JsonBuilder newJsonBuilder() {
        return new JsonBuilder();
    }

    /**
     * 判断当前对象是否一个集合列表类型,或者是一个数组
     *
     * @param obj 对象为null时必定返回false
     * @return
     */
    public static boolean isArrayOrCollection(Object obj) {
        if (obj != null && obj.getClass().isArray()
                || obj instanceof Collection) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 将某个对象转成json数组.此参数可以是集合/Map/或者普通的数据对象,都会正常转换,包括将集合中的实例对象也会正常转换成json
     *
     * @param obj 任意类型的对象,暂时仅成转换public字段
     * @return
     */
    @NotNull
    public static JsonBuilder toJson(Object obj) {
        if (obj == null) {
            return new JsonBuilder();
        } else {
            if (obj instanceof Map) {
                //map
                return new JsonBuilder((Map) obj);
            } else if (isArrayOrCollection(obj)) {
                //数组或者collection
                return new JsonBuilder(obj);
            } else if (obj instanceof JsonBuilder) {
                return new JsonBuilder((JsonBuilder) obj);
            } else if (obj instanceof JSONObject) {
                return new JsonBuilder((JSONObject) obj);
            } else if (obj instanceof JSONArray) {
                return new JsonBuilder((JSONArray) obj);
            } else {
                JsonBuilder builder = new JsonBuilder();
                //实例对象
                Class clazz = obj.getClass();
                Field[] fields = clazz.getFields();
                try {
                    if (fields != null && fields.length > 0) {
                        for (Field field : fields) {
                            field.setAccessible(true);
                            String name = field.getName();
                            //实例对象还需要根据当前的数据再将传入判断类型并转换
                            builder.put(name, field.get(obj));
                        }
                    }
                } catch (Exception ex) {
                    //ignore exception
                }
                return builder;
            }
        }
    }

    /**
     * 将对象转换成json数组,此处支持深度转换,即允许列表中存在实例对象,实例对象中甚至可以带其它集合或者数组的实例
     *
     * @param obj 参数不允许为null
     * @return
     */
    @NotNull
    public static JSONArray toDeepJSONArray(@NotNull Object obj) {
        JSONArray jsonArray = new JSONArray();
        //collection,集合列表类型
        if (obj instanceof Collection) {
            Collection col = (Collection) obj;
            for (Object item : col) {
                jsonArray.put(wrap(item));
            }
        } else if (obj.getClass().isArray()) {
            //数组类型
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                Object item = Array.get(obj, i);
                jsonArray.put(wrap(item));
            }
        } else {
            //其它普通类型,再次转换
            jsonArray.put(wrap(obj));
        }
        return jsonArray;
    }


    /**
     * 将对象转换成实际有效的用于JSON化的对象
     *
     * @param object
     * @return
     */
    private static Object wrap(Object object) {
        try {
            //null对象
            if (object == null) {
                return JSONObject.NULL;
            }
            //jsonBuilder类型,转换为json对象
            if (object instanceof JsonBuilder) {
                JsonBuilder builder = (JsonBuilder) object;
                //若是json数组类型,转换成json数组
                if (builder.isJsonArray()) {
                    return builder.toJsonArray();
                } else {
                    //若是json对象类型,转换成json对象类型
                    return builder.toJsonObj();
                }
            }

            //基本的数据类型
            if (object instanceof JSONObject || object instanceof JSONArray
                    || JSONObject.NULL.equals(object) || object instanceof JSONString
                    || object instanceof Byte || object instanceof Character
                    || object instanceof Short || object instanceof Integer
                    || object instanceof Long || object instanceof Boolean
                    || object instanceof Float || object instanceof Double
                    || object instanceof String || object instanceof BigInteger
                    || object instanceof BigDecimal || object instanceof Enum) {
                return object;
            }

            //判断当前的数据类型是否为集合或者数组
            if (isArrayOrCollection(object)) {
                return toDeepJSONArray(object);
            }
            //判断当前的数据类型是否为map
            if (object instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) object;
                return toJson(map).toJsonObj();
            }
            //java相关的类,直接返回字符串
            Package objectPackage = object.getClass().getPackage();
            String objectPackageName = objectPackage != null ? objectPackage
                    .getName() : "";
            if (objectPackageName.startsWith("java.")
                    || objectPackageName.startsWith("javax.")
                    || object.getClass().getClassLoader() == null) {
                return object.toString();
            }

            //其它类型数据通过转换成json对象再返回
            return toJson(object).toJsonObj();
        } catch (Exception exception) {
            return null;
        }
    }

    public JsonBuilder() {
        mJson = new JSONObject();
    }

    public JsonBuilder(JSONObject json) {
        if (json != null) {
            //这里只能把json对象直接转移过来使用
            mJson = json;
        } else {
            mJson = new JSONObject();
        }
    }

    public JsonBuilder(JsonBuilder builder) {
        if (builder != null) {
            //这里需要注意,builder存在时,可能同时存在json或者jsonArr
            mJson = builder.mJson;
            mJsonArr = builder.mJsonArr;
        } else {
            mJson = new JSONObject();
        }
    }

    public JsonBuilder(Map<?, ?> map) {
        this();
        this.put(map);
    }

    public JsonBuilder(Collection<?> col) {
        this();
        mJsonArr = toDeepJSONArray(col);
    }

    public JsonBuilder(JSONArray array) {
        this();
        mJsonArr = array;
    }

    /**
     * 内部构造方法,仅用于生成jsonArray类型的数据
     *
     * @param arr
     */
    private JsonBuilder(@NotNull Object arr) {
        this();
        mJsonArr = toDeepJSONArray(arr);
    }

    /**
     * 判断当前的jsonBuilder对象是否为json数组,而不是一个json对象
     *
     * @return
     */
    public boolean isJsonArray() {
        return mJsonArr != null;
    }

    public JsonBuilder put(@NotNull String key, byte value) {
        mJson.put(key, value & 0xFF);
        return this;
    }

    public JsonBuilder put(@NotNull String key, short value) {
        mJson.put(key, value & 0xFFFF);
        return this;
    }

    public JsonBuilder put(@NotNull String key, int value) {
        mJson.put(key, value);
        return this;
    }

    public JsonBuilder put(@NotNull String key, long value) {
        mJson.put(key, value);
        return this;
    }

    public JsonBuilder put(@NotNull String key, boolean value) {
        mJson.put(key, value);
        return this;
    }

    public JsonBuilder put(@NotNull String key, String value) {
        if (value == null) {
            mJson.put(key, JSONObject.NULL);
        } else {
            mJson.put(key, value);
        }
        return this;
    }

    public JsonBuilder put(@NotNull String key, Collection<?> value) {
        if (value == null || value.size() <= 0) {
            mJson.put(key, EMPTY_ARRAY);
        } else {
            mJson.put(key, toDeepJSONArray(value));
        }
        return this;
    }

    public JsonBuilder put(@NotNull String key, Map<?, ?> value) {
        if (value == null || value.size() <= 0) {
            mJson.put(key, JSONObject.NULL);
        } else {
            mJson.put(key, wrap(value));
        }
        return this;
    }

    /**
     * 将map的数据都转换成对应的json数据,其中map的key会作为json的key,map的Value会作为json的Value
     *
     * @param value 当参数为null时忽略
     * @return
     */
    public JsonBuilder put(Map<?, ?> value) {
        if (value != null && value.size() > 0) {
            for (Map.Entry<?, ?> entry : value.entrySet()) {
                this.put(String.valueOf(entry.getKey()), wrap(entry.getValue()));
            }
        }
        return this;
    }

    public JsonBuilder put(@NotNull String key, Object[] arr) {
        if (arr == null) {
            mJson.put(key, EMPTY_ARRAY);
        } else {
            mJson.put(key, toDeepJSONArray(arr));
        }
        return this;
    }

    /**
     * 根据数据类型自动存储数据
     *
     * @param key
     * @param obj
     * @return
     */
    public JsonBuilder put(@NotNull String key, Object obj) {
        mJson.put(key, wrap(obj));
        return this;
    }

    /**
     * 将字节数组转成16进制字符串再存储到json中
     *
     * @param key
     * @param value
     * @return
     */
    public JsonBuilder putBytesToHex(@NotNull String key, byte[] value) {
        if (value == null) {
            mJson.put(key, EMPTY_ARRAY);
        } else {
            mJson.put(key, ByteUtil.bytesToHex(value));
        }
        return this;
    }

    /**
     * 将bytes数组转换成String保存
     *
     * @param key
     * @param value
     * @return
     */
    public JsonBuilder putBytesToString(@NotNull String key, byte[] value) {
        mJson.put(key, StringUtil.getStringEmptyDefault(value));
        return this;
    }

    /**
     * 清除所有数据
     */
    public JsonBuilder clear() {
        mJson = new JSONObject();
        return this;
    }

    /**
     * 返回当前可用的json对象,不可能为null,但可能没有任何内容
     *
     * @return
     */
    @NotNull
    public JSONObject toJsonObj() {
        return mJson;
    }

    /**
     * 返回当前可用的json数组对象,可能为null,当此json字符串不是以数组形式开始时则为null
     *
     * @return
     */
    @Nullable
    public JSONArray toJsonArray() {
        return mJsonArr;
    }

    /**
     * 转换成格式化的json字符串
     *
     * @param indentFactor
     * @return
     */
    @NotNull
    public String toFormatJson(int indentFactor) {
        if (indentFactor < 0 || indentFactor > 10) {
            indentFactor = 0;
        }
        //若当前为json数组,则输出json数组
        if (mJsonArr != null) {
            return mJsonArr.toString(indentFactor);
        } else if (mJson != null) {
            //否则输出json对象
            return mJson.toString(indentFactor);
        } else {
            return "{}";
        }
    }

    @Override
    public String toString() {
        return toFormatJson(0);
    }
}
