package cn.oyzh.fx.gui.svg.pane;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.svg.glyph.EyeCloseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.EyeOpenSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGPane;
import cn.oyzh.fx.plus.font.FontManager;
import javafx.geometry.Insets;
import javafx.scene.text.Font;

/**
 * 显隐切换图标面板
 *
 * @author oyzh
 * @since 2024-12-09
 */
public class HiddenSVGPane extends SVGPane {

    /**
     * 构造显隐切换图标面板，默认隐藏状态。
     */
    public HiddenSVGPane() {
        this.hidden();
    }

    /**
     * 切换为显示图标（睁眼）
     */
    public void show() {
        this.setChild(new EyeOpenSVGGlyph(this.getSize()));
    }

    /**
     * 切换为隐藏图标（闭眼）
     */
    public void hidden() {
        this.setChild(new EyeCloseSVGGlyph(this.getSize()));
    }

    /**
     * 是否隐藏状态
     *
     * @return 是否隐藏状态
     */
    public boolean isHidden() {
        SVGGlyph svgGlyph = (SVGGlyph) this.getChildren().getFirst();
        return svgGlyph.getUrl().contains("eye-close.svg");
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
