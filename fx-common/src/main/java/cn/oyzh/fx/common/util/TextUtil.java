package cn.oyzh.fx.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.experimental.UtilityClass;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 文本工具类
 *
 * @author oyzh
 * @since 2023/3/30
 */
@UtilityClass
public class TextUtil {

    /**
     * 搜索索引
     *
     * @param text        文字
     * @param word        词汇
     * @param formIndex   开始位置
     * @param compareCase 是否比较大小写
     * @param fullMatch   是否全文匹配
     * @return 索引位置
     */
    public static int findIndex(String text, String word, Integer formIndex, boolean compareCase, boolean fullMatch) {
        if (text == null || word == null) {
            return -1;
        }
        if (text.length() < word.length()) {
            return -1;
        }
        if (!compareCase) {
            text = text.toLowerCase();
            word = word.toLowerCase();
        }
        // 全文匹配
        if (fullMatch && text.equals(word)) {
            return 0;
        }
        // 搜索索引
        int start;
        if (formIndex == null) {
            start = text.indexOf(word);
        } else {
            start = text.indexOf(word, formIndex);
        }
        return start;
    }

    /**
     * 更换内容字符集
     *
     * @param bytes         内容
     * @param fromCharset   原始字符集
     * @param targetCharset 目标字符集
     * @return 处理字符集后的内容
     */
    public static byte[] changeCharset(byte[] bytes, String fromCharset, String targetCharset) {
        if (bytes == null || bytes.length == 0) {
            return bytes;
        }
        // 转换字符集
        return new String(bytes, getCharset(fromCharset)).getBytes(getCharset(targetCharset));
    }

