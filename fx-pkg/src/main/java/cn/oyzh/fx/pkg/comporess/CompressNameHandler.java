package cn.oyzh.fx.pkg.comporess;

import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PostHandler;
import cn.oyzh.fx.pkg.config.ExtPackrConfig;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author oyzh
 * @since 2024/4/2
 */
public class CompressNameHandler implements PostHandler {

    @Override
    public void handle(ExtPackrConfig packrConfig) throws Exception {
        String compressName = "";
        if (packrConfig.executable != null) {
            compressName += packrConfig.executable;
        }
        if (packrConfig.getVersion() != null) {
            compressName = compressName + "_" + packrConfig.getVersion();
        }
        if (packrConfig.platform != null) {
            compressName = compressName + "_" + packrConfig.platform.desc;
        }
        compressName += "_" + new SimpleDateFormat("yyyyMMdd").format(new Date());
        if (packrConfig.getBuildType() != null) {
            compressName = compressName + "_" + packrConfig.getBuildType();
        } else {
            compressName = compressName + "_release";
        }
        packrConfig.setCompressName(compressName);
    }

    @Override
    public int order() {
        return PackOrder.LOW -1;
    }

    @Override
    public String name() {
        return "压缩文件名处理";
    }
}
