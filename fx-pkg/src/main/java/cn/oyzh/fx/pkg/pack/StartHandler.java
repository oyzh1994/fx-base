package cn.oyzh.fx.pkg.pack;

import cn.oyzh.fx.common.util.OSUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PostHandler;
import cn.oyzh.fx.pkg.config.PackConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public class StartHandler implements PostHandler {

    @Getter
    @Setter
    @Accessors(chain = false, fluent = true)
    private int order = PackOrder.ORDER_MAX;

    @Override
    public boolean unique() {
        return true;
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        packConfig.putProperty("startTime", System.currentTimeMillis());
    }

    @Override
    public String name() {
        return "起始处理";
    }
}
