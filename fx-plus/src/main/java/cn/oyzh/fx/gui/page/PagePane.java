package cn.oyzh.fx.gui.page;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.util.Objects;

/**
 * 分页面板组件
 *
 * @author oyzh
 * @since 2022/12/22
 */
public class PagePane<T> extends Region implements LayoutAdapter, ThemeAdapter {

    /**
     * 分页信息文本组件
     */
    private final FXText pageText = new FXText();

    /**
     * 是否显示分页信息文本组件
     */
    private boolean showPageText = true;

    /**
     * 是否显示首页组件
     */
    private boolean showLast;

    /**
     * 是否显示尾页组件
     */
    private boolean showFirst;

    /**
     * 首页
     */
    private final SVGGlyph firstSVG;

    /**
     * 尾页
     */
    private final SVGGlyph lastSVG;

    /**
     * 上一页
     */
    private final SVGGlyph prevSVG;

    /**
     * 下一页
     */
    private final SVGGlyph nextSVG;

    /**
     * 分页信息文本模板
     */
    private String pageTextTpl = "共#count条，每页#limit条，#currentPage/#countPage页";

    /**
     * 分页信息
     */
    private Paging<T> paging;

    /**
     * 图标大小
     */
    private String iconSize;

    /**
     * 隐藏控件，如果少于等于1页内容时
     */
    private boolean hideIfLessPage = true;

    /**
     * 上一页点击事件
     */
    private EventHandler<MouseEvent> onNextClicked;

    /**
     * 下一页点击事件
     */
    private EventHandler<MouseEvent> onPrevClicked;

    /**
     * 首页点击事件
     */
    private EventHandler<MouseEvent> onFirstClicked;

    /**
     * 尾页点击事件
     */
    private EventHandler<MouseEvent> onLastClicked;

    /**
     * 默认边距
     */
    private static final Insets DEFAULT_MARGIN = new Insets(0, 0, 0, 5);

    {
//        this.setCache(true);
//        this.setCacheShape(true);
//        this.setCacheHint(CacheHint.QUALITY);
        this.firstSVG = new SVGGlyph("/fx-svg/page-first.svg");
        this.prevSVG = new SVGGlyph("/fx-svg/arrow-left-line.svg");
        this.nextSVG = new SVGGlyph("/fx-svg/arrow-right-line.svg");
        this.lastSVG = new SVGGlyph("/fx-svg/page-last.svg");
        this.firstSVG.setTipText("首页");
        this.prevSVG.setTipText("上一页");
        this.nextSVG.setTipText("下一页");
        this.lastSVG.setTipText("尾页");
        this.firstSVG.setOnMousePrimaryClicked(e -> {
            this.formatPageText();
            if (this.onFirstClicked != null) {
                this.onFirstClicked.handle(e);
            }
        });
        this.prevSVG.setOnMousePrimaryClicked(e -> {
            this.formatPageText();
            if (this.onPrevClicked != null) {
                this.onPrevClicked.handle(e);
            }
        });
        this.nextSVG.setOnMousePrimaryClicked(e -> {
            this.formatPageText();
            if (this.onNextClicked != null) {
                this.onNextClicked.handle(e);
            }
        });
        this.lastSVG.setOnMousePrimaryClicked(e -> {
            this.formatPageText();
            if (this.onLastClicked != null) {
                this.onLastClicked.handle(e);
            }
        });
        HBox box = new HBox(this.firstSVG, this.prevSVG, this.nextSVG, this.lastSVG, this.pageText);

        HBox.setMargin(this.firstSVG, Insets.EMPTY);
        HBox.setMargin(this.prevSVG, Insets.EMPTY);
        HBox.setMargin(this.nextSVG, DEFAULT_MARGIN);
        HBox.setMargin(this.lastSVG, DEFAULT_MARGIN);
        HBox.setMargin(this.pageText, DEFAULT_MARGIN);
        this.getChildren().add(box);
        this.firstSVG.managedProperty().bind(this.firstSVG.visibleProperty());
        this.lastSVG.managedProperty().bind(this.lastSVG.visibleProperty());
        this.pageText.managedProperty().bind(this.pageText.visibleProperty());

        // 执行初始化
        this.setShowLast(this.showLast);
        this.setShowFirst(this.showFirst);
        this.setShowPageText(this.showPageText);
        this.formatPageText();
        this.managedProperty().bind(this.visibleProperty());
//        this.changeTheme(ThemeManager.currentTheme());
        NodeManager.init(this);
    }

    /**
     * 格式化分页文本
     */
    private void formatPageText() {
        if (this.showPageText) {
            String text = Objects.requireNonNullElse(this.paging, Paging.EMPTY).formatTpl(this.pageTextTpl);
            this.pageText.setText(text);
        }
    }

