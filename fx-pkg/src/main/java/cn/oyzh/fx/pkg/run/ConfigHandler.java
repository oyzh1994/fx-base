package cn.oyzh.fx.pkg.run;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.pkg.ExtConfig;
import cn.oyzh.fx.pkg.PostHandler;
import cn.oyzh.fx.pkg.packr.PackrConfig;

import java.io.File;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public class ConfigHandler implements PostHandler {

    @Override
    public void handle(PackrConfig packrConfig, ExtConfig extConfig) throws Exception {
        if (StrUtil.isBlank(packrConfig.getMainJar())) {
            throw new Exception("mainJar 参数缺失！");
        }
        if (packrConfig.platform == PackrConfig.Platform.Win_64) {
            String cmdText = packrConfig.jrePath + "/bin/javaw.exe -jar " + packrConfig.getMainJar();
            File configFile = new File(packrConfig.outDir,  "app.conf");
            FileUtil.writeUtf8String(cmdText, configFile);
        }
    }
}
