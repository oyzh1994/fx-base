package cn.oyzh.fx.common.util;

/**
 * @author oyzh
 * @since 2024-09-29
 */
public class HexUtil {

    public static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, true);
    }

    public static String bytesToHex(byte[] bytes, boolean toUpperCase) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        String hexStr = hexString.toString();
        if (toUpperCase) {
            return hexStr.toUpperCase();
        }
        return hexStr;
    }
}
