package cn.oyzh.fx.pkg.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.pkg.PostHandler;
import cn.oyzh.fx.pkg.packr.Platform;

import java.io.File;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public class ConfHandler implements PostHandler {

    @Override
    public void handle(ExtPackrConfig packrConfig) throws Exception {
        if (packrConfig.platform == Platform.Win_64) {
            if (StrUtil.isBlank(packrConfig.getMainJar())) {
                throw new Exception("mainJar 参数缺失！");
            }
            String cmdText = packrConfig.jrePath + "/bin/javaw.exe -jar " + packrConfig.getMainJar();
            File configFile = new File(packrConfig.outDir, "app.conf");
            FileUtil.writeUtf8String(cmdText, configFile);
        }
    }
}
