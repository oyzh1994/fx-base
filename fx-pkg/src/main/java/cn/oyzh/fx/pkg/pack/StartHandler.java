package cn.oyzh.fx.pkg.pack;

import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PostHandler;
import cn.oyzh.fx.pkg.config.ExtPackrConfig;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public class StartHandler implements PostHandler {

    @Override
    public boolean unique() {
        return true;
    }

    @Override
    public void handle(ExtPackrConfig packrConfig) throws Exception {
        packrConfig.putProperty("startTime", System.currentTimeMillis());
    }

    @Override
    public int order() {
        return PackOrder.HIGH_MAX;
    }

    @Override
    public String name() {
        return "起始处理";
    }
}
