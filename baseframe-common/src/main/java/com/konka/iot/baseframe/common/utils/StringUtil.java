package com.konka.iot.baseframe.common.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Created by legendmohe on 16/5/20.
 */
public class StringUtil {

    private static final int PAD_LIMIT = 8192;
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static String[] macRandom = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    /**
     * 获取字节数组解析成的字符串数据,出现异常返回空字符串
     *
     * @param data
     * @return
     */
    @NotNull
    public static String getStringEmptyDefault(byte[] data) {
        try {
            return getString(data);
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * 获取字节数组解析成的字符串数据,出现异常返回空字符串
     *
     * @param data
     * @param offset 跳过的长度
     * @param len    需要解析的字符长度
     * @return
     */
    @NotNull
    public static String getStringEmptyDefault(byte[] data, int offset, int len) {
        try {
            return getString(data, offset, len, Charset.forName(DEFAULT_CHARSET));
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * 获取字节数组解析成的字符串数据,出现异常返回默认字符串值
     *
     * @param data
     * @param defaultStr
     * @return
     */
    @NotNull
    public static String getString(byte[] data, String defaultStr) {
        try {
            return getString(data);
        } catch (Exception ex) {
            return defaultStr;
        }
    }

    /**
     * 获取字符数组解析成的字符串数据,使用默认字符集
     *
     * @param data
     * @return
     * @throws Exception 解析出错的异常
     */
    @NotNull
    public static String getString(byte[] data) throws Exception {
        return getString(data, 0, data.length, Charset.forName(DEFAULT_CHARSET));
    }

    /**
     * 获取字符数组解析成的字符串数据
     *
     * @param data
     * @param offset
     * @param len
     * @param requestCharset 若为空,则使用默认的字符串集(UTF-8)  @return
     * @throws Exception 解析出错的异常
     */
    public static String getString(byte[] data, int offset, int len, Charset requestCharset) throws Exception {
        if (data.length > 0) {
            if (requestCharset == null) {
                requestCharset = Charset.forName(DEFAULT_CHARSET);
            }
            return new String(data, offset, len, requestCharset);
        } else {
            return "";
        }
    }

    /**
     * 获取指定字符串集的字符串转成的byte
     *
     * @param value
     * @param requestCharset 若为空,则使用默认的字符串集(UTF-8)
     * @return
     */
    @NotNull
    public static byte[] getBytes(String value, Charset requestCharset) {
        if (value == null || value.isEmpty()) {
            return new byte[0];
        } else {
            if (requestCharset == null) {
                requestCharset = Charset.forName(DEFAULT_CHARSET);
            }
            return value.getBytes(requestCharset);
        }
    }

    /**
     * 获取默认字符串集(UTF-8)的字符串转成的Byte
     *
     * @param value
     * @return
     */
    @NotNull
    public static byte[] getBytes(String value) {
        return getBytes(value, Charset.forName(DEFAULT_CHARSET));
    }

    /**
     * 判断两个字符串是否完全相同
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 完全相同返回true, 否则返回false
     */
    public static boolean equals(String str1, String str2) {
        if (str1 == str2) {
            return true;
        } else if (str1 == null || str2 == null) {
            return false;
        } else {
            return str1.equals(str2);
        }
    }

    /**
     * 解析ip字符串为地址字节数组,若解析失败时,返回空字节数组
     *
     * @param ip 标准IP地址,4段,不允许带其它任何字符
     * @return
     */
    @NotNull
    public static byte[] ipToBytes(@NotNull String ip) {
        String[] adds = ip.split("\\.");
        if (adds.length == 4) {
            byte[] addByte = new byte[4];
            for (int i = 0; i < adds.length; i++) {
                try {
                    addByte[i] = ByteUtil.intSubLastByte(Integer.valueOf(adds[i]));
                } catch (Exception ex) {
                    return new byte[0];
                }
            }
            return addByte;
        } else {
            return new byte[0];
        }
    }

    /**
     * 解析ip字节数组为字符串地址,若解析失败时,返回空字符串
     *
     * @param ipBytes ip字符数组,必须是4个字节
     * @return
     */
    @NotNull
    public static String bytesToIp(byte[] ipBytes) {
        if (ipBytes == null || ipBytes.length != 4) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder(20);
            for (byte addr : ipBytes) {
                builder.append(String.valueOf(addr & 0xFF));
                builder.append('.');
            }
            //删除掉最后一个.
            if (builder.length() > 0) {
                builder.deleteCharAt(builder.length() - 1);
            }
            return builder.toString();
        }
    }

    /**
     * 获取多个字符串的hash值
     *
     * @param values
     * @return
     */
    public static int hashStrCode(@Nullable String... values) {
        if (values == null || values.length <= 0) {
            return 0;
        } else {
            int h = 0;
            for (String value : values) {
                if (value != null && value.length() > 0) {
                    for (int i = 0; i < value.length(); i++) {
                        h = 31 * h + value.charAt(i);
                    }
                }
            }
            return h;
        }
    }

    public static String subFixLengthString(String src, int length) {
        if (isEmpty(src)) {
            return "";
        } else if (src.length() > length) {
            return src.substring(0, length);
        } else {
            return src;
        }
    }

    /**
     * 判断字符串是否为byte字符串
     *
     * @param str
     * @return
     */
    public static boolean isByteStr(String str) {
        if (StringUtil.isEmpty(str)) {
            return false;
        } else {
            Pattern pattern = Pattern.compile("-?[0-9a-fA-F]*");
            return pattern.matcher(str).matches();
        }
    }

    public static boolean isEmpty(String target) {
        return target == null || target.length() == 0;
    }

    public static boolean isNotEmpty(String target) {
        return target != null && target.length() != 0;
    }

    public static boolean isAllNotEmpty(String... targets) {
        if (targets != null) {
            for (String target :
                    targets) {
                if (isEmpty(target)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static void appendIfNotEmpty(StringBuilder sb, String target, String sep) {
        if (sb == null)
            return;
        if (isNotEmpty(target)) {
            sb.append(target);
            if (isNotEmpty(sep)) {
                sb.append(sep);
            }
        }
    }

    public static void appendIfNotEmpty(StringBuilder sb, String target) {
        appendIfNotEmpty(sb, target, null);
    }

    public static String joinStrings(String sep, Collection<String> strings) {
        if (CommonUtil.isEmpty(strings))
            return "";
        StringBuilder sb = new StringBuilder();
        for (String item :
                strings) {
            if (item != null && item.length() != 0) {
                sb.append(item);
                sb.append(sep);
            }
        }
        if (sb.length() != 0) {
            sb.delete(sb.length() - sep.length(), sb.length() - 1);
        }
        return sb.toString();
    }

    public static String joinStrings(String sep, String... strings) {
        if (CommonUtil.isEmpty(strings))
            return "";
        Collection<String> col = Arrays.asList(strings);
        return joinStrings(sep, col);
    }

    public static String chooseNotNull(String... strings) {
        for (String curString :
                strings) {
            if (!StringUtil.isEmpty(curString))
                return curString;
        }
        return "";
    }

    private static String padding(int repeat, char padChar) throws IndexOutOfBoundsException {
        if (repeat < 0) {
            throw new IndexOutOfBoundsException("Cannot pad a negative amount: " + repeat);
        }
        final char[] buf = new char[repeat];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = padChar;
        }
        return new String(buf);
    }

    public static String rightPad(String str, int size) {
        return rightPad(str, size, ' ');
    }

    public static String rightPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > PAD_LIMIT) {
            return rightPad(str, size, String.valueOf(padChar));
        }
        return str.concat(padding(pads, padChar));
    }

    public static String rightPad(String str, int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = " ";
        }
        int padLen = padStr.length();
        int strLen = str.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return rightPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return str.concat(padStr);
        } else if (pads < padLen) {
            return str.concat(padStr.substring(0, pads));
        } else {
            char[] padding = new char[pads];
            char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return str.concat(new String(padding));
        }
    }

    public static String leftPad(String str, int size) {
        return leftPad(str, size, ' ');
    }

    public static String leftPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > PAD_LIMIT) {
            return leftPad(str, size, String.valueOf(padChar));
        }
        return padding(pads, padChar).concat(str);
    }

    public static String leftPad(String str, int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = " ";
        }
        int padLen = padStr.length();
        int strLen = str.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return leftPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return padStr.concat(str);
        } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        } else {
            char[] padding = new char[pads];
            char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return new String(padding).concat(str);
        }
    }

    public static String beautifulArray(String[] src) {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (String item :
                src) {
            sb.append(item).append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * @return 随机生成12位mac
     */
    public static String randomMac() {
        StringBuilder builder = new StringBuilder();
        builder.append(macRandom[(int) (Math.random() * 11 + 1)]);
        for (int i = 0; i < 11; i++) {
            builder.append(macRandom[(int) (Math.random() * 12)]);
        }
        return builder.toString();
    }
}
