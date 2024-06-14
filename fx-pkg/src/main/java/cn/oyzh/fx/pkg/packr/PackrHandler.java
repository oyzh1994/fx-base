package cn.oyzh.fx.pkg.packr;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.oyzh.fx.pkg.ConfigParser;
import cn.oyzh.fx.pkg.ExtConfig;
import cn.oyzh.fx.pkg.Handler;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public class PackrHandler implements Handler, ConfigParser<PackrConfig> {

    private final Packr packr = new Packr();

    @Getter
    private PackrConfig packrConfig;

    @Override
    public PackrConfig parse(String configFile) {
        JSONObject object = JSONUtil.parseObj(FileUtil.readUtf8String(configFile));
        this.packrConfig = new PackrConfig();
        this.packrConfig.jdk = object.getStr("jdk");
        this.packrConfig.setMainJar(object.getStr("mainJar"));
        this.packrConfig.mainClass = object.getStr("mainClass");
        this.packrConfig.executable = object.getStr("executable");
        this.packrConfig.outDir = new File(object.getStr("outDir"));
        this.packrConfig.bundleIdentifier = object.getStr("bundleIdentifier");
        this.packrConfig.iconResource = new File(object.getStr("iconResource"));
        this.packrConfig.verbose = object.getBool("verbose", false);
        this.packrConfig.platform = PackrConfig.Platform.valueOf(object.getStr("platform"));
        this.packrConfig.classpath = object.getJSONArray("classpath").toList(String.class);
        this.packrConfig.useZgcIfSupportedOs = object.getBool("useZgcIfSupportedOs", true);
        if (object.containsKey("vmArgs")) {
            this.packrConfig.vmArgs = object.getJSONArray("vmArgs").toList(String.class);
        } else {
            this.packrConfig.vmArgs = new ArrayList<>();
        }
        return this.packrConfig;
    }

    @Override
    public void handle(PackrConfig packrConfig, ExtConfig extConfig) throws Exception {
        if (extConfig.getMinimizeJre() != null) {
            packrConfig.jdk = extConfig.getMinimizeJre();
        } else if (extConfig.getJlinkJre() != null) {
            packrConfig.jdk = extConfig.getJlinkJre();
        }
        packrConfig.jrePath = "jre";
        if (FileUtil.exist(packrConfig.outDir)) {
            FileUtil.del(packrConfig.outDir);
        }
        packr.pack(packrConfig);
    }
}
