package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class SFTPSVGGlyph extends SVGGlyph {

    public SFTPSVGGlyph() {
        this.setUrl("/fx-svg/sftp.svg");
    }

    public SFTPSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
