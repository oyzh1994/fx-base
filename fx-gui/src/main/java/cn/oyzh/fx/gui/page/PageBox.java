package cn.oyzh.fx.gui.page;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.fx.gui.svg.glyph.page.PageFirstSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.page.PageLastSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.page.PageNextSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.page.PagePrevSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.page.PageSettingSVGGlyph;
import cn.oyzh.fx.plus.controls.FlexFlowPane;
import cn.oyzh.fx.plus.controls.label.FlexLabel;
import cn.oyzh.fx.gui.textfield.NumberTextField;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nManager;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
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
public class PageBox<T> extends FlexFlowPane {

    /**
     * 是否显示文本组件
     */
    @Getter
    private boolean showText = true;

    /**
     * 文本组件
     */
    @Getter
    private FlexLabel text;

    /**
     * 是否显示跳转组件
     */
    @Getter
    private boolean showJump;

    /**
     * 跳页组件
     */
    @Getter
    private NumberTextField jump;

    /**
     * 跳页事件
     */
    @Getter
    private EventHandler<PageEvent.PageJumpEvent> onJumpFired;

    /**
     * 是否显示首页组件
     */
    @Getter
    private boolean showFirst;

    /**
     * 首页
     */
    @Getter
    private PageFirstSVGGlyph firstBtn;

    /**
     * 首页点击事件
     */
    @Getter
    private EventHandler<MouseEvent> onFirstClicked;

    /**
     * 是否显示尾页组件
     */
    @Getter
    private boolean showLast;

    /**
     * 尾页
     */
    @Getter
    private PageLastSVGGlyph lastBtn;

    /**
     * 尾页点击事件
     */
    @Getter
    private EventHandler<MouseEvent> onLastClicked;

    /**
     * 上一页
     */
    @Getter
    private PagePrevSVGGlyph prevBtn;

    /**
     * 上一页点击事件
     */
    @Setter
    @Getter
    private EventHandler<MouseEvent> onPrevClicked;

    /**
     * 下一页
     */
    @Getter
    private PageNextSVGGlyph nextBtn;

    /**
     * 下一页点击事件
     */
    @Setter
    @Getter
    private EventHandler<MouseEvent> onNextClicked;

    /**
     * 是否显示设置组件
     */
    @Getter
    private boolean showSetting;

    /**
     * 设置
     */
    @Getter
    private PageSettingSVGGlyph settingBtn;