    /**
     * 设置图标大小
     *
     * @param iconSize 图标大小
     */
    public void setIconSize(String iconSize) {
        this.iconSize = iconSize;
        this.firstSVG.setSizeStr(iconSize);
        this.prevSVG.setSizeStr(iconSize);
        this.nextSVG.setSizeStr(iconSize);
        this.lastSVG.setSizeStr(iconSize);
    }

    /**
     * 设置分页信息
     *
     * @param paging 分页信息
     */
    public void setPaging(Paging<T> paging) {
        this.paging = paging;
        this.formatPageText();
        if (this.hideIfLessPage) {
            this.setVisible(paging.countPage() > 1);
        }
    }

    /**
     * 设置如果少于等于1页内容时，是否隐藏控件
     *
     * @param hideIfLessPage 少于等于1页内容时，是否隐藏控件
     */
    public void setHideIfLessPage(boolean hideIfLessPage) {
        this.hideIfLessPage = hideIfLessPage;
        if (!this.isVisible()) {
            this.setVisible(true);
        }
    }

    /**
     * 设置是否显示分页信息文本组件
     *
     * @param showPageText 是否显示分页信息文本组件
     */
    public void setShowPageText(boolean showPageText) {
        this.showPageText = showPageText;
        this.pageText.setVisible(this.showPageText);
    }

    /**
     * 设置首行点击时间
     *
     * @param onFirstClicked 首行点击时间
     */
    public void setOnFirstClicked(EventHandler<MouseEvent> onFirstClicked) {
        this.onFirstClicked = onFirstClicked;
        this.setShowFirst(true);
    }

    /**
     * 设置是否显示首页组件
     *
     * @param showFirst 是否显示首页组件
     */
    public void setShowFirst(boolean showFirst) {
        this.showFirst = showFirst;
        this.firstSVG.setVisible(showFirst);
        if (this.showFirst) {
            HBox.setMargin(this.prevSVG, DEFAULT_MARGIN);
        } else {
            HBox.setMargin(this.prevSVG, Insets.EMPTY);
        }
    }

    /**
     * 设置尾行点击时间
     *
     * @param onLastClicked 尾行点击时间
     */
    public void setOnLastClicked(EventHandler<MouseEvent> onLastClicked) {
        this.onLastClicked = onLastClicked;
        this.setShowLast(true);
    }

    /**
     * 设置是否显示尾页组件
     *
     * @param showLast 是否显示尾页组件
     */
    public void setShowLast(boolean showLast) {
        this.showLast = showLast;
        this.lastSVG.setVisible(showLast);
    }

//    @Override
//    public double getRealWidth() {
//        return LayoutAdapter.super.realWidth();
//    }
//
//    @Override
//    public void setRealWidth(double width) {
//        LayoutAdapter.super.realWidth(width);
//    }
//
//    @Override
//    public double getRealHeight() {
//        return LayoutAdapter.super.realHeight();
//    }
//
//    @Override
//    public void setRealHeight(double height) {
//        LayoutAdapter.super.realHeight(height);

    public FXText getPageText() {
        return pageText;
    }

    public boolean isShowPageText() {
        return showPageText;
    }

    public boolean isShowLast() {
        return showLast;
    }

    public boolean isShowFirst() {
        return showFirst;
    }

    public SVGGlyph getFirstSVG() {
        return firstSVG;
    }

    public SVGGlyph getLastSVG() {
        return lastSVG;
    }

    public SVGGlyph getPrevSVG() {
        return prevSVG;
    }

    public SVGGlyph getNextSVG() {
        return nextSVG;
    }

    public String getPageTextTpl() {
        return pageTextTpl;
    }

    public void setPageTextTpl(String pageTextTpl) {
        this.pageTextTpl = pageTextTpl;
    }

    public Paging<T> getPaging() {
        return paging;
    }

    public String getIconSize() {
        return iconSize;
    }

    public boolean isHideIfLessPage() {
        return hideIfLessPage;
    }

    public EventHandler<MouseEvent> getOnNextClicked() {
        return onNextClicked;
    }

    public void setOnNextClicked(EventHandler<MouseEvent> onNextClicked) {
        this.onNextClicked = onNextClicked;
    }

    public EventHandler<MouseEvent> getOnPrevClicked() {
        return onPrevClicked;
    }

    public void setOnPrevClicked(EventHandler<MouseEvent> onPrevClicked) {
        this.onPrevClicked = onPrevClicked;
    }

    public EventHandler<MouseEvent> getOnFirstClicked() {
        return onFirstClicked;
    }

    public EventHandler<MouseEvent> getOnLastClicked() {
        return onLastClicked;
    }
//    }
}
