package cn.oyzh.fx.plus.adapter;

import cn.oyzh.fx.plus.font.FontAdapter;

/**
 * 文本组件适配器
 *
 * @author oyzh
 * @since 2023/1/29
 */
@Deprecated
public interface AreaAdapter extends TextAdapter, FontAdapter {

    /**
     * 刷新光标
     */
    void flushCaret();

    /**
     * 字体大小递增
     */
    default void fontSizeIncr() {
        this.setFontSize(this.getFontSize() + 1);
        this.flushCaret();
    }

    /**
     * 字体大小递减
     */
    default void fontSizeDecr() {
        this.setFontSize(this.getFontSize() - 1);
        this.flushCaret();
    }

    /**
     * 设置内容
     *
     * @param text 文本内容
     */
    default void setTextExt(String text) {

    }

}
