package cn.oyzh.fx.pkg;

import cn.oyzh.fx.pkg.packr.PackrConfig;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public interface Handler {

    default int order() {
        return 0;
    }

    void handle(PackrConfig packrConfig, ExtConfig extConfig) throws Exception;

}
