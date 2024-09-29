package cn.oyzh.fx.pkg.comporess;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.util.StringUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PostHandler;
import cn.oyzh.fx.pkg.config.PackConfig;
import cn.oyzh.fx.pkg.util.PkgUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;

/**
 * 压缩处理器
 *
 * @author oyzh
 * @since 2024/4/2
 */
public class CompressHandler implements PostHandler {

    @Getter
    @Setter
    @Accessors(chain = false, fluent = true)
    private int order = PackOrder.ORDER_M6;

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        CompressConfig compressConfig = packConfig.getCompressConfig();
        if (compressConfig == null) {
            return;
        }
        if (StringUtil.isNotBlank(compressConfig.getType())) {
            String compressName = compressConfig.getName();
            if (StringUtil.isBlank(compressName)) {
                throw new Exception("compressName为空！");
            }
            String dest = packConfig.getDest();
            File compressFile = switch (compressConfig.getType().toLowerCase()) {
                case "zip" -> {
                    if (packConfig.isPlatformMacos()) {
                        yield PkgUtil.zipDestByMacos(compressName, dest);
                    } else {
                        yield PkgUtil.zipDest(compressName, dest);
                    }
                }
                case "tar" -> PkgUtil.tarDest(compressName, dest);
                case "tar.gz" -> PkgUtil.gzipDest(compressName, dest);
                default ->
                        throw new IllegalStateException("Unexpected value: " + compressConfig.getType().toLowerCase());
            };
            packConfig.setCompressFile(compressFile);
        }
    }

    @Override
    public String name() {
        return "压缩处理";
    }
}
