package cn.oyzh.fx.pkg.jlink;

import cn.oyzh.fx.common.util.RuntimeUtil;
import cn.oyzh.fx.pkg.util.PkgUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

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
}
