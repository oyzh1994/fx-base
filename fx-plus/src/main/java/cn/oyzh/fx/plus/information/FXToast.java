package cn.oyzh.fx.plus.information;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.thread.ExecutorUtil;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.FontUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;


/**
 * 消息提示
 *
 * @author oyzh
 * @since 2020/10/19
 */
@Deprecated
@Slf4j
@Accessors(fluent = true, chain = true)
public class FXToast extends Stage {

    /**
     * 消息
     */
    @Getter
    private final String msg;

    /**
     * 持续时间
     */
    @Getter
    @Setter
    private int duration;

    /**
     * 字体
     */
    @Setter
    @Getter
    private String textFont;

    /**
     * 字体大小
     */
    @Getter
    @Setter
    private double textSize;

    /**
     * 字体颜色
     */
    @Setter
    @Getter
    private String textColor;

    /**
     * 图标
     */
    @Getter
    @Setter
    private SVGGlyph icon;

    /**
     * 边框颜色
     */
    @Setter
    @Getter
    private String borderColor;

    /**
     * 边框宽度
     */
    @Setter
    @Getter
    private double borderWidth;

    /**
     * 边框样式
     */
    @Setter
    @Getter
    private String borderStyle;

    /**
     * 边框圆角
     */
    @Setter
    @Getter
    private double borderRadius;

    /**
     * 背景颜色
     */
    @Setter
    @Getter
    private String backgroundColor;

    public FXToast(@NonNull String msg) {
        this.msg = msg;
    }

    /**
     * 重置为默认参数
     */
    protected void resetDefault() {
        if (this.borderWidth <= 0) {
            this.borderWidth = 0;
        }
        if (this.borderRadius <= 0) {
            this.borderRadius = 3;
        }
        if (this.duration <= 0) {
            this.duration = 1500;
        }
        if (this.textSize <= 0) {
            this.textSize = 14;
        }
        if (this.textFont == null) {
            this.textFont = "System Regular";
        }
        if (this.textColor == null) {
            this.textColor = "#FFFFFF";
        }
        if (this.borderStyle == null) {
            this.borderStyle = "none";
        }
        if (this.backgroundColor == null) {
            this.backgroundColor = "#000000";
        }
    }

    /**
     * 初始化
     */
    public void init(Window owner) {
        this.resetDefault();
        HBox box = new HBox();
        // 设置样式
        StringBuilder style = new StringBuilder();
        if (this.borderWidth > 0 && !"none".equalsIgnoreCase(this.borderStyle)) {
            style.append(";-fx-border-style:").append(this.borderStyle);
            style.append(";-fx-border-color:").append(this.borderColor);
            style.append(";-fx-border-width:").append(this.borderWidth);
            style.append(";-fx-border-radius:").append(this.borderRadius);
        }
        style.append(";-fx-background-color:").append(this.backgroundColor);
        style.append(";-fx-background-radius:").append(this.borderRadius);
        if (StrUtil.isNotBlank(box.getStyle())) {
            style.append(";").append(box.getStyle());
        }
        box.setStyle(style.substring(1));

        // 设置间距等
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(3));

        // 字符度量
        FontMetrics fontMetrics = FontUtil.fontMetrics(this.textFont, (int) this.textSize);
        // 节点的宽度
        double boxWidth = fontMetrics.stringWidth(this.msg);
        // 设置图标
        if (this.icon != null) {
            this.icon.setSize(fontMetrics.getHeight() * 0.95);
            box.getChildren().add(this.icon);
            boxWidth += this.icon.getWidth();
        }

        // 设置文字
        Font font = Font.font(this.textFont, this.textSize);
        Text text = new Text();
        text.setFont(font);
        text.setText(this.msg);
        text.setFill(Paint.valueOf(this.textColor));
        HBox.setMargin(text, new Insets(0, 0, 0, 3));
        box.getChildren().add(text);

        // 设置宽度
        box.setPrefWidth(boxWidth + 12);

        // 初始化面板
        Scene scene = new Scene(box);
        scene.setCursor(Cursor.NONE);

        // 初始化stage
        this.initOwner(owner);
        this.setOpacity(0.9);
        this.setAlwaysOnTop(true);
        this.initStyle(StageStyle.UNDECORATED);
        this.setScene(scene);
        this.setOnShown(e -> {
            // 计算位置
            FXUtil.computePos(owner, this);
            // 执行延迟关闭
            ExecutorUtil.start(this::close, this.duration);
        });
    }

    @Override
    public void hide() {
        try {
            FXUtil.runLater(super::hide);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
