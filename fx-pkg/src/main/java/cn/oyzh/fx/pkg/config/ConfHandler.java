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
    public String name() {
        return "配置处理";
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        if (packConfig.isParkByPackr() && packConfig.isPlatformWindows()) {
            if (StrUtil.isBlank(packConfig.mainJar())) {
                throw new Exception("mainJar参数缺失！");
            }
            String cmdText = packConfig.jrePath() + "/bin/javaw.exe -jar " + packConfig.mainJarName();
            File configFile = new File(packConfig.getDest(), "app.conf");
            FileUtil.writeUtf8String(cmdText, configFile);
        }
    }
}
