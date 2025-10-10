package cn.oyzh.fx.pkg.jlink;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.common.thread.ProcessExecResult;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;
import cn.oyzh.fx.pkg.SingleHandler;
import cn.oyzh.fx.pkg.config.PackConfig;
import cn.oyzh.fx.pkg.util.PkgUtil;

/**
 * jlink处理
 *
 * @author oyzh
 * @since 2023/3/8
 */
public class JLinkHandler implements PreHandler, SingleHandler {

    private int order = PackOrder.ORDER_P5;

    public int order() {
        return order;
    }

    public void order(int order) {
        this.order = order;
    }

    private boolean executed;

    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    @Override
    public boolean unique() {
        return true;
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        if (this.executed) {
            return;
        }
        JLinkConfig jLinkConfig = packConfig.getjLinkConfig();
        if (jLinkConfig == null) {
            return;
        }
        String jdkPath = packConfig.getJdkPath();
        if (StringUtil.isBlank(jdkPath)) {
            throw new Exception("jdkPath为空！");
        }
        if (FileUtil.exist(jLinkConfig.getOutput())) {
            FileUtil.del(jLinkConfig.getOutput());
        }
        String[] cmd = PkgUtil.getJLinkCMD1(jLinkConfig);
        cmd = PkgUtil.getJDKExecCMD(jdkPath, cmd);
        String cmdStr = StringUtil.join(" ", cmd);
        JulLog.info("JLink cmd:{}", "\n" + cmdStr);
        // 执行jlink
//        RuntimeUtil.execAndWait(cmdStr);
//        ProcessExecBuilder builder = ProcessExecBuilder.newBuilder(cmdStr);
//        builder.env("MAVEN_OPTS", "-Dfile.encoding=UTF-8");
//        builder.env("JAVA_OPTS", "-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8");
//        builder.directory(jdkPath + "/bin");
//        builder.timeout(30_000);
        ProcessExecResult result = RuntimeUtil.execForResult(cmd);
        JulLog.info("JLink result:{}", result);
        if (!result.isSuccess()) {
            JulLog.error("JLink error:{}", result.getError());
            throw new Exception("JLink error:" + result.getError());
        }
        // 更新jre路径
        packConfig.setJlinkJre(jLinkConfig.getOutput());
        this.executed = true;
    }

    @Override
    public String name() {
        return "jlink处理";
    }
}
