package cn.oyzh.fx.gui.svg.glyph.file.g;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Gradle 构建文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileGradleSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileGradleSVGGlyph() {
        super("/fx-svg/file/g/file-gradle.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileGradleSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
