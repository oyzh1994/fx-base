package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 二维码 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class QRCodeSVGGlyph extends SVGGlyph {

    /**
     * 构造二维码 SVG 图标控件
     */
    public QRCodeSVGGlyph() {
        super("/fx-svg/qrcode.svg");
    }

    /**
     * 构造指定尺寸的二维码 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public QRCodeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.qrCode());
//        super.initNode();
//    }
}
