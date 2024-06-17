package cn.oyzh.fx.pkg.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PostHandler;
import cn.oyzh.fx.pkg.packr.Platform;

import java.io.File;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public class ConfHandler implements PostHandler {

    @Override
    public String name() {
        return "配置处理";
    }

    @Override
    public void handle(ExtPackrConfig packrConfig) throws Exception {
        if (packrConfig.platform == Platform.Win_64) {
            if (StrUtil.isBlank(packrConfig.mainJar())) {
                throw new Exception("mainJar参数缺失！");
            }
            String cmdText = packrConfig.jrePath + "/bin/javaw.exe -jar " + packrConfig.mainJarName();
            File configFile = new File(packrConfig.outDir, "app.conf");
            FileUtil.writeUtf8String(cmdText, configFile);
        }
    }
}
