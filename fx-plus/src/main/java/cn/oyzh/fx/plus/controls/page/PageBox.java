package cn.oyzh.fx.plus.controls.page;

import cn.oyzh.fx.common.dto.Paging;
import cn.oyzh.fx.plus.controls.FlexHBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.button.FlexButton;
import cn.oyzh.fx.plus.controls.text.FXLabel;
import cn.oyzh.fx.plus.controls.text.FlexLabel;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nManager;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Locale;
import java.util.Objects;

/**
 * 分页面板组件
 *
 * @author oyzh
 * @since 2022/12/22
 */
public class PageBox<T> extends FlexHBox {

    /**
     * 分页信息文本组件
     */
    private final FlexLabel pageText;

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
    private final FlexButton firstBtn;

    /**
     * 尾页
     */
    private final FlexButton lastBtn;

    /**
     * 上一页
     */
    private final FlexButton prevBtn;

    /**
     * 下一页
     */
    private final FlexButton nextBtn;

    /**
     * 分页信息文本模板
     */
    @Getter
    @Setter
    private String pageTextTpl = this.pageTextTpl();

    /**
     * 获取解析模板
     *
     * @return 模板内容
     */
    private String pageTextTpl() {
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE) {
            return "共#count条，每页#limit条，#currentPage/#countPage页";
        }
        if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE) {
            return "共#count條，每頁#limit條，#currentPage/#countPage頁";
        }

        return "Total #count, Limit #limit, Page #currentPage/#countPage";
    }

    /**
     * 分页信息
     */
    @Getter
    private Paging<T> paging;

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
        // 首页
        FXLabel firstLabel = new FXLabel("<<");
        firstLabel.setAlignment(Pos.CENTER);
        this.firstBtn = new FlexButton();
        this.firstBtn.setGraphic(firstLabel);
        this.firstBtn.setFlexHeight("90%");
        this.firstBtn.prefWidth(40);
        this.firstBtn.setTipText(I18nHelper.firstPage());
        this.firstBtn.setOnMousePrimaryClicked(e -> {
            this.formatPageText();
            if (this.onFirstClicked != null) {
                this.onFirstClicked.handle(e);
            }
        });
        this.firstBtn.managedBindVisible();
        this.firstBtn.setPadding(new Insets(0,0,0,0));

        // 上一页
        FXLabel prevLabel = new FXLabel("<");
        prevLabel.setAlignment(Pos.CENTER);
        this.prevBtn = new FlexButton();
        this.prevBtn.setGraphic(prevLabel);
        this.prevBtn.setFlexHeight("90%");
        this.prevBtn.prefWidth(30);
        this.prevBtn.setTipText(I18nHelper.prevPage());
        this.prevBtn.setOnMousePrimaryClicked(e -> {
            this.formatPageText();
            if (this.onPrevClicked != null) {
                this.onPrevClicked.handle(e);
            }
        });
        this.prevBtn.setPadding(new Insets(0,0,0,0));

        // 下一页
        FXLabel nextLabel = new FXLabel(">");
        nextLabel.setAlignment(Pos.CENTER);
        this.nextBtn = new FlexButton();
        this.nextBtn.setGraphic(nextLabel);
        this.nextBtn.setFlexHeight("90%");
        this.nextBtn.prefWidth(30);
        this.nextBtn.setTipText(I18nHelper.nextPage());
        this.nextBtn.setOnMousePrimaryClicked(e -> {
            this.formatPageText();
            if (this.onNextClicked != null) {
                this.onNextClicked.handle(e);
            }
        });
        this.nextBtn.setPadding(new Insets(0,0,0,0));

        // 尾页
        FXLabel lastLabel = new FXLabel(">>");
        lastLabel.setAlignment(Pos.CENTER);
        this.lastBtn = new FlexButton();
        this.lastBtn.setGraphic(lastLabel);
        this.lastBtn.setFlexHeight("90%");
        this.lastBtn.prefWidth(40);
        this.lastBtn.setTipText(I18nHelper.lastPage());
        this.lastBtn.setOnMousePrimaryClicked(e -> {
            this.formatPageText();
            if (this.onLastClicked != null) {
                this.onLastClicked.handle(e);
            }
        });
        this.lastBtn.managedBindVisible();
        this.lastBtn.setPadding(new Insets(0,0,0,0));

        // 页码文本
        this.pageText = new FlexLabel();
        this.pageText.setFlexHeight("90%");
        this.pageText.managedBindVisible();

        // // 设置边距
        // HBox.setMargin(this.firstBtn, new Insets(0, 0, 0, 0));
        // HBox.setMargin(this.prevBtn, new Insets(0, 0, 0, 0));
        // HBox.setMargin(this.nextBtn, new Insets(0, 0, 0, 0));
        // HBox.setMargin(this.lastBtn, new Insets(0, 0, 0, 0));
        HBox.setMargin(this.pageText, new Insets(0, 0, 0, 5));

        // 添加子节点
        this.getChildren().addAll(this.firstBtn, this.prevBtn, this.nextBtn, this.lastBtn, this.pageText);

        // 执行初始化
        this.setShowLast(this.showLast);
        this.setShowFirst(this.showFirst);
        this.setShowPageText(this.showPageText);
        this.formatPageText();
        this.managedProperty().bind(this.visibleProperty());
        NodeManager.init(this);
    }

    /**
     * 格式化分页文本
     */
    private void formatPageText() {
        if (this.showPageText) {
            String text = Objects.requireNonNullElse(this.paging, Paging.EMPTY).formatTpl(this.pageTextTpl);
            FXUtil.runWait(()-> this.pageText.setText(text));
        }
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
        this.firstBtn.setVisible(showFirst);
        // if (this.showFirst) {
        //     HBox.setMargin(this.prevBtn, new Insets(0, 0, 0, 5));
        // } else {
        //     HBox.setMargin(this.prevBtn, new Insets(0, 0, 0, 0));
        // }
        this.updateSize();
    }

    /**
     * 更新组件大小
     */
    private void updateSize() {
        if (this.showFirst && this.showLast) {
            this.pageText.setFlexWidth("100% - 190");
        } else if (this.showFirst || this.showLast) {
            this.pageText.setFlexWidth("100% - 140");
        } else {
            this.pageText.setFlexWidth("100% - 90");
        }
    }

    /**
     * 设置尾行点击事件
     *
     * @param onLastClicked 尾行点击事件
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
        this.lastBtn.setVisible(showLast);
        this.updateSize();
    }
}
