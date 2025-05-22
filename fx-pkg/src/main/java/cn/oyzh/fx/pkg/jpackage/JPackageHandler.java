package cn.oyzh.fx.pkg.jpackage;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ProcessExecBuilder;
import cn.oyzh.common.thread.ProcessExecResult;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.pkg.PackHandler;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.config.PackConfig;
import cn.oyzh.fx.pkg.util.PkgUtil;

import java.io.File;

/**
 * jpackage处理
 *
 * @author oyzh
 * @since 2023/3/8
 */
public class JPackageHandler implements PackHandler {

    private int order = PackOrder.ORDER_0;

    public int order() {
        return order;
    }

    public void order(int order) {
        this.order = order;
    }

    @Override
    public String name() {
        return "jpackage打包处理";
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        JPackageConfig jPackageConfig = packConfig.getjPackageConfig();
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
        String name = jPackageConfig.getName();
        if (name == null) {
            jPackageConfig.setName(packConfig.getAppName());
//            jPackageConfig.setName(packConfig.getAppName() + "_v" + packConfig.appVersion());
        } else {
            name = name.replace("${appName}", packConfig.getAppName());
            name = name.replace("${os.arch}", System.getProperty("os.arch"));
            jPackageConfig.setName(name);
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
        JulLog.info("JPackage cmd:{}", "\n" + cmdStr);
        // 执行jpackage
        // RuntimeUtil.execAndWait(cmdStr);
        ProcessExecBuilder builder = ProcessExecBuilder.newBuilder(cmdStr);
        builder.timeout(30_000);
        ProcessExecResult result = builder.exec();
        JulLog.info("JPackage result:{}", result);
    }
}
