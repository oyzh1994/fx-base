package cn.oyzh.fx.pkg.pack;

import cn.hutool.core.io.FileUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PostHandler;
import cn.oyzh.fx.pkg.config.ExtPackrConfig;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public class EndHandler implements PostHandler {

    @Override
    public void handle(ExtPackrConfig packrConfig) throws Exception {
        // 删除打包目录，仅保留压缩目录
        if (packrConfig.getCompressFile() != null) {
            FileUtil.del(packrConfig.outDir);
        }
        // 删除裁剪的jre
        if (packrConfig.getMinimizeJre() != null) {
            FileUtil.del(packrConfig.getMinimizeJre());
        }
    }

    @Override
    public int order() {
        return PackOrder.LOW_MAX;
    }
}
