package cn.oyzh.fx.pkg;

/**
 * 后置处理器
 *
 * @author oyzh
 * @since 2024/6/14
 */
public interface PostHandler extends Handler {


    @Override
    default int order() {
        return PackOrder.LOW;
    }
}
