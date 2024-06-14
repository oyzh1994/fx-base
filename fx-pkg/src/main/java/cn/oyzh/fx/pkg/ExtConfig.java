package cn.oyzh.fx.pkg;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024/6/14
 */
@Getter
public class ExtConfig {

    @Setter
    private String jlinkJre;

    @Setter
    private String minimizeJre;

    private final Map<String, Object> properties = new HashMap<>();

    public void putProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public Object getProperty(String key) {
        return this.properties.get(key);
    }
}
