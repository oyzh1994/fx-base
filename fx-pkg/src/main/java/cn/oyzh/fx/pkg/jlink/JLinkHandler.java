package cn.oyzh.fx.pkg.jlink;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.util.RuntimeUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;
import cn.oyzh.fx.pkg.SingleHandler;
import cn.oyzh.fx.pkg.config.PackConfig;
import cn.oyzh.fx.pkg.util.PkgUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * jlink处理
 *
 * @author oyzh
 * @since 2023/3/8
 */
public class JLinkHandler implements PreHandler, SingleHandler {

    @Getter
    @Setter
    @Accessors(chain = false, fluent = true)
    private int order = PackOrder.ORDER_P5;

    @Getter
    @Setter
    private boolean executed;

    @Override
    public boolean unique() {
        return true;
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        if (this.executed) {
            return;
        }
        JLinkConfig jLinkConfig = packConfig.getJLinkConfig();
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
        String cmdStr = PkgUtil.getJLinkCMD(jLinkConfig);
        cmdStr = PkgUtil.getJDKExecCMD(jdkPath, cmdStr);
        // 执行jlink
        RuntimeUtil.execAndWait(cmdStr);
        // 更新jre路径
        packConfig.setJlinkJre(jLinkConfig.getOutput());
        this.executed = true;
    }

    @Override
    public String name() {
        return "jlink处理";
    }
}
