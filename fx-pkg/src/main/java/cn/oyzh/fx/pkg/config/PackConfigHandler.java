package cn.oyzh.fx.pkg.config;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 打包信息处理器
 *
 * @author oyzh
 * @since 2024/6/18
 */
public class PackConfigHandler implements PreHandler {

    @Getter
    @Setter
    @Accessors(chain = false, fluent = true)
    private int order = PackOrder.ORDER_P8;

    @Override
    public String name() {
        return "打包信息处理";
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        if (StrUtil.equals(packConfig.getJdkPath(), "$SYSTEM")) {
            String javaHome = System.getenv("JAVA_HOME");
            packConfig.setJdkPath(javaHome);
        }
        String appVersion = packConfig.appVersion();
        if (appVersion != null) {
            packConfig.setDest(StrUtil.replace(packConfig.getDest(), "${appVersion}", appVersion));
            packConfig.setMainJar(StrUtil.replace(packConfig.getMainJar(), "${appVersion}", appVersion));
        }
        String projectPath = (String) packConfig.getProperty("projectPath");
        if (projectPath != null) {
            packConfig.setMainJar(StrUtil.replace(packConfig.getMainJar(), "${projectPath}", projectPath));
            packConfig.setAppIcon(StrUtil.replace(packConfig.getAppIcon(), "${projectPath}", projectPath));
        }
    }
}
