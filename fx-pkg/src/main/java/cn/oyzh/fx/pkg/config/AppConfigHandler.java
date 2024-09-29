package cn.oyzh.fx.pkg.config;

import cn.hutool.core.io.FileUtil;
import cn.oyzh.fx.common.util.StringUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PostHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;

/**
 * 配置处理器
 *
 * @author oyzh
 * @since 2024/6/14
 */
public class AppConfigHandler implements PostHandler {

    @Getter
    @Setter
    @Accessors(chain = false, fluent = true)
    private int order = PackOrder.ORDER_M4;

    @Override
    public String name() {
        return "配置处理";
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        if (packConfig.isParkByPackr()) {
            if (StringUtil.isBlank(packConfig.mainJar())) {
                throw new Exception("mainJar参数缺失！");
            }
            String jre = packConfig.getPackrConfig().jrePath();
            String cmdText = null;
            if (packConfig.isPlatformWindows()) {
                cmdText = jre + "/bin/javaw.exe -jar " + packConfig.mainJarName();
            } else if (packConfig.isPlatformLinux()) {
                cmdText = "./" + jre + "/bin/java -jar " + packConfig.mainJarName();
            }
            if (cmdText != null) {
                File configFile = new File(packConfig.getDest(), "app.conf");
                FileUtil.writeUtf8String(cmdText, configFile);
            }
        }
    }
}
