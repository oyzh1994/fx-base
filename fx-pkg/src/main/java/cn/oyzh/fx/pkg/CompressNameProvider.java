package cn.oyzh.fx.pkg;

import cn.oyzh.fx.pkg.packager.PackageConfig;

/**
 * @author oyzh
 * @since 2024/4/2
 */
public interface CompressNameProvider {

    String getCompressName(PackageConfig packageConfig);

}
