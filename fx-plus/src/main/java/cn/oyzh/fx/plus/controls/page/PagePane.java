package cn.oyzh.fx.plus.controls.page;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.text.FlexText;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

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
    private final FlexText pageText = new FlexText();

    /**
     * 是否显示分页信息文本组件
     */
    @Getter
    private boolean showPageText = true;

    /**
     * 是否显示首页组件
     */
    @Getter
    private boolean showLast;

    /**
     * 是否显示尾页组件
     */
    @Getter
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
    @Getter
    @Setter
    private String pageTextTpl = "共#count条，每页#limit条，#currentPage/#countPage页";

    /**
     * 分页信息
     */
    @Getter
    private Paging<T> paging;

    /**
     * 图标大小
     */
    @Getter
    private String iconSize;

    /**
     * 隐藏控件，如果少于等于1页内容时
     */
    @Getter
    private boolean hideIfLessPage = true;

    /**
     * 上一页点击事件
     */
    @Setter
    @Getter
    private EventHandler<MouseEvent> onNextClicked;

    /**
     * 下一页点击事件
     */
    @Setter
    @Getter
    private EventHandler<MouseEvent> onPrevClicked;

    /**
     * 首页点击事件
     */
    @Getter
    private EventHandler<MouseEvent> onFirstClicked;

    /**
     * 尾页点击事件
     */
    @Getter
    private EventHandler<MouseEvent> onLastClicked;

    {
//        this.setCache(true);
//        this.setCacheShape(true);
//        this.setCacheHint(CacheHint.QUALITY);
        this.firstSVG = new SVGGlyph("/fx-plus/font/page-first.svg");
        this.prevSVG = new SVGGlyph("/fx-plus/font/arrow-left-line.svg");
        this.nextSVG = new SVGGlyph("/fx-plus/font/arrow-right-line.svg");
        this.lastSVG = new SVGGlyph("/fx-plus/font/page-last.svg");
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
        HBox.setMargin(this.firstSVG, new Insets(0, 0, 0, 0));
        HBox.setMargin(this.prevSVG, new Insets(0, 0, 0, 0));
        HBox.setMargin(this.nextSVG, new Insets(0, 0, 0, 5));
        HBox.setMargin(this.lastSVG, new Insets(0, 0, 0, 5));
        HBox.setMargin(this.pageText, new Insets(0, 0, 0, 5));
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
    public void setIconSize(@NonNull String iconSize) {
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
    public void setPaging(@NonNull Paging<T> paging) {
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
            HBox.setMargin(this.prevSVG, new Insets(0, 0, 0, 5));
        } else {
            HBox.setMargin(this.prevSVG, new Insets(0, 0, 0, 0));
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

    @Override
    public double getRealWidth() {
        return LayoutAdapter.super.realWidth();
    }

    @Override
    public void setRealWidth(double width) {
        LayoutAdapter.super.realWidth(width);
    }

    @Override
    public double getRealHeight() {
        return LayoutAdapter.super.realHeight();
    }

    @Override
    public void setRealHeight(double height) {
        LayoutAdapter.super.realHeight(height);
    }
}
