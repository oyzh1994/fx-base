package cn.oyzh.fx.pkg.packager;

import cn.hutool.core.io.FileUtil;
import cn.oyzh.fx.common.util.OSUtil;
import cn.oyzh.fx.pkg.packr.PackrConfig;

/**
 * mac打包器
 *
 * @author oyzh
 * @since 2023/3/8
 */
public class MacPackager extends BasePackager {

    @Override
    protected void packBefore() throws Exception {
        if (!OSUtil.isMacOS()) {
            throw new UnsupportedOperationException("current os is:" + OSUtil.getOSType() + ", cat not jlink macos jre!");
        }
        super.packBefore();
        FileUtil.del(this.packageConfig().getDestPath());
    }

    @Override
    public void pack() throws Exception {
        super.packBefore();
        super.packByPackr();
        super.packAfter();
    }

    @Override
    protected PackrConfig.Platform getPlatform() {
        return PackrConfig.Platform.MacOS;
    }
}
