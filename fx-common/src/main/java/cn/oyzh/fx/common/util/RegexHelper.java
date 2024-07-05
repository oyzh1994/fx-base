package cn.oyzh.fx.common.util;

import java.util.regex.Pattern;

/**
 * @author oyzh
 * @since 2024/7/5
 */
public class RegexHelper {

    /**
     * json符号正则模式
     */
    private static Pattern Json_Symbol_Pattern;

    public static Pattern jsonSymbolPattern() {
        if (Json_Symbol_Pattern == null) {
            Json_Symbol_Pattern = Pattern.compile("[{}|\\[\\]]");
        }
        return Json_Symbol_Pattern;
    }

    /**
     * json键正则模式
     */
    private static Pattern Json_Key_Pattern;

    public static Pattern jsonKeyPattern() {
        if (Json_Key_Pattern == null) {
            Json_Key_Pattern = Pattern.compile("\"([a-zA-Z0-9-_.]+[\\w\\s]*[\\w]+)\":");
        }
        return Json_Key_Pattern;
    }

    /**
     * json值正则模式
     */
    private static Pattern Json_Value_Pattern;

    public static Pattern jsonValuePattern() {
        if (Json_Value_Pattern == null) {
            Json_Value_Pattern = Pattern.compile("(?<=:)\\s*(\"[^\"]*\"|\\d+|true|false|\\[.*?]|\\{.*?})");
        }
        return Json_Value_Pattern;
    }
}
