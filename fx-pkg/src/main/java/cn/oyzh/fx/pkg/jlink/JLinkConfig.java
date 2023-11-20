package cn.oyzh.fx.pkg.jlink;

import cn.oyzh.fx.pkg.config.BaseConfig;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2023/3/8
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JLinkConfig extends BaseConfig {

    /**
     * vm类型
     */
    private String vm = "server";

    /**
     * 输出目录
     */
    private String output;

    /**
     * 压缩等级
     */
    private Integer compress = 2;

    /**
     * 打印过程日志
     */
    private boolean verbose = true;

    /**
     * 不无需主页面
     */
    private boolean noManPages = true;

    /**
     * 无需头文件
     */
    private boolean noHeaderFiles = true;

    /**
     * 去除debug文件
     */
    private boolean stripDebug = true;

    /**
     * 去除debug属性
     */
    private boolean stripJavaDebugAttributes = true;

    /**
     * 添加的模块
     */
    private List<String> addModules;

    /**
     * 排除文件
     */
    private List<String> excludeFiles;

    public void parseConfig(JSONObject object) {
        super.parseConfig(object);
        JSONArray excludeFiles = object.getJSONArray("excludeFiles");
        if (excludeFiles != null) {
            this.excludeFiles = new ArrayList<>();
            for (Object o : excludeFiles) {
                this.excludeFiles.add(o.toString());
            }
        }
        JSONArray addModules = object.getJSONArray("addModules");
        if (addModules != null) {
            this.addModules = new ArrayList<>();
            for (Object o : addModules) {
                this.addModules.add(o.toString());
            }
        }
        Boolean verbose = object.getBoolean("verbose");
        if (verbose != null) {
            this.verbose = verbose;
        }
        Boolean noManPages = object.getBoolean("noManPages");
        if (noManPages != null) {
            this.noManPages = noManPages;
        }
        Boolean noHeaderFiles = object.getBoolean("noHeaderFiles");
        if (noHeaderFiles != null) {
            this.noHeaderFiles = noHeaderFiles;
        }
        Boolean stripDebug = object.getBoolean("stripDebug");
        if (stripDebug != null) {
            this.stripDebug = stripDebug;
        }
        Boolean stripJavaDebugAttributes = object.getBoolean("stripJavaDebugAttributes");
        if (stripJavaDebugAttributes != null) {
            this.stripJavaDebugAttributes = stripJavaDebugAttributes;
        }
        Integer compress = object.getInteger("compress");
        if (compress != null) {
            this.compress = compress;
        }
        String vm = object.getString("vm");
        if (vm != null) {
            this.vm = vm;
        }
        String output = object.getString("output");
        if (output != null) {
            this.output = output;
        }
    }

    @Override
    public JLinkConfig clone() {
        JLinkConfig config = new JLinkConfig();
        config.vm = this.vm;
        config.output = this.output;
        config.enable = this.enable;
        config.verbose = this.verbose;
        config.compress = this.compress;
        config.addModules = this.addModules;
        config.stripDebug = this.stripDebug;
        config.noManPages = this.noManPages;
        config.excludeFiles = this.excludeFiles;
        config.noHeaderFiles = this.noHeaderFiles;
        config.stripJavaDebugAttributes = this.stripJavaDebugAttributes;
        return config;
    }

    @Override
    public JLinkConfig cross(Object o) {
        JLinkConfig config1 = this.clone();
        if (o instanceof JLinkConfig config) {
            config1.enable = config.enable;
            config1.verbose = config.verbose;
            config1.stripDebug = config.stripDebug;
            config1.noManPages = config.noManPages;
            config1.noHeaderFiles = config.noHeaderFiles;
            config1.stripJavaDebugAttributes = config.stripJavaDebugAttributes;
            if (config.vm != null) {
                config1.vm = config.vm;
            }
            if (config.addModules != null) {
                if (config1.addModules == null) {
                    config1.addModules = config.addModules;
                } else {
                    config1.addModules.addAll(config.addModules);
                }
            }
            if (config.excludeFiles != null) {
                if (config1.excludeFiles == null) {
                    config1.excludeFiles = config.excludeFiles;
                } else {
                    config1.excludeFiles.addAll(config.excludeFiles);
                }
            }
            if (config.compress != null) {
                config1.compress = config.compress;
            }
            if (config.output != null) {
                config1.output = config.output;
            }
        }
        return config1;
    }
}
