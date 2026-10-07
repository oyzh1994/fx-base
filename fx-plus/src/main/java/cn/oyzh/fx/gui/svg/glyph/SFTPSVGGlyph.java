package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * SFTP SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class SFTPSVGGlyph extends SVGGlyph {

    /**
     * 构造SFTP SVG 图标控件
     */
    public SFTPSVGGlyph() {
        super("/fx-svg/sftp.svg");
    }

    /**
     * 构造指定尺寸的SFTP SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public SFTPSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
