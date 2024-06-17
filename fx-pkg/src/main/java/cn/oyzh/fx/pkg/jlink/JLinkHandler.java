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
public class JLinkHandler implements PreHandler, ConfigParser<JLinkConfig> {

    private final JLinkConfig config;

    public JLinkHandler(String configFile) {
        this.config = this.parse(configFile);
    }

    /**
     * 执行jlink
     *
     * @param config  配置
     * @param jdkPath jdk路径
     */
    public void exec(@NonNull JLinkConfig config, String jdkPath) throws Exception {
        String cmdStr = PkgUtil.getJLinkCMD(config);
        cmdStr = PkgUtil.getJDKExecCMD(jdkPath, cmdStr);
        // 执行jlink
        RuntimeUtil.execAndWait(cmdStr);
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
    public JLinkConfig parse(String configFile) {
        JSONObject object = JSONUtil.parseObj(FileUtil.readUtf8String(configFile));
        JLinkConfig config1 = new JLinkConfig();
        config1.parseConfig(object);
        return config1;
    }

    @Override
    public int order() {
        return PackOrder.LOW;
    }
}
