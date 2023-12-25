package cn.oyzh.fx.plus.theme;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.plus.FXStyle;
import cn.oyzh.fx.plus.adapter.PropAdapter;
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
     * @param themeType 主题
     */
    default void changeTheme(ThemeType themeType) {
        if (this.isEnableTheme() && themeType != null) {
            switch (this) {
                case Parent node -> this.handleStyle(node, themeType);
                case StageWrapper wrapper -> this.handleStyle(wrapper.root(), themeType);
                case Stage stage -> this.handleStyle(stage.getScene().getRoot(), themeType);
                default -> {
                }
            }
        }
    }

    /**
     * 处理样式
     *
     * @param node      节点
     * @param themeType 主题
     */
    private void handleStyle(Parent node, ThemeType themeType) {
        if (node != null) {
            String style = switch (themeType) {
                case DRACULA -> AtlantaFX.DRACULA.getUserAgentStylesheet();
                case NORD_DARK -> AtlantaFX.NORD_DARK.getUserAgentStylesheet();
                case NORD_LIGHT -> AtlantaFX.NORD_LIGHT.getUserAgentStylesheet();
                case PRIMER_DARK -> AtlantaFX.PRIMER_DARK.getUserAgentStylesheet();
                case PRIMER_LIGHT -> AtlantaFX.PRIMER_LIGHT.getUserAgentStylesheet();
                case CUPERTINO_DARK -> AtlantaFX.CUPERTINO_DARK.getUserAgentStylesheet();
                case CUPERTINO_LIGHT -> AtlantaFX.CUPERTINO_LIGHT.getUserAgentStylesheet();
            };
            List<String> removes = CollUtil.toList(AtlantaFX.styles());
            removes.add(FXStyle.FX_BASE);
            node.getStylesheets().removeAll(removes);
            node.getStylesheets().add(style);
            node.getStylesheets().add(FXStyle.FX_BASE);
        }
    }
}
