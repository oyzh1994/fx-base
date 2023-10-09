package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.svg.SVGGlyph;
import cn.oyzh.fx.plus.util.ControlUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
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
public class ToggleSwitch extends FlexPane {

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
    private SVGGlyph selectedBtn;

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
        this.unselectedBtn = ControlUtil.initSwitchButton(2);
        // 选中变化事件
        this.selectedChanged((observable, oldValue, t1) -> {
            if (t1) {
                // 初始化选中按钮
                if (this.selectedBtn == null) {
                    this.selectedBtn = ControlUtil.initSwitchButton(1);
                    this.getChildren().add(this.selectedBtn);
                }
                this.selectedBtn.showNode();
                this.unselectedBtn.hideNode();
                this.setText(this.selectedText);
            } else {
                this.selectedBtn.hideNode();
                this.unselectedBtn.showNode();
                this.setText(this.unselectedText);
            }
        });
        // 鼠标点击事件
        this.setOnMousePressed(mouseEvent -> this.reverseSelected());
        // 添加组件
        this.getChildren().add(this.unselectedBtn);
        // 设置基本高度
        this.setRealHeight(20);
    }

    /**
     * 设置文字
     *
     * @param text 文字
     */
    private void setText(String text) {
        if (this.label == null) {
            this.label = new FXLabel(text);
            this.getChildren().add(this.label);
        } else {
            this.label.setText(text);
        }
    }

    /**
     * 反转选中状态
     */
    public void reverseSelected() {
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
        super.resize(width, height);
        // 图标高度
        double h = height - 2;
        // 图标宽度是长度2.2倍
        double w = h * 2.2;
        // 设置图标大小
        if (this.selectedBtn != null && this.selectedBtn.isVisible()) {
            this.selectedBtn.setSizeStr(w + "," + height);
            this.selectedBtn.relocate(0, 1);
        } else if (this.unselectedBtn.isVisible()) {
            this.unselectedBtn.setSizeStr(w + "," + height);
            this.unselectedBtn.relocate(0, 1);
        }
        // 重新计算label位置
        if (this.label != null) {
            double y2 = (height - this.label.getRealHeight()) / 2 + 1;
            this.label.relocate(w + 5, y2);
        }
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
}
