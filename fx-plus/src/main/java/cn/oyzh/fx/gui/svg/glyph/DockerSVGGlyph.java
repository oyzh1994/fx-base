package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class DockerSVGGlyph extends SVGGlyph {

    public DockerSVGGlyph() {
        this.setUrl("/fx-svg/docker.svg");
    }

    public DockerSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
