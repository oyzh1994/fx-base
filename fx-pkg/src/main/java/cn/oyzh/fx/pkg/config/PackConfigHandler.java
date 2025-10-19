package cn.oyzh.fx.pkg.config;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.pkg.PackCost;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;

/**
 * 打包信息处理器
 *
 * @author oyzh
 * @since 2024/6/18
 */
public class PackConfigHandler implements PreHandler {

    private int order = PackOrder.ORDER_P8;

    public int order() {
        return order;
    }

    public void order(int order) {
        this.order = order;
    }

    @Override
    public String name() {
        return "打包信息处理";
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        if (StringUtil.equals(packConfig.getJdkPath(), "$SYSTEM")) {
            String javaHome = System.getenv("JAVA_HOME");
            if (StringUtil.isBlank(javaHome)) {
                JulLog.warn("从环境变量获取JAVA_HOME失败，改用系统属性获取");
                javaHome = System.getProperty("java.home");
                if (StringUtil.isBlank(javaHome)) {
                    JulLog.warn("从系统属性获取获取java.home失败");
                }
            }
            packConfig.setJdkPath(javaHome);
        }
        String appVersion = packConfig.mainAppVersion();
        if (appVersion != null) {
            packConfig.setDest(StringUtil.replace(packConfig.getDest(), "${appVersion}", appVersion));
            packConfig.setMainJar(StringUtil.replace(packConfig.getMainJar(), "${appVersion}", appVersion));
        }
        String projectPath = (String) packConfig.getProperty(PackCost.PROJECT_PATH);
        if (projectPath != null) {
            packConfig.setMainJar(StringUtil.replace(packConfig.getMainJar(), "${projectPath}", projectPath));
            packConfig.setAppIcon(StringUtil.replace(packConfig.getAppIcon(), "${projectPath}", projectPath));
            packConfig.setAppImageRuntime(StringUtil.replace(packConfig.getAppImageRuntime(), "${projectPath}", projectPath));
        }
    }
}
