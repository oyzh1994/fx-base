package cn.oyzh.fx.pkg.jre;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * jre配置
 *
 * @author oyzh
 * @since 2024/06/18
 */
public class JreConfig {

    private List<String> excludes;

    public void parseConfig(JSONObject object) {
        JSONArray excludes = object.getJSONArray("excludes");
        if (excludes != null) {
            this.excludes = new ArrayList<>();
            for (Object o : excludes) {
                this.excludes.add(o.toString());
            }
        }
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }
}
