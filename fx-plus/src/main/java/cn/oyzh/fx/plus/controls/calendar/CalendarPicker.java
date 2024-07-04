package cn.oyzh.fx.plus.controls.calendar;

import atlantafx.base.controls.Popover;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.oyzh.fx.plus.controls.box.FlexHBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.textfield.ClearableTextField;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author oyzh
 * @since 2023/12/24
 */
public abstract class CalendarPicker<S extends CalendarSelector> extends FlexHBox {

    /**
     * 弹出组件选择器
     */
    protected S selector;

    /**
     * 弹出组件
     */
    protected Popover popup;

    /**
     * 格式化器
     */
    protected DateTimeFormatter formatter;

    /**
     * 文本组件
     */
    protected final ClearableTextField textField;

    /**
     * 值属性
     */
    protected ObjectProperty<LocalDateTime> valueProperty;

    public CalendarPicker() {
        // 初始化文本组件
        this.textField = new ClearableTextField();
        this.textField.setFocusTraversable(false);
        this.textField.maxHeightProperty().bind(this.maxHeightProperty());
        this.textField.setFlexWidth("100% - 30");
        this.textField.minHeightProperty().bind(this.minHeightProperty());
        this.textField.prefHeightProperty().bind(this.prefHeightProperty());

        // 初始化按钮组件
        FXButton button = new FXButton();
        button.setRealWidth(30);
        button.maxHeightProperty().bind(this.maxHeightProperty());
        button.minHeightProperty().bind(this.minHeightProperty());
        button.prefHeightProperty().bind(this.prefHeightProperty());
        button.setGraphic(new ImageView("/fx-plus/img/calendar.png"));

        // 初始化监听器
        this.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                this.textField.clear();
            } else {
                this.textField.setText(this.formatter.format(newValue));
            }
        });
        button.setOnMousePrimaryClicked(event -> {
            if (this.isPopupShowing()) {
                this.hidePopup();
            } else {
                this.showPopup();
            }
        });

        // 添加到子节点列表
        this.getChildren().add(this.textField);
        this.getChildren().add(button);
    }

    /**
     * 设置日期格式
     *
     * @param pattern 日期格式
     */
    public void setPattern(String pattern) {
        // 初始化格式化器
        if (pattern == null || pattern.isBlank()) {
            this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        } else {
            this.formatter = DateTimeFormatter.ofPattern(pattern);
            this.setProp("pattern", pattern);
        }
    }

    /**
     * 获取日期格式
     *
     * @return 日期格式
     */
    public String getPattern() {
        return this.getProp("pattern");
    }

    /**
     * 设置提示文本
     *
     * @param promptText 提示文本
     */
    public void setPromptText(String promptText) {
        this.textField.setPromptText(promptText);
    }

    /**
     * 获取提示文本
     *
     * @return 提示文本
     */
    public String setPromptText() {
        return this.textField.getPromptText();
    }

    /**
     * 获取值属性
     *
     * @return 值属性
     */
    public ObjectProperty<LocalDateTime> valueProperty() {
        if (this.valueProperty == null) {
            this.valueProperty = new SimpleObjectProperty<>();
        }
        return this.valueProperty;
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public LocalDateTime getValue() {
        return this.valueProperty == null ? null : this.valueProperty.get();
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void setValue(LocalDateTime value) {
        this.valueProperty().setValue(value);
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void setValue(Date value) {
        this.setValue(LocalDateTimeUtil.of(value));
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void setValue(java.sql.Date value) {
        if (value != null) {
            this.setValue(new Date(value.getTime()));
        }
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void setValue(Object value) {
        if (value instanceof java.sql.Date d1) {
            this.setValue(d1);
        } else if (value instanceof Date d2) {
            this.setValue(d2);
        } else if (value instanceof LocalDateTime d3) {
            this.setValue(d3);
        }
    }

    /**
     * 设置为当前时间
     */
    public void setNow() {
        this.setValue(LocalDateTime.now());
    }

    /**
     * 清除值
     */
    public void clearValue() {
        this.valueProperty().set(null);
    }

    /**
     * 获取年
     *
     * @return 年
     */
    public Integer yearValue() {
        LocalDateTime dateTime = this.getValue();
        return dateTime == null ? null : dateTime.getYear();
    }

    /**
     * 隐藏弹窗
     */
    public void hidePopup() {
        if (this.popup.isShowing()) {
            this.popup.hide();
        }
        this.popup = null;
        this.selector = null;
    }

    /**
     * 隐藏弹窗
     */
    public void showPopup() {
        // 初始化弹窗
        if (this.popup == null) {
            this.popup = new Popover(this.selector);
            this.popup.setAutoFix(true);
            this.popup.setAnimated(true);
            this.popup.setAutoHide(true);
            this.popup.setHideOnEscape(true);
            this.popup.setFadeInDuration(Duration.millis(600));
            this.popup.setFadeOutDuration(Duration.millis(600));
        }
        // 显示弹窗
        this.popup.show(this);
    }

    /**
     * 弹窗是否显示中
     *
     * @return 结果
     */
    public boolean isPopupShowing() {
        return this.popup != null && this.popup.isShowing();
    }

}
