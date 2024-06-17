package cn.oyzh.fx.pkg.comporess;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PostHandler;
import cn.oyzh.fx.pkg.config.ExtPackrConfig;
import cn.oyzh.fx.pkg.packr.Platform;
import cn.oyzh.fx.pkg.util.PkgUtil;

import java.io.File;

/**
 * @author oyzh
 * @since 2024/4/2
 */
public class CompressHandler implements PostHandler {

    @Override
    public void handle(ExtPackrConfig packrConfig) throws Exception {
        if (StrUtil.isNotBlank(packrConfig.getCompressType())) {
            String compressName = packrConfig.getCompressName();
            if (StrUtil.isBlank(compressName)) {
                throw new Exception("compressName为空！");
            }
            String dest = packrConfig.outDir.getPath();
            File compressFile= switch (packrConfig.getCompressType().toLowerCase()) {
                case "zip" -> {
                    if (packrConfig.platform == Platform.MacOS) {
                        yield PkgUtil.zipDestByMacos(compressName, dest);
                    } else {
                        yield PkgUtil.zipDest(compressName, dest);
                    }
                }
                case "tar" ->  PkgUtil.tarDest(compressName, dest);
                case "tar.gz" -> PkgUtil.gzipDest(compressName, dest);
                default ->
                        throw new IllegalStateException("Unexpected value: " + packrConfig.getCompressType().toLowerCase());
            };
            packrConfig.setCompressFile(compressFile);
        }
    }

    @Override
    public int order() {
        return PackOrder.HIGH - 1;
    }
}
