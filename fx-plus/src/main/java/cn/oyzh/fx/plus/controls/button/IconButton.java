package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
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
    private Double iconSizePercent = 1.0;

    @Override
    public void initNode() {
        this.initListener();
        super.initNode();
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
            SVGGlyph glyph = new SVGGlyph(iconUrl);
            this.setGraphic(glyph);
        }
        this.setIconSizePercent(iconSizePercent);
        this.initGlyph();
    }

    /**
     * 初始化图标
     *
     * @param glyph           图标
     * @param iconSizePercent 图标对比字符的百分比
     */
    protected void init(SVGGlyph glyph, double iconSizePercent) {
        this.setGraphic(glyph);
        this.setIconSizePercent(iconSizePercent);
        this.initGlyph();
    }

    public void setIconUrl(String url) {
        this.init(url, this.iconSizePercent);
    }

    public String getIconUrl() {
        if (this.getGraphic() instanceof SVGGlyph glyph) {
            return glyph.getUrl();
        }
        return null;
    }

    /**
     * 初始化图标
     */
    protected void initListener() {
        this.fontProperty().addListener((observable, o, n) -> this.initGlyph());
        this.graphicProperty().addListener((observable, o, n) -> this.initGlyph());
        this.textFillProperty().addListener((observable, o, n) -> this.initGlyph());
        this.backgroundProperty().addListener((observable, o, n) -> this.initGlyph());
    }

    /**
     * 初始化图标
     */
    private void initGlyph() {
        if (this.getGraphic() instanceof SVGGlyph glyph) {
            glyph.disableTheme();
            if (this.iconSizePercent != null && this.getFont() != null) {
                glyph.setSize(this.getFontSize() * this.iconSizePercent);
            }
            if (this.getTextFill() != null) {
                glyph.setColor(this.getTextFill());
            }
        }
    }
}
