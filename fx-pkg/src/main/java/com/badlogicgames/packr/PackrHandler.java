package com.badlogicgames.packr;

import cn.hutool.core.io.FileUtil;
import cn.oyzh.fx.pkg.PackHandler;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.config.PackConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.ArrayList;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public class PackrHandler implements PackHandler {

    @Getter
    @Setter
    @Accessors(chain = false, fluent = true)
    private int order = PackOrder.ORDER_0;

    private final Packr packr = new Packr();

    @Override
    public String name() {
        return "打包处理";
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        PackrConfig packrConfig = packConfig.getPackrConfig();
        // 修正executable
        if (packrConfig.executable == null) {
            packrConfig.executable = packConfig.getAppName();
        }
        // 修正jd
        if (packrConfig.jdk == null) {
            packrConfig.jdk = packConfig.jrePath();
        }
        // 修正outDir
        if (packrConfig.outDir == null) {
            packrConfig.outDir = new File(packConfig.getDest());
        }
        // 修正icon
        if (packrConfig.iconResource == null) {
            packrConfig.iconResource = new File(packConfig.getAppIcon());
        }
        // 修正platform
        if (packrConfig.platform == null) {
            packrConfig.platform = Platform.byDesc(packConfig.getPlatform());
        }
        // 处理主程序
        if (packrConfig.classpath == null) {
            packrConfig.classpath = new ArrayList<>();
        }
        if (packConfig.getMinimizeManJar() != null) {
            packrConfig.classpath.remove(packConfig.getMinimizeManJar());
            packrConfig.classpath.add(packConfig.mainJar());
        } else {
            packrConfig.classpath.add(packConfig.mainJar());
        }
        // 删除输出目录
        if (FileUtil.exist(packConfig.getDest())) {
            FileUtil.del(packConfig.getDest());
        }
        // 执行打包
        this.packr.pack(packrConfig);
    }
}
