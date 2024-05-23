package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class BatchOperationMenuItem extends BatchOptMenuItem {

    public BatchOperationMenuItem(String iconSize, Runnable action) {
        super(I18nHelper.batchOpt(), null, iconSize, action);
    }
}
