package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;
import lombok.Getter;

/**
 * 图标按钮
 *
 * @author oyzh
 * @since 2023/1/13
 */
public class IconButton extends FlexButton {

    /**
     * 图标对比字符的百分比
     */
    @Getter
    private Double iconSizePercent = 0.7;

    {
        this.initListener();
    }

    /**
     * 设置图标对比字符的百分比
     *
     * @param iconSizePercent 图标对比字符的百分比
     */
    public void setIconSizePercent(Double iconSizePercent) {
        this.iconSizePercent = iconSizePercent;
        if (iconSizePercent != null && this.getFont() != null && this.getGraphic() instanceof SVGGlyph glyph) {
            glyph.setSize(this.getFont().getSize() * iconSizePercent);
        }
    }

    /**
     * 初始化图标
     *
     * @param iconUrl         图标地址
     * @param iconSizePercent 图标对比字符的百分比
     */
    protected void init(String iconUrl, double iconSizePercent) {
        if (iconUrl != null) {
            this.setGraphic(new SVGGlyph(iconUrl));
        }
        this.setIconSizePercent(iconSizePercent);
    }

    /**
     * 初始化图标
     */
    protected void initListener() {
        this.fontProperty().addListener((observable, o, n) -> {
            if (this.iconSizePercent != null && this.getGraphic() instanceof SVGGlyph glyph) {
                glyph.setSize(n.getSize() * this.iconSizePercent);
            }
        });
        this.textFillProperty().addListener((observable, o, n) -> {
            if (n instanceof Color color && this.getGraphic() instanceof SVGGlyph glyph) {
                glyph.setColor(color);
            }
        });
//        this.disableProperty().addListener((observable, o, n) -> {
//            if (!n && this.getGraphic() instanceof SVGGlyph glyph && this.getTextFill() instanceof Color color) {
//                glyph.setColor(color);
//            }
//        });
        this.graphicProperty().addListener((observable, o, n) -> {
            if (n instanceof SVGGlyph glyph) {
                glyph.disableTheme();
                if (this.iconSizePercent != null && this.getFont() != null) {
                    glyph.setSize(this.getFont().getSize() * this.iconSizePercent);
                }
                glyph.setColor((Color) this.getTextFill());
            }
        });
    }
}
