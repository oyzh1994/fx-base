package cn.oyzh.fx.gui.test;

import cn.oyzh.fx.plus.util.FXUtil;
import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;

public class LabeledComboBox extends VBox {
    private String key;
    private String textColor = "#838186";
    private String textPadding = "0 3 0 3";
    private int textSize = 12;
    private String backgroundColor = "null";
    private String borderColor = "#838186";
    private double borderRadius = 5;
    private double borderWidth = 1;
    private ComboBox<String> comboBox;
    private Label label;
    private static final int LABEL_START_X = 8;

    public LabeledComboBox(String key) {
        super();
        this.key = key;
        initView();
    }

    private void initView() {
        //创建视图
        comboBox = new ComboBox<>();
        label = new Label(key);
        //设置样式
        this.updateComboBoxStyle();
        this.updateLabelStyle();
        //添加到布局
        this.getChildren().addAll(comboBox, label);
        //设置文字位置
        label.setTranslateX(LABEL_START_X);
        label.setTranslateY(-(label.getHeight() / 2));
        //设置属性监听器
        this.widthProperty().addListener((o, n, n1) -> update_view());
        this.heightProperty().addListener((o, n, n1) -> update_view());
        comboBox.focusedProperty().addListener((o, n, isFocused) -> {
            if (isFocused) {
                this.setBorderColor("#3282E0");
                this.setTextColor("#3282E0");
            } else {
                this.setBorderColor("#838186");
                this.setTextColor("#838186");
            }
        });
    }

    public void setWidth(double width) {
        super.setWidth(width);
        this.comboBox.setPrefWidth(width);
    }

    public void setHeight(double height) {
        super.setHeight(height);
        this.comboBox.setPrefHeight(height);
    }

    public void setItems(List<String> items) {
        this.comboBox.setItems(FXCollections.observableArrayList(items));
    }

    public void setSelectedItem(String item) {
        this.comboBox.getSelectionModel().select(item);
    }

    public void setKey(String key) {
        this.key = key;
        this.label.setText(key);
    }

    public void setBorderWidth(double borderWidth) {
        this.borderWidth = borderWidth;
        updateComboBoxStyle();
    }

    public void setBorderRadius(double borderRadius) {
        this.borderRadius = borderRadius;
        updateComboBoxStyle();
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
        updateComboBoxStyle();
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        updateComboBoxStyle();
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        updateLabelStyle();
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
        updateLabelStyle();
    }

    public void setTextPadding(String textPadding) {
        this.textPadding = textPadding;
        updateLabelStyle();
    }

    private void updateLabelStyle() {
        label.setStyle(
                "-fx-text-fill : " + textColor + ";\n" +
                        "-fx-padding   : " + textPadding + ";\n" +
                        "-fx-font-size : " + textSize + ";\n");
        label.setFont(new Font(textSize));
    }

    private void updateComboBoxStyle() {
        comboBox.setStyle(
                "-fx-background-color : " + backgroundColor + ";\n" +
                        "-fx-border-color     : " + borderColor + ";\n" +
                        "-fx-border-radius    : " + borderRadius + ";\n" +
                        "-fx-border-width     : " + borderWidth + ";\n");
    }

    public void update_view() {
        FXUtil.runLater(() -> {
            //计算文字宽高
            Bounds labelBounds = measureTextBounds(label.getFont(), label.getText());
            //更新文字位置
            label.setTranslateY(-(labelBounds.getHeight() / 2));
            //更新擦除区域
            Rectangle mask = new Rectangle(LABEL_START_X, 0, LABEL_START_X + labelBounds.getWidth() + label.getPadding().getRight() - 3, 2);
            Rectangle node_rect = new Rectangle(0, 0, comboBox.getPrefWidth(), comboBox.getPrefHeight());
            comboBox.setClip(Shape.subtract(node_rect, mask));
        });
    }

    public static Bounds measureTextBounds(Font font, String text) {
        Text textNode = new Text(text);
        textNode.setFont(font);
        return textNode.getBoundsInLocal();
    }
}
