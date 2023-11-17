package cn.oyzh.fx.pkg.config;

import cn.hutool.core.io.resource.ResourceUtil;
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

    /**
     * 程序描述
     */
    private String desc;

    /**
     * 作者
     */
    private String vendor;

    /**
     * 程序名称
     */
    private String appName;

    /**
     * 版本号
     */
    private String version;

    /**
     * 可执行程序的名称
     */
    private String executable;

    public void parseConfig(JSONObject object) {
        String desc = object.getString("desc");
        if (desc != null) {
            this.desc = desc;
        }
        String vendor = object.getString("vendor");
        if (vendor != null) {
            this.vendor = vendor;
        }
        String version = object.getString("version");
        if (version != null) {
            this.version = version;
        }
        String appName = object.getString("appName");
        if (appName != null) {
            this.appName = appName;
        }
        String executable = object.getString("executable");
        if (executable != null) {
            this.executable = executable;
        } else {
            this.executable = this.appName;
        }
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

    /**
     * 加载配置
     *
     * @param configPath 配置路径
     * @return GlobalConfig
     */
    public static GlobalConfig loadConfig(String configPath) {
        String text = ResourceUtil.readUtf8Str(configPath);
        JSONObject object = JSONObject.parseObject(text);
        GlobalConfig config = new GlobalConfig();
        config.parseConfig(object);
        return config;
    }
}
