package cn.oyzh.fx.gui.svg.glyph.file.a;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * APK 安装包 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileApkSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileApkSVGGlyph() {
        super("/fx-svg/file/a/file-apk.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileApkSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
