package cn.oyzh.fx.plus.tray;

/**
 * 托盘菜单项
 *
 * @author oyzh
 * @since 2025/08/19
 */
public interface BaseTrayItem {

    /**
     * 转换为DorkboxTrayItem
     *
     * @return DorkboxTrayItem
     */
    DorkboxTrayItem toDorkboxTrayItem();

}
