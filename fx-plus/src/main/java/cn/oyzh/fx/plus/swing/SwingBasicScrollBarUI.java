package cn.oyzh.fx.plus.swing;

import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Dimension;

/**
 * @author oyzh
 * @since 2025-08-05
 */
public class SwingBasicScrollBarUI extends BasicScrollBarUI {

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
}
