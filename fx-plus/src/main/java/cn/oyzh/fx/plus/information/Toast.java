package cn.oyzh.fx.plus.information;

import cn.oyzh.fx.common.thread.TaskManager;
import cn.oyzh.fx.plus.controls.popup.FXPopup;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.stage.StageManager;
import cn.oyzh.fx.plus.stage.StageWrapper;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.awt.*;


/**
 * 消息提示
 *
 * @author oyzh
 * @since 2020/10/19
 */
@Accessors(fluent = true, chain = true)
public class Toast {

    /**
     * 消息
     */
    @Getter
    protected String msg;

    /**
     * 持续时间
     */
    @Getter
    @Setter
    protected int duration = 1500;

    /**
     * 字体
     */
    @Setter
    @Getter
    protected Font font = Font.getDefault();

    /**
     * 图标
     */
    @Getter
    @Setter
    protected SVGGlyph icon;

    /**
     * 边框
     */
    @Setter
    @Getter
    protected Border border;

    /**
     * 文本颜色
     */
    @Setter
    @Getter
    protected Paint textFill;

    /**
     * 背景
     */
    @Setter
    @Getter
    protected Background background;

    /**
     * 当前窗口
     */
    protected Window window;

    public Toast(@NonNull String msg) {
        this.msg = msg;
    }

    /**
     * 重置为默认参数
     */
    protected void resetDefault() {
        if (this.duration <= 0) {
            this.duration = 1500;
        }
    }

    /**
     * 显示组件
     *
     * @param owner 父窗口
     */
    public void show(Window owner) {
        // 重置默认参数
        this.resetDefault();
        // 创建组件
        HBox box = new HBox();
        // 设置间距等
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(3));
        // 边框
        if (this.border != null) {
            box.setBorder(this.border);
        }
        // 背景
        if (this.background != null) {
            box.setBackground(this.background);
        }
        // 字符度量
        FontMetrics fontMetrics = FontUtil.fontMetrics(this.font);
        // 节点的宽度
        double boxWidth = fontMetrics.stringWidth(this.msg);
        // 设置图标
        if (this.icon != null) {
            double iconSize = fontMetrics.getHeight() * 0.95;
            this.icon.setSize(iconSize);
            boxWidth += iconSize;
            box.getChildren().add(this.icon);
        }
        // 设置文字
        Text text = new Text();
        text.setText(this.msg);
        text.setFont(this.font);
        // 设置文字颜色
        if (this.textFill != null) {
            text.setFill(this.textFill);
        }
        // 设置边距
        HBox.setMargin(text, new Insets(0, 0, 0, 3));
        box.getChildren().add(text);
        // 设置宽度
        box.setPrefWidth(boxWidth + 20);
        // 初始化面板
        if (owner == null) {// Stage
            StageWrapper wrapper = StageManager.newStage(null);
            Stage stage = wrapper.stage();
            this.window = stage;
            Scene scene = new Scene(box);
            scene.setCursor(Cursor.NONE);
            stage.setScene(scene);
            stage.setAlwaysOnTop(true);
            stage.initStyle(StageStyle.UNDECORATED);
        } else {// Popup
            Popup popup = new FXPopup();
            this.window = popup;
            box.setCursor(Cursor.NONE);
            popup.setAutoFix(true);
            popup.setAutoHide(true);
            popup.getContent().setAll(box);
        }
        // 设置透明度
        this.window.setOpacity(0.9);
        // 设置自动隐藏，位置定位
        if (owner != null || this.duration > 0) {
            this.window.setOnShown(e -> {
                // 计算位置
                FXUtil.computePos(owner, this.window);
                // 执行延迟关闭
                TaskManager.startDelay(this::close, this.duration);
            });
        }
        // 显示窗口
        if (this.window instanceof StageWrapper wrapper) {
            wrapper.changeTheme(ThemeManager.currentTheme());
            wrapper.display();
        } else if (this.window instanceof FXPopup popup) {
            popup.changeTheme(ThemeManager.currentTheme());
            popup.show(owner);
        } else if (this.window instanceof Stage stage) {
            stage.show();
        } else if (this.window instanceof Popup popup) {
            popup.show(owner);
        }
    }

    /**
     * 关闭组件
     */
    public void close() {
        try {
            this.msg = null;
            this.icon = null;
            this.font = null;
            this.border = null;
            this.textFill = null;
            this.background = null;
            if (this.window != null) {
                FXUtil.runLater(this.window::hide);
            }
            this.window = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 是否显示中
     *
     * @return 结果
     */
    public boolean isShowing() {
        return this.window != null && this.window.isShowing();
    }

    /**
     * 设置隐藏事件
     *
     * @param onHidden 隐藏事件
     */
    public void setOnHidden(EventHandler<WindowEvent> onHidden) {
        if (this.window != null && onHidden != null) {
            this.window.setOnHidden(onHidden);
        }
    }
}