    /**
     * 按钮大小
     */
    @Getter
    @Setter
    private String bthSize;

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
     * 设置点击事件
     */
    @Getter
    private EventHandler<MouseEvent> onSettingClicked;

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
    protected String pageTextTpl() {
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE) {
            return "共#count条，每页#limit条，#currentPage/#countPage页";
        }
        if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE) {
            return "共#count條，每頁#limit條，#currentPage/#countPage頁";
        }
        return "Total #count, Limit #limit, Page #currentPage/#countPage";
    }

    public PageBox() {
        this("13");
    }

    public PageBox(String bthSize) {
        this.bthSize = bthSize;
        this.init();
    }

    /**
     * 执行初始化
     */
    protected void init() {
        // 首页
        this.firstBtn = new PageFirstSVGGlyph(this.bthSize);
        this.firstBtn.managedBindVisible();
        this.firstBtn.setOnMousePrimaryClicked(e -> {
            this.formatPage();
            if (this.onFirstClicked != null) {
                this.onFirstClicked.handle(e);
            }
        });
        this.firstBtn.setPadding(new Insets(0, 0, 0, 0));

        // 上一页
        this.prevBtn = new PagePrevSVGGlyph(this.bthSize);
        this.prevBtn.setOnMousePrimaryClicked(e -> {
            this.formatPage();
            if (this.onPrevClicked != null) {
                this.onPrevClicked.handle(e);
            }
        });
        this.prevBtn.setPadding(new Insets(0, 0, 0, 0));

        // 下一页
        this.nextBtn = new PageNextSVGGlyph(this.bthSize);
        this.nextBtn.setOnMousePrimaryClicked(e -> {
            this.formatPage();
            if (this.onNextClicked != null) {
                this.onNextClicked.handle(e);
            }
        });
        this.nextBtn.setPadding(new Insets(0, 0, 0, 0));

        // 尾页
        this.lastBtn = new PageLastSVGGlyph(this.bthSize);
        this.lastBtn.managedBindVisible();
        this.lastBtn.setOnMousePrimaryClicked(e -> {
            this.formatPage();
            if (this.onLastClicked != null) {
                this.onLastClicked.handle(e);
            }
        });
        this.lastBtn.setPadding(new Insets(0, 0, 0, 0));

        // 设置
        this.settingBtn = new PageSettingSVGGlyph(this.bthSize);
        this.settingBtn.managedBindVisible();
        this.settingBtn.setOnMousePrimaryClicked(e -> {
            this.formatPage();
            if (this.onSettingClicked != null) {
                this.onSettingClicked.handle(e);
            }
        });
        this.settingBtn.setPadding(new Insets(0, 0, 0, 0));

        // 跳页
        this.jump = new NumberTextField(true);
        this.jump.setMinVal(1);
        this.jump.setMaxWidth(50);
        this.jump.setBtnMarginRight(0);
        this.jump.setFlexHeight("90%");
        this.jump.managedBindVisible();
        this.jump.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (KeyboardUtil.isEnter(event) && this.onJumpFired != null) {
                this.onJumpFired.handle(PageEvent.jump(this.jump.getIntValue() - 1));
                event.consume();
            }
        });
        this.jump.setPadding(new Insets(0, 0, 0, 0));

        // 设置边距
        FlowPane.setMargin(this.jump, new Insets(0, 0, 0, 5));
        FlowPane.setMargin(this.prevBtn, new Insets(0, 0, 0, 5));
        FlowPane.setMargin(this.nextBtn, new Insets(0, 0, 0, 5));
        FlowPane.setMargin(this.lastBtn, new Insets(0, 0, 0, 5));
        FlowPane.setMargin(this.firstBtn, new Insets(0, 0, 0, 5));
        FlowPane.setMargin(this.settingBtn, new Insets(0, 0, 0, 5));

        // 添加子节点
        this.setChild(this.firstBtn, this.prevBtn, this.jump, this.nextBtn, this.lastBtn, this.settingBtn);

        // 执行初始化
        this.setShowLast(this.showLast);
        this.setShowText(this.showText);
        this.setShowJump(this.showJump);
        this.setShowFirst(this.showFirst);
        this.setShowSetting(this.showSetting);
        this.managedBindVisible();
        NodeManager.init(this);
    }

    /**
     * 格式化分页
     */
    private void formatPage() {
        if (this.showText) {
            String text = Objects.requireNonNullElse(this.paging, Paging.EMPTY).formatTpl(this.pageTextTpl);
            FXUtil.runWait(() -> this.text.setText(text));
        }
        if (this.showJump) {
            this.jump.setValue(this.paging.currentPage() + 1);
        }
    }

    /**
     * 设置分页信息
     *
     * @param paging 分页信息
     */
    public void setPaging(@NonNull Paging<T> paging) {
        this.paging = paging;
        this.formatPage();
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
     * 设置是否显示文本组件
     *
     * @param showText 是否显示分页信息文本组件
     */
    public void setShowText(boolean showText) {
        this.showText = showText;
        if (showText) {
            if (this.text == null) {
                this.text = new FlexLabel();
                this.text.setFlexHeight("90%");
                FlowPane.setMargin(this.text, new Insets(0, 0, 0, 5));
                this.addChild(this.text);
            }
        } else {
            if (this.text != null) {
                this.removeChild(this.text);
                this.text = null;
            }
        }
    }

    /**
     * 设置是否显示首页组件
     *
     * @param showFirst 是否显示首页组件
     */
    public void setShowFirst(boolean showFirst) {
        this.showFirst = showFirst;
        this.firstBtn.setVisible(showFirst);
    }

    /**
     * 设置首行点击事件
     *
     * @param onFirstClicked 首行点击事件
     */
    public void setOnFirstClicked(EventHandler<MouseEvent> onFirstClicked) {
        this.onFirstClicked = onFirstClicked;
        this.setShowFirst(true);
    }

    /**
     * 设置是否显示设置组件
     *
     * @param showSetting 是否显示首页组件
     */
    public void setShowSetting(boolean showSetting) {
        this.showSetting = showSetting;
        this.settingBtn.setVisible(showSetting);
    }

    /**
     * 设置设置点击事件
     *
     * @param onSettingClicked 首行点击事件
     */
    public void setOnSettingClicked(EventHandler<MouseEvent> onSettingClicked) {
        this.onSettingClicked = onSettingClicked;
        this.setShowSetting(true);
    }

    /**
     * 设置是否显示尾页组件
     *
     * @param showLast 是否显示尾页组件
     */
    public void setShowLast(boolean showLast) {
        this.showLast = showLast;
        this.lastBtn.setVisible(showLast);
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
     * 设置是否显示跳页组件
     *
     * @param showJump 是否显示跳页组件
     */
    public void setShowJump(boolean showJump) {
        this.showJump = showJump;
        this.jump.setVisible(showJump);
    }

    /**
     * 设置跳页触发事件
     *
     * @param onJumpFired 跳页触发事件
     */
    public void setOnJumpFired(EventHandler<PageEvent.PageJumpEvent> onJumpFired) {
        this.onJumpFired = onJumpFired;
        this.setShowJump(true);
    }
}
