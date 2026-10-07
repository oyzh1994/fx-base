package cn.oyzh.fx.gui.svg.glyph.file.g;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Git 忽略配置文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileGitIgnoreSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileGitIgnoreSVGGlyph() {
        super("/fx-svg/file/g/file-gitignore.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileGitIgnoreSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
