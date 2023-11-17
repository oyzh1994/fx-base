package cn.oyzh.fx.pkg.config;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * 全局配置
 *
 * @author oyzh
 * @since 2023/11/15
 */
@Data
public class GlobalConfig {

    /**
     * jar路径
     */
    private String jarPath;

    /**
     * 目标路径
     */
    private String destPath;

    /**
     * 是否保留过程文件
     */
    private boolean retainDuringDir;

    public void parseConfig(JSONObject object) {
        String jarPath = object.getString("jarPath");
        if (jarPath != null) {
            this.jarPath = jarPath;
        }
        String destPath = object.getString("destPath");
        if (destPath != null) {
            this.destPath = destPath;
        }
        Boolean retainDuringDir = object.getBoolean("retainDuringDir");
        if (retainDuringDir != null) {
            this.retainDuringDir = retainDuringDir;
        }
    }
}
