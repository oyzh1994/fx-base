package cn.oyzh.fx.pkg.jlink;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.oyzh.fx.common.util.RuntimeUtil;
import cn.oyzh.fx.pkg.ConfigParser;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;
import cn.oyzh.fx.pkg.config.ExtPackrConfig;
import cn.oyzh.fx.pkg.util.PkgUtil;
import lombok.NonNull;

/**
 * jlink处理
 *
 * @author oyzh
 * @since 2023/3/8
 */
public class JLinkHandler implements PreHandler {

    private final JLinkConfig config;

    public JLinkHandler(String configFile) {
        this.config = JLinkConfigParser.parseConfig(configFile);
    }

    @Override
    public boolean unique() {
        return true;
    }

    @Override
    public void handle(ExtPackrConfig packrConfig) throws Exception {
        String jdk = packrConfig.jdk;
        if (StrUtil.isBlank(jdk)) {
            throw new Exception("jdk为空！");
        }
        if (FileUtil.exist(this.config.getOutput())) {
            FileUtil.del(this.config.getOutput());
        }
        String cmdStr = PkgUtil.getJLinkCMD(this.config);
        cmdStr = PkgUtil.getJDKExecCMD(jdk, cmdStr);
        // 执行jlink
        RuntimeUtil.execAndWait(cmdStr);
        // 更新jre路径
        packrConfig.setJlinkJre(this.config.getOutput());
    }

    @Override
    public String name() {
        return "jlink处理";
    }
}
