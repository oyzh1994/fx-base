package cn.oyzh.fx.pkg.packager;

import cn.oyzh.fx.common.util.OSUtil;
import com.badlogicgames.packr.PackrConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * linux平台打包器
 *
 * @author oyzh
 * @since 2023/3/8
 */
public class LinuxPackager extends BasePackager {

    /**
     * 打包配置
     */
    @Getter
    @Setter
    private LinuxPkgConfig pkgConfig;

    @Override
    protected void packBefore() throws Exception {
        if (!OSUtil.isLinux() && this.getJLinkHandler() != null) {
            throw new UnsupportedOperationException("current os is:" + OSUtil.getOSType() + ", cat not jlink linux jre!");
        }
        super.packBefore();
    }

    @Override
    public void pack() throws Exception {
        super.packBefore();
        if (OSUtil.isLinux()) {
            super.packByJPackage();
        } else {
            super.packByPackr();
        }
        super.packAfter();
    }

    @Override
    protected PackrConfig.Platform getPlatform() {
        return PackrConfig.Platform.Linux64;
    }
}
