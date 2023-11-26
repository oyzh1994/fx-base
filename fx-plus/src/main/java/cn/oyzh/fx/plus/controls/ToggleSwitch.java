package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.adapter.FontAdapter;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.text.FXLabel;
import cn.oyzh.fx.plus.util.ControlUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import lombok.Getter;
import lombok.NonNull;

/**
 * 切换开关组件
 *
 * @author oyzh
 * @since 2023/08/29
 */
public class ToggleSwitch extends FlexFlowPane implements FontAdapter {

    /**
     * 选中时显示的文字
     */
    @Getter
    private String selectedText;

    /**
     * 未选中时显示的文字
     */
    @Getter
    private String unselectedText;

    /**
     * 文字内容
     */
    private FXLabel label;

    /**
     * 选中图标
     */
    private final SVGGlyph selectedBtn;

    /**
     * 未选中图标
     */
    private final SVGGlyph unselectedBtn;

    /**
     * 选中属性
     */
    private BooleanProperty selectedProperty;

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setCursor(Cursor.HAND);
        this.setCacheHint(CacheHint.QUALITY);
        // 初始化按钮
        this.selectedBtn = ControlUtil.initSwitchButton(1);
        this.selectedBtn.disappear();
        this.selectedBtn.setPadding(new Insets(1, 0, 0, 0));
        this.unselectedBtn = ControlUtil.initSwitchButton(2);
        this.unselectedBtn.setPadding(new Insets(1, 0, 0, 0));
        // 选中变化事件
        this.selectedChanged((observable, oldValue, t1) -> {
            if (t1) {
                // 初始化选中按钮
                this.selectedBtn.display();
                this.unselectedBtn.disappear();
                this.setText(this.selectedText);
            } else {
                this.selectedBtn.disappear();
                this.unselectedBtn.display();
                this.setText(this.unselectedText);
            }
        });
        // 鼠标点击事件
        this.setOnMousePressed(mouseEvent -> this.reverse());
        // 添加组件
        this.getChildren().setAll(this.selectedBtn, this.unselectedBtn);
        // 设置基本高度
        this.setRealHeight(18);
    }

    /**
     * 设置文字
     *
     * @param text 文字
     */
    protected void setText(String text) {
        if (this.label == null) {
            this.label = new FXLabel(text);
            this.getChildren().add(this.label);
        } else {
            this.label.setText(text);
        }
        this.reCalcLabelPadding();
    }

    /**
     * 重新计算label的边距
     */
    protected void reCalcLabelPadding() {
        if (this.label != null) {
            double top = (this.getHeight() - this.label.getRealHeight()) / 2 + 1;
            top = Math.min(0, top);
            Insets insets = this.label.getInsets();
            if (insets == null || insets.getTop() != top || insets.getLeft() != 5) {
                this.label.setPadding(new Insets(top, 0, 0, 5));
            }
        }
    }

    /**
     * 反转状态
     */
    public void reverse() {
        this.setSelected(!this.isSelected());
    }

    /**
     * 设置选中
     *
     * @param value 值
     */
    public void setSelected(boolean value) {
        selectedProperty().set(value);
    }

    /**
     * 是否选中
     *
     * @return 结果
     */
    public boolean isSelected() {
        return this.selectedProperty != null && this.selectedProperty.get();
    }

    /**
     * 获取选中属性
     *
     * @return 选中属性
     */
    public final BooleanProperty selectedProperty() {
        if (this.selectedProperty == null) {
            this.selectedProperty = new SimpleBooleanProperty();
        }
        return this.selectedProperty;
    }

    @Override
    public void resize(double width, double height) {
        // 图标高度
        double h = height - 2;
        // 图标宽度是长度2.2倍
        double w = h * 2.2;
        // 设置图标大小
        if (this.selectedBtn.isVisible()) {
            if (this.selectedBtn.getWidth() != w || this.selectedBtn.getHeight() != h) {
                this.selectedBtn.setSizeStr(w + "," + height);
            }
        } else if (this.unselectedBtn.isVisible()) {
            if (this.unselectedBtn.getWidth() != w || this.unselectedBtn.getHeight() != h) {
                this.unselectedBtn.setSizeStr(w + "," + height);
            }
        }
        // 重新计算label边距
        this.reCalcLabelPadding();
        super.resize(width, height);
    }

    /**
     * 设置选中文字
     *
     * @param selectedText 选中文字
     */
    public void setSelectedText(String selectedText) {
        this.selectedText = selectedText;
        if (this.isSelected()) {
            this.setText(this.selectedText);
        }
    }

    /**
     * 设置未选中文字
     *
     * @param unselectedText 未选中文字
     */
    public void setUnselectedText(String unselectedText) {
        this.unselectedText = unselectedText;
        if (!this.isSelected()) {
            this.setText(this.unselectedText);
        }
    }

    /**
     * 选中变化事件
     *
     * @param listener 监听器
     */
    public void selectedChanged(@NonNull ChangeListener<Boolean> listener) {
        this.selectedProperty().addListener(listener);
    }

    @Override
    public void setFontSize(double fontSize) {
        FontAdapter.super.fontSize(fontSize);
    }

    @Override
    public double getFontSize() {
        return FontAdapter.super.fontSize();
    }

    @Override
    public void setFontFamily(@NonNull String fontFamily) {
        FontAdapter.super.fontFamily(fontFamily);
    }

    @Override
    public String getFontFamily() {
        return FontAdapter.super.fontFamily();
    }
}
