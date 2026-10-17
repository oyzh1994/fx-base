package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.CollectSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.UnCollectSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import javafx.scene.text.Font;

/**
 * 收藏切换图标面板
 *
 * @author oyzh
 * @since 2024-12-09
 */
public class CollectSVGLabel extends SVGLabel {

    /**
     * 构造收藏切换图标面板，默认未收藏状态。
     */
    public CollectSVGLabel() {
        this.unCollect();
    }

    /**
     * 切换为已收藏图标
     */
    public void collect() {
        this.graphic(new UnCollectSVGGlyph());
        //super.setText("\u200B");
    }

    /**
     * 切换为未收藏图标
     */
    public void unCollect() {
        this.graphic(new CollectSVGGlyph());
        //super.setText("\u200B");
    }

    /**
     * 是否为已收藏状态
     *
     * @return 是否为已收藏状态
     */
    public boolean isCollect() {
        return this.getUrl().contains("star.svg");
    }

    /**
     * 设置收藏状态
     *
     * @param collect 是否已收藏
     */
    public void setCollect(boolean collect) {
        if (collect) {
            this.collect();
        } else {
            this.unCollect();
        }
    }

    @Override
    public void changeFont(Font font) {
        if (!this.isChildEmpty() && this.isEnableFont()) {
            if (this.isCollect()) {
                this.collect();
            } else {
                this.unCollect();
            }
        }
        super.changeFont(font);
    }
}