    /**
     * 更换内容字符集
     *
     * @param str           内容
     * @param fromCharset   原始字符集
     * @param targetCharset 目标字符集
     * @return 处理字符集后的内容
     */
    public static String changeCharset(String str, String fromCharset, String targetCharset) {
        if (str == null || str.length() == 0) {
            return str;
        }
        // 转换字符集
        try {
            return new String(str.getBytes(fromCharset), targetCharset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 获取字符集
     *
     * @param name 字符名称
     * @return 字符集
     */
    public static Charset getCharset(String name) {
        if (name == null || name.isEmpty() || Objects.equals("跟随系统", name)) {
            return Charset.defaultCharset();
        }
        try {
            return Charset.forName(name);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Charset.defaultCharset();
    }

    /**
     * 格式美化
     *
     * @param list          字符串列表
     * @param lineItemLimit 单行内容个数限制
     * @return 美化后的字符
     */
    public static String beautifyFormat(Collection<String> list, int lineItemLimit, int spacing) {
        if (list == null || lineItemLimit <= 0) {
            return "";
        }
        List<List<String>> lists = CollUtil.split(list, lineItemLimit);
        // 记录单行的最大字符宽
        Map<Integer, Integer> lenMap = new HashMap<>();
        for (List<String> subList : lists) {
            for (int i = 0; i < subList.size(); i++) {
                String str = subList.get(i);
                Integer maxLen = lenMap.get(i);
                int strLen = getDisplayLen(str);
                if (maxLen == null || strLen > maxLen) {
                    lenMap.put(i, strLen);
                }
            }
        }

        // 拼接字符
        StringBuilder builder = new StringBuilder();
        for (List<String> strings : lists) {
            for (int i = 0; i < strings.size(); i++) {
                String str = strings.get(i);
                builder.append(str);
                int maxLen = lenMap.get(i);
                int strLen = getDisplayLen(str);
                int len = maxLen - strLen;
                // 使用空格填充
                builder.append(" ".repeat(len));
                // 使用空格填充间距
                if (spacing > 0) {
                    builder.append(" ".repeat(spacing));
                }
                // 然后进行tab补全
                builder.append("\t");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * 获取字符显示长度
     *
     * @param str 字符长
     * @return 显示长度
     */
    public static int getDisplayLen(String str) {
        if (str == null || str.isEmpty()) {
            return 0;
        }
        char[] charArray = str.toCharArray();
        double displayLen = 0;
        for (char c : charArray) {
            if (isChinese(c)) {
                displayLen += 1.5;
            } else {
                displayLen += 1;
            }
        }
        return (int) displayLen;
    }

    /**
     * 是否中文字符
     *
     * @param c 字符
     * @return 结果
     */
    public static boolean isChinese(char c) {
        Character.UnicodeScript sc = Character.UnicodeScript.of(c);
        return sc == Character.UnicodeScript.HAN;
    }

    /**
     * 获取json数据
     *
     * @return json数据
     */
    public String getJsonData(Object rawData) {
        if (rawData == null) {
            return null;
        }
        String data = null;
        try {
            if (rawData instanceof byte[] bytes) {
                data = new String(bytes);
            }
            // if (rawData instanceof Byte[] bytes) {
            //     byte[] bytes1 = new byte[bytes.length];
            //     for (int i = 0; i < bytes1.length; i++) {
            //         bytes1[i] = bytes[i];
            //     }
            //     data = new String(bytes1);
            // }
            if (rawData instanceof CharSequence sequence) {
                data = sequence.toString();
            }
            if (data == null) {
                return null;
            }
            if (!data.contains("{") && !data.contains("[")) {
                return data;
            }
            return JSONUtil.toJsonPrettyStr(data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return data;
    }

    /**
     * 获取二进制数据
     *
     * @return 二进制数据
     */
    public String getBinaryData(Object rawData) {
        if (rawData == null) {
            return null;
        }
        // if (rawData instanceof Byte[] bytes) {
        //     return StringUtil.toBinary(bytes);
        // }
        if (rawData instanceof byte[] bytes) {
            return StringUtil.toBinary(bytes);
        }
        if (rawData instanceof CharSequence sequence) {
            return StringUtil.toBinary(sequence.toString());
        }
        return null;
    }

    /**
     * 获取十六进制数据
     *
     * @return 十六进制数据
     */
    public String getHexData(Object rawData) {
        if (rawData == null) {
            return "";
        }
        if (rawData instanceof CharSequence sequence) {
            return HexUtil.encodeHexStr(sequence.toString().getBytes(), false);
        }
        if (rawData instanceof byte[] bytes) {
            return HexUtil.encodeHexStr(bytes, false);
        }
        return null;
    }

    /**
     * 获取字符串数据
     *
     * @return 十字符串数据
     */
    public String getStringData(Object rawData) {
        if (rawData == null) {
            return "";
        }
        if (rawData instanceof CharSequence sequence) {
            return sequence.toString();
        }
        if (rawData instanceof byte[] bytes) {
            return new String(bytes);
        }
        return null;
    }

    /**
     * byte转bit字符串
     *
     * @param bytes 数据
     * @return bit字符串
     */
    public static String byteToBitStr(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (byte aByte : bytes) {
            builder.append(byteToBitStr(aByte));
        }
        return builder.toString();
    }

    /**
     * byte转bit字符串
     *
     * @param b 数据
     * @return bit字符串
     */
    public static String byteToBitStr(byte b) {
        StringBuilder builder = new StringBuilder();
        // 将字节与0xFF进行按位与运算，保留最低8位
        int bitValue = b & 0xFF;
        for (int i = 7; i >= 0; i--) {
            builder.append((bitValue & (1 << i)) != 0 ? "1" : "0");
        }
        return builder.toString();
    }

    /**
     * bit字符串转byte
     *
     * @param bitStr bit字符串
     * @return byte数组
     */
    public static byte[] bitStrToByte(String bitStr) {
        if (bitStr == null || bitStr.isEmpty()) {
            return null;
        }
        String[] bits = StrUtil.split(bitStr, 8);
        byte[] bytes = new byte[bits.length];
        int i = 0;
        for (String bit : bits) {
            // 将二进制字符串转换为十进制整数
            int decimalValue = Integer.parseInt(bit, 2);
            // 将十进制整数转换为字节
            byte byteValue = Byte.parseByte(String.valueOf(decimalValue));
            bytes[i++] = byteValue;
        }
        return bytes;
    }

}
