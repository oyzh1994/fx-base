package cn.oyzh.fx.pkg.jlink;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.pkg.ConfigParser;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.HashSet;
import java.util.List;

/**
 * jlink配置解析器
 *
 * @author oyzh
 * @since 2024/6/17
 */
public class JLinkConfigParser implements ConfigParser<JLinkConfig> {

    @Override
    public JLinkConfig parse(JSONObject object) {
        JLinkConfig config = new JLinkConfig();
        List<String> excludeFiles = JSONUtil.toList(object, "exclude-files", String.class);
        if (excludeFiles != null) {
            config.setExcludeFiles(new HashSet<>(excludeFiles));
        }
        List<String> addModules = JSONUtil.toList(object, "add-modules", String.class);
        if (addModules != null) {
            config.setAddModules(new HashSet<>(addModules));
        }
        String vm = object.getString("vm");
        if (vm != null) {
            config.setVm(vm);
        }
        // 临时jre目录
        String tmpJreDir = new File(FileUtil.getTmpDir(), "_temp_jre_" + UUID.fastUUID().toString(true)).getPath();
        if (object.containsKey("output")) {
            config.setOutput(StringUtil.emptyToDefault(object.getString("output"), tmpJreDir));
        } else {
            config.setOutput(tmpJreDir);
        }
        String compress = object.getString("compress");
        if (compress != null) {
            config.setCompress(compress);
        }
        Boolean verbose = object.getBoolean("verbose");
        if (verbose != null) {
            config.setVerbose(verbose);
        }
        Boolean enable = object.getBoolean("enable");
        if (enable != null) {
            config.setEnable(enable);
        }
        Boolean noManPages = object.getBoolean("no-man-pages");
        if (noManPages != null) {
            config.setNoManPages(noManPages);
        }
        Boolean stripDebug = object.getBoolean("strip-debug");
        if (stripDebug != null) {
            config.setStripDebug(stripDebug);
        }
        Boolean noHeaderFiles = object.getBoolean("no-header-files");
        if (noHeaderFiles != null) {
            config.setNoHeaderFiles(noHeaderFiles);
        }
        Boolean ignoreSigningInformation = object.getBoolean("ignore-signing-information");
        if (ignoreSigningInformation != null) {
            config.setIgnoreSigningInformation(ignoreSigningInformation);
        }
        Boolean stripJavaDebugAttributes = object.getBoolean("strip-java-debug-attributes");
        if (stripJavaDebugAttributes != null) {
            config.setStripJavaDebugAttributes(stripJavaDebugAttributes);
        }
        return config;
    }

    public static JLinkConfig parseConfig(JSONObject object) {
        return new JLinkConfigParser().parse(object);
    }

    public static JLinkConfig parseConfig(String configFile) {
        return new JLinkConfigParser().parse(configFile);
    }
}
