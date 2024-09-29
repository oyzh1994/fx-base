package cn.oyzh.fx.pkg.jpackage;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.util.RuntimeUtil;
import cn.oyzh.fx.common.util.StringUtil;
import cn.oyzh.fx.pkg.PackHandler;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.config.PackConfig;
import cn.oyzh.fx.pkg.util.PkgUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;

/**
 * jpackage处理
 *
 * @author oyzh
 * @since 2023/3/8
 */
public class JPackageHandler implements PackHandler {

    @Getter
    @Setter
    @Accessors(chain = false, fluent = true)
    private int order = PackOrder.ORDER_0;

    @Override
    public String name() {
        return "jpackage打包处理";
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        JPackageConfig jPackageConfig = packConfig.getJPackageConfig();
        if (jPackageConfig == null) {
            return;
        }
        String jdkPath = packConfig.getJdkPath();
        if (StringUtil.isBlank(jdkPath)) {
            throw new Exception("jdkPath为空！");
        }
        if (jPackageConfig.getInput() == null) {
            File dir = new File(FileUtil.getTmpDir(), "_temp_jpackage_input_" + UUID.fastUUID().toString(true));
            FileUtil.mkdir(dir);
            File target = new File(dir, packConfig.mainJarName());
            FileUtil.copyFile(packConfig.mainJar(), target.getPath());
            jPackageConfig.setInput(dir.getPath());
            packConfig.setJpackageInput(jPackageConfig.getInput());
        }
        if (jPackageConfig.getMainJar() == null) {
            jPackageConfig.setMainJar(packConfig.mainJarName());
        }
        if (jPackageConfig.getDest() == null) {
            jPackageConfig.setDest(packConfig.getDest());
        }
        if (jPackageConfig.getIcon() == null) {
            jPackageConfig.setIcon(packConfig.getAppIcon());
        }
        if (jPackageConfig.getName() == null) {
            jPackageConfig.setName(packConfig.getAppName() + "_v" + packConfig.appVersion());
        }
        if (jPackageConfig.getAppVersion() == null) {
            jPackageConfig.setAppVersion(packConfig.appVersion());
        }
        if (jPackageConfig.getRuntimeImage() == null) {
            jPackageConfig.setRuntimeImage(packConfig.jrePath());
        }
        // 删除输出目录
        if (FileUtil.exist(packConfig.getDest())) {
            FileUtil.del(packConfig.getDest());
        }
        String cmdStr = PkgUtil.getJPackageCMD(jPackageConfig);
        cmdStr = PkgUtil.getJDKExecCMD(jdkPath, cmdStr);
        // 执行jpackage
        RuntimeUtil.execAndWait(cmdStr);
    }
}
