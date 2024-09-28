package cn.oyzh.fx.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    public static String md5Hex(String input) {
        try {
            // 创建MD5 MessageDigest 实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            md.update(input.getBytes());
            // 获取密文
            byte[] digest = md.digest();
            // 将密文转换为十六进制的字符串形式
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().toLowerCase();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
