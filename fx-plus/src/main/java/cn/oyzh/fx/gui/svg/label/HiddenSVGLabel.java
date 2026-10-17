package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.EyeCloseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.EyeOpenSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import javafx.scene.text.Font;

/**
 * 显隐切换图标面板
 *
 * @author oyzh
 * @since 2025-04-02
 */
public class HiddenSVGLabel extends SVGLabel {

    /**
     * 构造显隐切换图标面板，默认隐藏状态。
     */
    public HiddenSVGLabel() {
        this.hidden();
    }

    /**
     * 切换为显示图标（睁眼）
     */
    public void show() {
        this.graphic(new EyeOpenSVGGlyph());
        //super.setText("\u200B");
    }

    /**
     * 切换为隐藏图标（闭眼）
     */
    public void hidden() {
        this.graphic(new EyeCloseSVGGlyph());
        //super.setText("\u200B");
    }

    /**
     * 是否隐藏状态
     *
     * @return 是否隐藏状态
     */
    public boolean isHidden() {
        return this.getUrl().contains("eye-close.svg");
    }

    /**
     * 设置隐藏状态
     *
     * @param hidden 是否隐藏
     */
    public void setHidden(boolean hidden) {
        if (hidden) {
            this.hidden();
        } else {
            this.show();
        }
    }

    @Override
    public void changeFont(Font font) {
        if (!this.isChildEmpty() && this.isEnableFont()) {
            if (this.isHidden()) {
                this.hidden();
            } else {
                this.show();
            }
        }
        super.changeFont(font);
    }
}
