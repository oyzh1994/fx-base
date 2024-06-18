package cn.oyzh.fx.pkg.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PostHandler;

import java.io.File;

/**
 * 配置处理器
 *
 * @author oyzh
 * @since 2024/6/14
 */
public class AppConfigHandler implements PostHandler {

    @Override
    public int order() {
        return PackOrder.LOW_M2;
    }

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
