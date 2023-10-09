package cn.oyzh.fx.pkg.jpackage;

import cn.hutool.core.io.FileUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * jpackage处理
 *
 * @author oyzh
 * @since 2023/3/8
 */
@Slf4j
public class JPackageHandler {

    /**
     * 执行jpackage
     */
    public void exec(@NonNull JPackageConfig config) throws InterruptedException, IOException {
        FileUtil.del(config.getDest());
        String cmdStr = "jpackage";
        if (config.isVerbose()) {
            cmdStr += " --verbose";
        }
        if (config.getVendor() != null) {
            cmdStr += " --vendor " + config.getVendor();
        }
        if (config.getDescription() != null) {
            cmdStr += " --description " + config.getDescription();
        }
        if (config.getIcon() != null) {
            cmdStr += " --icon " + config.getIcon();
        }
        if (config.getInput() != null) {
            cmdStr += " -i " + config.getInput();
        }
        if (config.getMainJar() != null) {
            cmdStr += " --main-jar " + config.getMainJar();
        }
        if (config.getName() != null) {
            cmdStr += " -n " + config.getName();
        }
        if (config.getType() != null) {
            cmdStr += " -t " + config.getType();
        }
        if (config.getAppVersion() != null) {
            cmdStr += " --app-version " + config.getAppVersion();
        }
        if (config.getRuntimeImage() != null) {
            cmdStr += " --runtime-image " + config.getRuntimeImage();
        }
        cmdStr += " -d " + config.getDest();
        // 执行jpackage
        log.info("jpackage exec start, command:{}.", cmdStr);
        Process process = Runtime.getRuntime().exec(cmdStr, null, null);
        process.waitFor();
        log.info("jpackage finish.");
    }
}
