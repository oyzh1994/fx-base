package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class SftpSVGGlyph extends SVGGlyph {

    public SftpSVGGlyph() {
        this.setUrl("/fx-svg/sftp.svg");
    }

    public SftpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
