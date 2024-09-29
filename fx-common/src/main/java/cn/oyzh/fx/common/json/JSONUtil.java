package cn.oyzh.fx.common.json;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.NonNull;

import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-29
 */
public class JSONUtil {

    public static String toJsonPrettyStr(Object obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.PrettyFormat);
    }

    public static String toJsonStr(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static <T> List<T> toList(String json, Class<T> tClass) {
        return JSON.parseArray(json, tClass);
    }

    public static <T> List<T> toList(JSONArray array, Class<T> tClass) {
        return array.toList(tClass);
    }

    public static boolean isJsonText(String json) {
        return JSON.isValid(json);
    }

    public static JSONObject parseObj(@NonNull String json) {
        return JSON.parseObject(json);
    }

    public static <T> T toBean(String text, Class<T> beanClass) {
        return JSON.to(beanClass,text);
    }
}
