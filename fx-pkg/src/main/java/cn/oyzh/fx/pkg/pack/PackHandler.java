package cn.oyzh.fx.pkg.pack;

import cn.hutool.core.io.FileUtil;
import cn.oyzh.fx.pkg.Handler;
import cn.oyzh.fx.pkg.config.ExtPackrConfig;
import cn.oyzh.fx.pkg.packr.Packr;
import cn.oyzh.fx.pkg.packr.PackrConfig;

import java.util.ArrayList;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public class PackHandler implements Handler {

    private final Packr packr = new Packr();

    @Override
    public String name() {
        return "打包处理";
    }

    @Override
    public void handle(ExtPackrConfig packrConfig) throws Exception {
        // 修正jdk路径
        if (packrConfig.getMinimizeJre() != null) {
            packrConfig.jdk = packrConfig.getMinimizeJre();
        } else if (packrConfig.getJlinkJre() != null) {
            packrConfig.jdk = packrConfig.getJlinkJre();
        }
        // 处理主程序
        if (packrConfig.classpath == null) {
            packrConfig.classpath = new ArrayList<>();
        }
        if (packrConfig.getMinimizeManJar() != null) {
            packrConfig.classpath.remove(packrConfig.getMainJar());
            packrConfig.classpath.add(packrConfig.getMinimizeManJar());
        } else {
            packrConfig.classpath.add(packrConfig.getMainJar());
        }
        // 删除输出目录
        if (FileUtil.exist(packrConfig.outDir)) {
            FileUtil.del(packrConfig.outDir);
        }
        // 执行打包
        this.packr.pack(packrConfig);
    }
}
