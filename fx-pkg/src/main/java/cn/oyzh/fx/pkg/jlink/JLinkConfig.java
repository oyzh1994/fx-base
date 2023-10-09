package cn.oyzh.fx.pkg.jlink;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2023/3/8
 */
@Data
public class JLinkConfig {

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
    private List<String> addModules = new ArrayList<>();

    /**
     * 排除文件
     */
    private List<String> excludeFiles = new ArrayList<>();

    /**
     * 解析配置
     *
     * @param configPath 配置路径
     */
    public void parseConfig(String configPath) {
        String text = FileUtil.readUtf8String(configPath);
        JSONObject object = JSONObject.parseObject(text);
        this.fromConfig(object);
    }

    /**
     * 从配置对象生成
     *
     * @param object 配置对象
     */
    protected void fromConfig(JSONObject object) {
        this.output = object.getString("output");
        if (object.containsKey("vm")) {
            this.vm = object.getString("vm");
        }
        if (object.containsKey("compress")) {
            this.compress = object.getIntValue("compress");
        }
        if (object.containsKey("verbose")) {
            this.verbose = object.getBooleanValue("verbose");
        }
        if (object.containsKey("stripDebug")) {
            this.stripDebug = object.getBooleanValue("stripDebug");
        }
        if (object.containsKey("noManPages")) {
            this.noManPages = object.getBooleanValue("noManPages");
        }
        if (object.containsKey("noHeaderFiles")) {
            this.noHeaderFiles = object.getBooleanValue("noHeaderFiles");
        }
        if (object.containsKey("stripJavaDebugAttributes")) {
            this.stripJavaDebugAttributes = object.getBooleanValue("stripJavaDebugAttributes");
        }
        JSONArray addModules = object.getJSONArray("addModules");
        if (CollUtil.isNotEmpty(addModules)) {
            for (Object addModule : addModules) {
                this.addModules.add((String) addModule);
            }
        }
        JSONArray excludeFiles = object.getJSONArray("excludeFiles");
        if (CollUtil.isNotEmpty(excludeFiles)) {
            for (Object excludeFile : excludeFiles) {
                this.excludeFiles.add((String) excludeFile);
            }
        }
    }

    /**
     * 从配置文件生成配置对象
     *
     * @param configPath 配置文件路径
     * @return 配置对象
     */
    public static JLinkConfig fromConfig(String configPath) {
        JLinkConfig config = new JLinkConfig();
        config.parseConfig(configPath);
        return config;
    }
}
