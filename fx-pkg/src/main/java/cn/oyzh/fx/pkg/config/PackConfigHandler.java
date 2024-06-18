package cn.oyzh.fx.pkg.config;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;

/**
 * 打包信息处理器
 *
 * @author oyzh
 * @since 2024/6/18
 */
public class PackConfigHandler implements PreHandler {

    @Override
    public int order() {
        return PackOrder.HIGH_P3;
    }

    @Override
    public String name() {
        return "打包信息处理";
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        if (packConfig.getAppVersion() == null) {
            return;
        }
        String appVersion = packConfig.appVersion();
        if (StrUtil.contains(packConfig.getDest(), "${appVersion}")) {
            packConfig.setDest(StrUtil.replace(packConfig.getDest(), "${appVersion}", appVersion));
        }
        if (StrUtil.contains(packConfig.getMainJar(), "${appVersion}")) {
            packConfig.setMainJar(StrUtil.replace(packConfig.getMainJar(), "${appVersion}", appVersion));
        }
    }
}
