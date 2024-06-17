package cn.oyzh.fx.pkg.pack;

import cn.hutool.core.io.FileUtil;
import cn.oyzh.fx.pkg.Handler;
import cn.oyzh.fx.pkg.config.ExtPackrConfig;
import cn.oyzh.fx.pkg.packr.Packr;

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
        if (packrConfig.getMinimizeJre() != null) {
            packrConfig.jdk = packrConfig.getMinimizeJre();
        } else if (packrConfig.getJlinkJre() != null) {
            packrConfig.jdk = packrConfig.getJlinkJre();
        }
        if (packrConfig.classpath == null) {
            packrConfig.classpath = new ArrayList<>();
        }
        if (packrConfig.getMinimizeManJar() != null) {
            packrConfig.classpath.remove(packrConfig.getMainJar());
            packrConfig.classpath.add(packrConfig.getMinimizeManJar());
        } else {
            packrConfig.classpath.add(packrConfig.getMainJar());
        }
        packrConfig.jrePath = "jre";
        if (FileUtil.exist(packrConfig.outDir)) {
            FileUtil.del(packrConfig.outDir);
        }
        packr.pack(packrConfig);
    }
}
