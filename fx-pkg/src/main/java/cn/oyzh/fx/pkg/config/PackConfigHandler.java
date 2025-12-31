package cn.oyzh.fx.pkg.config;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.pkg.PackCost;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;

import java.io.File;
import java.util.List;

/**
 * 打包信息处理器
 *
 * @author oyzh
 * @since 2024/6/18
 */
public class PackConfigHandler implements PreHandler {

    private int order = PackOrder.ORDER_P8;

    @Override
    public int order() {
        return order;
    }

    @Override
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
        String pkgPath = (String) packConfig.getProperty(PackCost.PKG_PATH);
        if (pkgPath != null) {
            packConfig.setAppImageRuntime(StringUtil.replace(packConfig.getAppImageRuntime(), "${" + PackCost.PKG_PATH + "}", pkgPath));
        }
        String projectPath = (String) packConfig.getProperty(PackCost.PROJECT_PATH);
        if (projectPath != null) {
            packConfig.setMainJar(StringUtil.replace(packConfig.getMainJar(), "${" + PackCost.PROJECT_PATH + "}", projectPath));
            packConfig.setAppIcon(StringUtil.replace(packConfig.getAppIcon(), "${" + PackCost.PROJECT_PATH + "}", projectPath));
            packConfig.setAppImageRuntime(StringUtil.replace(packConfig.getAppImageRuntime(), "${" + PackCost.PROJECT_PATH + "}", projectPath));
        }
        // 清除打包目录
        List<File> files = FileUtil.getAllFiles(packConfig.getDest());
        for (File file : files) {
            if (!file.exists()) {
                continue;
            }
            if (file.isFile()) {
                FileUtil.del(file);
            } else {
                FileUtil.cleanDir(file);
            }
        }
    }
}
