package cn.oyzh.fx.pkg.jre;

import cn.oyzh.fx.pkg.ConfigMargeAble;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * jre配置
 *
 * @author oyzh
 * @since 2024/06/18
 */
public class JreConfig implements ConfigMargeAble<JreConfig> {

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 排除的文件
     */
    private Set<String> excludes;

    public void parseConfig(JSONObject object) {
        JSONArray excludes = object.getJSONArray("excludes");
        if (excludes != null) {
            this.excludes = new HashSet<>();
            for (Object o : excludes) {
                this.excludes.add(o.toString());
            }
        }
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }

    public boolean isEnable() {
        return enable == null || this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public void marge(JreConfig config) {
        if (config == null) {
            return;
        }
        if (this.excludes == null) {
            this.excludes = config.excludes;
        } else if (config.excludes != null){
            this.excludes.addAll(config.excludes);
        }
        if (config.enable != null) {
            this.enable = config.enable;
        }
    }
}
