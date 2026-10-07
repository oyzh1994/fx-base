package cn.oyzh.fx.plus.swing;

import cn.oyzh.fx.plus.theme.ThemeManager;

import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Color;
import java.awt.Dimension;

/**
 * Swing 滚动条外观实现，按明暗主题设置轨道与滑块颜色并隐藏两端按钮（已废弃）
 *
 * @author oyzh
 * @since 2025-08-05
 */
@Deprecated
public class SwingScrollBarUI extends BasicScrollBarUI {

    /**
     * 暗色主题轨道颜色
     */
    public static final Color TRACK_COLOR_DARK = new Color(80, 80, 80);

    /**
     * 暗色主题滑块颜色
     */
    public static final Color THUMB_COLOR_DARK = new Color(120, 120, 120);

    /**
     * 暗色主题轨道高亮颜色
     */
    public static final Color TRACK_HIGHLIGHT_COLOR_DARK = new Color(160, 160, 160);

    /**
     * 亮色主题轨道颜色
     */
    public static final Color TRACK_COLOR_LIGHT = new Color(240, 240, 240);

    /**
     * 亮色主题滑块颜色
     */
    public static final Color THUMB_COLOR_LIGHT = new Color(180, 180, 180);

    /**
     * 亮色主题轨道高亮颜色
     */
    public static final Color TRACK_HIGHLIGHT_COLOR_LIGHT = new Color(120, 120, 120);

    @Override
    protected void configureScrollBarColors() {
        super.configureScrollBarColors();
        if (ThemeManager.isDarkMode()) {
            this.trackColor = TRACK_COLOR_DARK;
            this.thumbColor = THUMB_COLOR_DARK;
            this.trackHighlightColor = TRACK_HIGHLIGHT_COLOR_DARK;
        } else {
            this.trackColor = TRACK_COLOR_LIGHT;
            this.thumbColor = THUMB_COLOR_LIGHT;
            this.trackHighlightColor = TRACK_HIGHLIGHT_COLOR_LIGHT;
        }
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return this.createEmptyButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return this.createEmptyButton();
    }

    /**
     * 创建零尺寸的隐藏按钮
     *
     * @return 空按钮
     */
    private JButton createEmptyButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }

    @Override
    protected void layoutVScrollbar(JScrollBar sb) {
        try {
            super.layoutVScrollbar(sb);
        } catch (NullPointerException ignored) {

        }
    }

    @Override
    protected void layoutHScrollbar(JScrollBar sb) {
        try {
            super.layoutHScrollbar(sb);
        } catch (NullPointerException ignored) {

        }
    }
}
