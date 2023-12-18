package cn.oyzh.fx.plus.controls.popup;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 * @author oyzh
 * @since 2023/3/7
 */
public class ContextMenuExt extends ContextMenu implements LayoutAdapter, ThemeAdapter {

    {
        this.setStyle("-fx-padding: 0 0 0 0;");
        this.getItems().addListener((ListChangeListener<MenuItem>) c -> this.calcWidth());
        this.changeTheme(ThemeManager.currentTheme());
    }

    public ContextMenuExt() {
        super();
    }

    public ContextMenuExt(MenuItemExt... items) {
        super();
        this.getItems().addAll(items);
    }

    /**
     * 计算菜单宽度
     */
    private void calcWidth() {
        if (CollUtil.isNotEmpty(this.getItems())) {
            double maxWidth = 0.d;
            for (MenuItem item : this.getItems()) {
                if (item instanceof MenuItemExt item1 && item1.getWidth() > maxWidth) {
                    maxWidth = item1.getWidth();
                }
            }
            // 设置宽度
            this.setWidth(maxWidth);
            this.setRealWidth(maxWidth);
        }
    }

    @Override
    public double getRealWidth() {
        return LayoutAdapter.super.realWidth();
    }

    @Override
    public void setRealWidth(double width) {
        LayoutAdapter.super.realWidth(width);
    }

    @Override
    public double getRealHeight() {
        return LayoutAdapter.super.realHeight();
    }

    @Override
    public void setRealHeight(double height) {
        LayoutAdapter.super.realHeight(height);
    }
}
