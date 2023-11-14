package cn.oyzh.fx.pkg.jlink;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.oyzh.fx.common.util.RuntimeUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * jlink处理
 *
 * @author oyzh
 * @since 2023/3/8
 */
@Slf4j
public class JLinkHandler {

    /**
     * 执行jlink
     */
    public void exec(@NonNull JLinkConfig config) throws Exception {
        FileUtil.del(config.getOutput());
        String cmdStr = "jlink";
        if (config.isVerbose()) {
            cmdStr += " --verbose";
        }
        if (config.getVm() != null) {
            cmdStr += " --vm=" + config.getVm();
        }
        if (config.getCompress() != null) {
            cmdStr += " --compress=" + config.getCompress();
        }
        if (config.isNoHeaderFiles()) {
            cmdStr += " --no-header-files";
        }
        if (config.isNoManPages()) {
            cmdStr += " --no-man-pages";
        }
        if (config.isStripDebug()) {
            cmdStr += " --strip-debug";
        }
        if (config.isStripJavaDebugAttributes()) {
            cmdStr += " --strip-java-debug-attributes";
        }
        if (CollUtil.isNotEmpty(config.getAddModules())) {
            cmdStr += " --add-modules " + CollUtil.join(config.getAddModules(), ",");
        }
        if (CollUtil.isNotEmpty(config.getExcludeFiles())) {
            cmdStr += " --exclude-files=" + CollUtil.join(config.getExcludeFiles(), ",");
        }
        cmdStr += " --output " + config.getOutput();
        // 执行jlink
        // log.info("jlink exec start, command:{}.", cmdStr);
        // Process process = Runtime.getRuntime().exec(cmdStr, null, null);
        // process.waitFor();
        RuntimeUtil.execAndWait(cmdStr);
        // log.info("jlink finish.");
    }
}
