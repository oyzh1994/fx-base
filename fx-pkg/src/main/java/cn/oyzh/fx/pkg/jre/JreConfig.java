package cn.oyzh.fx.pkg.jre;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * jre裁剪配置
 *
 * @author oyzh
 * @since 2023/03/09
 */
@Data
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
}
