package cn.oyzh.fx.gui.svg.glyph.file.g;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Git Rebase 配置文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileGitRebaseSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileGitRebaseSVGGlyph() {
        super("/fx-svg/file/g/file-git-rebase.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileGitRebaseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
