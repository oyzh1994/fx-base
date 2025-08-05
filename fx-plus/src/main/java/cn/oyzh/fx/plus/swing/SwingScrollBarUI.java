package cn.oyzh.fx.plus.swing;

import cn.oyzh.fx.plus.theme.ThemeManager;

import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Color;
import java.awt.Dimension;

/**
 * @author oyzh
 * @since 2025-08-05
 */
public class SwingScrollBarUI extends BasicScrollBarUI {

    @Override
    protected void configureScrollBarColors() {
        super.configureScrollBarColors();
        if (ThemeManager.isDarkMode()) {
            this.trackColor = new Color(80, 80, 80);
            this.thumbColor = new Color(120, 120, 120);
            this.trackHighlightColor = new Color(160, 160 ,160);
        } else {
            this.trackColor = new Color(240, 240, 240);
            this.thumbColor = new Color(180, 180, 180);
            this.trackHighlightColor = new Color(120, 120 ,120);
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
