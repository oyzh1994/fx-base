package cn.oyzh.fx.plus.theme;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.plus.FXStyle;
import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.extra.AtlantaFX;
import cn.oyzh.fx.plus.stage.StageWrapper;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.util.List;

/**
 * 主题适配器
 *
 * @author oyzh
 * @since 2023/05/11
 */
public interface ThemeAdapter extends PropAdapter {

    /**
     * 禁用主题
     */
    default void disableTheme() {
        this.setProp("_enableTheme", false);
    }

    /**
     * 启用主题
     */
    default void enableTheme() {
        this.removeProp("_enableTheme");
    }

    /**
     * 设置启用主题
     *
     * @param enableTheme 启用主题
     */
    default void setEnableTheme(boolean enableTheme) {
        this.setProp("_enableTheme", enableTheme);
    }

    /**
     * 是否启用主题
     *
     * @return 结果
     */
    default boolean isEnableTheme() {
        Boolean b = this.getProp("_enableTheme");
        return b == null || b;
    }

    /**
     * 更改主题
     *
     * @param theme 主题
     */
    default void changeTheme(Theme theme) {
        if (this.isEnableTheme() && theme != null) {
            switch (this) {
                case Parent node -> this.handleStyle(node, theme);
                case StageWrapper wrapper -> this.handleStyle(wrapper.root(), theme);
                case Stage stage -> this.handleStyle(stage.getScene().getRoot(), theme);
                default -> {
                }
            }
        }
    }

    /**
     * 处理样式
     *
     * @param node  节点
     * @param theme 主题
     */
    private void handleStyle(Parent node, Theme theme) {
        if (node != null) {
            String style = switch (theme) {
                case PRIMER_LIGHT -> AtlantaFX.PRIMER_LIGHT;
                case PRIMER_DARK -> AtlantaFX.PRIMER_DARK;
                case NORD_LIGHT -> AtlantaFX.NORD_LIGHT;
                case NORD_DARK -> AtlantaFX.NORD_DARK;
                case CUPERTINO_LIGHT -> AtlantaFX.CUPERTINO_LIGHT;
                case CUPERTINO_DARK -> AtlantaFX.CUPERTINO_DARK;
                case DRACULA -> AtlantaFX.DRACULA;
            };
            List<String> removes = CollUtil.toList(AtlantaFX.styles());
            removes.add(FXStyle.FX_BASE);
            node.getStylesheets().removeAll(removes);
            node.getStylesheets().addAll(style, FXStyle.FX_BASE);
        }
    }
}
