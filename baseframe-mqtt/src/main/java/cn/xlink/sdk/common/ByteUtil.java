package cn.xlink.sdk.common;

import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ByteUtil {
    final public static byte[] EMPTY_BYTES = new byte[0];
    final public static char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
    final public static String[] BIT_HEXS = new String[]{
            "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111",
            "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"
    };

    private static MessageDigest sMD5DigestInstance = null;


    public static byte setBit(byte src, int index, boolean isSet) {
        if (index > 7 || index < 0)
            throw new IndexOutOfBoundsException("index our of bounds");
        return (byte) (isSet ? (src | (0x01 << index)) : (src & ~(0x01 << index)));
    }

    /**
     * 将一个单字节的Byte转换成十六进制的数
     *
     * @param b byte
     * @return convert result
     */
    public static String byteToHex(byte b) {
        char[] hexChars = new char[2];
        int v = b & 0xFF;
        hexChars[0] = HEX_CHARS[v >>> 4];
        hexChars[1] = HEX_CHARS[v & 0x0F];
        return new String(hexChars);
    }

    /**
     * 将一个Byte数组转换成十六进制表示
     *
     * @param bytes
     * @return convert result
     * <p>
     * http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
     */
    public static String bytesToHexWithSeperator(byte[] bytes) {
        if (bytes == null || bytes.length <= 0) {
            return "";
        }
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = HEX_CHARS[v >>> 4];
            hexChars[j * 3 + 1] = HEX_CHARS[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    public static byte[] shortToByte(short src) {
        byte[] targets = new byte[2];
        shortToByte(targets, 0, src);
        return targets;
    }

    /**
     * 32位int转byte[]
     */
    public static byte[] intToByte(int src) {
        byte[] result = new byte[4];
        intToByte(result, 0, src);
        return result;
    }

    /**
     * 64位long转byte[]
     *
     * @param src
     * @return
     */
    public static byte[] longToByte(long src) {
        byte[] result = new byte[8];
        longToByte(result, 0, src);
        return result;
    }


    /**
     * 16进制字符串转字节数组。
     *
     * @param s
     * @return
     */
    public static byte[] hexToBytes(String s) {
        if (s == null || s.length() <= 0) {
            return EMPTY_BYTES;
        }
        int len = s.length();
        // 对于基数长度的字符串做了一个适配
        if (len % 2 != 0) {
            s = s.substring(0, s.length() - (len % 2));
            len = s.length();
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 短整形转成byte
     *
     * @param targets
     * @param offset
     * @param src
     * @return
     */
    public static boolean shortToByte(byte[] targets, int offset, short src) {
        if (targets != null && targets.length >= offset + 2) {
            targets[offset] = (byte) ((src >> 8) & 0xff);
            targets[offset + 1] = (byte) (src & 0xff);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 32位int转byte[]
     */
    public static boolean intToByte(byte[] targets, int offset, int src) {
        if (targets != null && targets.length >= offset + 4) {
            targets[offset] = (byte) ((src >> 24) & 0xFF);
            targets[offset + 1] = (byte) ((src >> 16) & 0xFF);
            targets[offset + 2] = (byte) ((src >> 8) & 0xFF);
            targets[offset + 3] = (byte) (src & 0xFF);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 64位long转byte[]
     *
     * @param targets
     * @param offset
     * @param src
     * @return
     */
    public static boolean longToByte(byte[] targets, int offset, long src) {
        if (targets != null && targets.length >= offset + 8) {
            for (int i = 0; i < 8; i++) {
                targets[offset + i] = (byte) ((src >> 8 * (7 - i)) & 0xFF);
            }
            return true;
        } else {
            return false;
        }
    }

    public static byte[] intArrayToBytes(int[] ints) {
        if (ints == null || ints.length == 0) {
            return EMPTY_BYTES;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(ints.length * 4);
        byteBuffer.asIntBuffer().put(ints);
        return byteBuffer.array();
    }

    public static int[] byteToIntArray(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return new int[0];
        }
        int[] result = new int[bytes.length / 4];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.asIntBuffer().get(result);
        return result;
    }

    /**
     * 16进制字符串转字节数组。
     *
     * @param s
     * @return
     */
    public static boolean hexToBytes(byte[] targets, int offset, String s) {
        if (s == null) {
            return false;
        }
        int len = s.length();
        // 对于基数长度的字符串做了一个适配
        if (len % 2 != 0) {
            s = s.substring(0, s.length() - (len % 2));
            len = s.length();
        }
        if (targets != null && targets.length >= offset + len) {
            for (int i = 0; i < len; i += 2) {
                targets[offset + i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
            }
            return true;
        } else {
            return false;
        }
    }

    public static short byteToShort(byte[] bytes) {
        return byteToShort(bytes, 0, (short) -1);
    }

    public static short byteToShort(byte high, byte low) {
        return (short) (((high << 8) & 0xFF00) | (low & 0xFF));
    }

    //unshort无法在java中表示出来,只能用int表示
    public static int byteToUnsignedShort(byte high, byte low) {
        return ((high << 8) & 0xFF00 | low & 0x00FF);
    }

    /**
     * 将bytes数组转成long值
     *
     * @param bytes byte数组
     * @return
     */
    public static long byteToLong(byte[] bytes) {
        return byteToLong(bytes, 0, -1);
    }

    /***
     * 将bytes数组转成long值
     *
     * @param byteNum      byte数组
     * @param offset       开始转化字节数组的偏移量
     * @param defaultValue 默认的值(如果参数不合法将返回默认值)
     * @return
     */
    public static long byteToLong(byte[] byteNum, int offset, long defaultValue) {
        if (byteNum == null || byteNum.length <= offset) {
            return defaultValue;
        }
        long num = 0;
        int index = 0;
        int remain = byteNum.length - offset;
        for (int ix = 0; ix < remain && ix < 8; ix++) {
            index = offset + (remain - ix - 1);
            num |= (byteNum[index] & 0xFFL) << (8 * ix);
        }
        return num;
    }

    public static short byteToShort(byte[] b, int offset, short defaultValue) {
        if (b == null || b.length < offset + 2) {
            return defaultValue;
        }
        return byteToShort(b[offset], b[offset + 1]);
    }

    public static String printBits(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b :
                bytes) {
            sb.append(BIT_HEXS[b >>> 4 & 0x0F]);
            sb.append(" ");
            sb.append(BIT_HEXS[b & 0x0F]);
            sb.append(" ");
        }
        return sb.toString();
    }

    public static byte XORShort(short data) {
        return (byte) ((data >>> 8 & 0xFF) ^ (data & 0xFF));
    }

    public static String shortToHex(short cmd) {
        return byteToHex((byte) (cmd >>> 8)) + byteToHex((byte) (cmd & 0xff));
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_CHARS[v >>> 4];
            hexChars[j * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * 无符号短整形转成int
     *
     * @param src
     * @return
     */
    public static int unshortToInt(short src) {
        return byteToInt(shortToByte(src));
    }

    /**
     * byte数组转成int,默认返回-1
     *
     * @param src
     * @return
     */
    public static int byteToInt(byte[] src) {
        return byteToInt(src, 0, -1);
    }

    /**
     * 字节数组转成int,这里字节数组只要>=1个字节都可以,多于4个字节最多只会转换最后4个字节成为一个int
     *
     * @param src          字节数组,允许为>=1长度的任何字节数组,最多会使用4个字节
     * @param offset       偏移量
     * @param defaultValue 默认值
     * @return
     */
    public static int byteToInt(byte[] src, int offset, int defaultValue) {
        if (src == null || src.length <= offset) {
            return defaultValue;
        }
        int value = 0, index = 0;
        //从0ffset位置开始,还有剩下的byte字节数量
        int remain = src.length - offset;
        //整数最大为4个字节,最多只需要4个字节
        for (int i = 0; i < remain && i < 4; i++) {
            //remain-i:从字节数组的最后位字节开始向前扫描
            //8*i:从字节数组的最后位字节开始向前,每一个字节左移8位,得到对应位置的bits
            index = offset + (remain - i - 1);
            value |= (src[index] & 0xFF) << 8 * i;
        }
//        value = (((src[offset] & 0xFF) << 24) | ((src[offset + 1] & 0xFF) << 16) | ((src[offset + 2] & 0xFF) << 8) | (src[offset + 3] & 0xFF));
        return value;
    }

    /**
     * @param data1
     * @param data2
     * @return
     */
    public static byte[] concatBytes(byte data1[], byte data2[]) {
        if (data1 == null || data2 == null) {
            return EMPTY_BYTES;
        }
        byte temp[] = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, temp, 0, data1.length);
        System.arraycopy(data2, 0, temp, data1.length, data2.length);
        return (temp);
    }


    /**
     * @param data1
     * @param off1
     * @param len1
     * @param data2
     * @param off2
     * @param len2
     * @return
     */
    public static byte[] concatBytes(byte data1[], int off1, int len1, byte data2[], int off2, int len2) {
        if (data1 == null || data2 == null) {
            return EMPTY_BYTES;
        }
        byte temp[] = new byte[len1 + len2];
        System.arraycopy(data1, off1, temp, 0, len1);
        System.arraycopy(data2, off2, temp, len1, len2);
        return (temp);
    }

    /**
     * @param data
     * @param offset
     * @param length
     * @return
     */
    public static byte[] sliceByteArray(byte data[], int offset, int length) {
        if (data == null) {
            return EMPTY_BYTES;
        }
        byte temp[] = new byte[length];
        System.arraycopy(data, offset, temp, 0, length);
        return temp;
    }

    /**
     * 获取字节数组的长度并返回short的长度(注意该长度最大值可以为65535,表示可能是负数)
     *
     * @param value
     * @return
     */
    public static short getBytesLengthForShort(byte[] value) {
        int len = getBytesLength(value);
        return intSubLastShort(len);
    }

    /**
     * 获取当前字节数组的长度
     *
     * @param value
     * @return
     */
    public static int getBytesLength(byte[] value) {
        return value == null ? 0 : value.length;
    }

    /**
     * 判断当前字节数组是否为空数组
     *
     * @param value
     * @return
     */
    public static boolean isEmpty(byte[] value) {
        return getBytesLength(value) == 0;
    }

    public static boolean isZeroByteArray(byte[] src) {
        if (isEmpty(src)) {
            return false;
        }
        for (byte b : src) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    public static byte[] reverseByteArray(byte[] src) {
        if (isEmpty(src)) {
            return EMPTY_BYTES;
        }
        for (int i = 0; i < src.length / 2; i++) {
            byte temp = src[i];
            src[i] = src[src.length - i - 1];
            src[src.length - i - 1] = temp;
        }
        return src;
    }

    /**
     * MD5加密
     *
     * @param src
     * @return
     */
    public static byte[] digestMD5(@Nullable byte[] src) {
        if (isEmpty(src)) {
            return EMPTY_BYTES;
        }
        if (sMD5DigestInstance == null) {
            try {
                sMD5DigestInstance = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return sMD5DigestInstance.digest(src);
    }

    /**
     * 获取int值的最后一个byte值
     *
     * @param value
     * @return
     */
    public static byte intSubLastByte(int value) {
        return intSubByte(value, 3);
    }

    /**
     * 获取int值中某个位置的byte值
     *
     * @param value
     * @param index index不在0-4之间时,都会返回0无效
     * @return
     */
    public static byte intSubByte(int value, int index) {
        if (index < 0 || index >= 4) {
            return 0x0;
        } else {
            return (byte) ((value >> (3 - index) * 8) & 0xFF);
        }
    }

    /**
     * 获取int的后半部分short
     *
     * @param value
     * @return
     */
    public static short intSubLastShort(int value) {
        short result = 0;
        result |= value & 0xFF00;
        result |= value & 0xFF;
        return result;
    }
}
