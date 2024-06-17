package cn.oyzh.fx.pkg;

import cn.oyzh.fx.pkg.config.ExtPackrConfig;
import cn.oyzh.fx.pkg.packr.PackrConfig;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public interface Handler {

    default int order() {
        return PackOrder.MID;
    }

    void handle(ExtPackrConfig packrConfig) throws Exception;

}
