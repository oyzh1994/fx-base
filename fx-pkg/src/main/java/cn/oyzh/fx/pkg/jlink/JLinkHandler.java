package cn.oyzh.fx.pkg.jlink;

import cn.hutool.core.io.FileUtil;
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
     */
    public void exec(@NonNull JLinkConfig config) throws Exception {
        FileUtil.del(config.getOutput());
        String cmdStr = PkgUtil.getJLinkCMD(config);
        // 执行jlink
        RuntimeUtil.execAndWait(cmdStr);
    }
}
