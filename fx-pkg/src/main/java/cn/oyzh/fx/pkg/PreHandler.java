package cn.oyzh.fx.pkg;

/**
 * 前置处理器
 *
 * @author oyzh
 * @since 2024/6/14
 */
public interface PreHandler extends Handler {

    @Override
    default int order() {
        return PackOrder.HIGH;
    }
}
