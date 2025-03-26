package cn.oyzh.fx.plus.information;

import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.TooltipUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Window;

import javax.swing.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * 消息盒子
 *
 * @author oyzh
 * @since 2023/10/24
 */

public class MessageBox {

    /**
     * 异常解析器
     */
    private static Function<Throwable, String> Exception_Parser;

    /**
     * 注册异常解析器
     *
     * @param exceptionParser 异常解析器
     */
    public static void registerExceptionParser(Function<Throwable, String> exceptionParser) {
        Exception_Parser = exceptionParser;
    }

    /**
     * 确认窗口
     *
     * @param content 文本信息
     */
    public static boolean confirm(String content) {
        return confirm(I18nHelper.tips(), content);
    }

    /**
     * 确认窗口
     *
     * @param title   标题信息
     * @param content 文本信息
     */
    public static boolean confirm(String title, String content) {
        String finalContent = content == null ? "" : content;
        AtomicReference<Boolean> result = new AtomicReference<>();
        FXUtil.runWait(() -> {
            ButtonType button1 = new ButtonType(I18nHelper.ok());
            ButtonType button2 = new ButtonType(I18nHelper.cancel());
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, finalContent, button1, button2);
            alert.setTitle(title);
            alert.setHeaderText(null);
            Optional<ButtonType> optional = alert.showAndWait();
            result.set(optional.map(b -> b.equals(button1)).orElse(false));
        });
        return result.get();
    }

    /**
     * 警告窗口
     *
     * @param content 文本信息
     */
    public static void warn(String content) {
        alert(Alert.AlertType.WARNING, I18nHelper.tips(), null, content);
    }

    /**
     * 警告窗口
     *
     * @param content 文本信息
     * @param owner   父窗口
     */
    public static void warn(String content, Window owner) {
        alert(Alert.AlertType.WARNING, I18nHelper.tips(), null, content, owner);
    }

    /**
     * 警告窗口
     *
     * @param content 文本信息
     * @param adapter 父窗口
     */
    public static void warn(String content, StageAdapter adapter) {
        alert(Alert.AlertType.WARNING, I18nHelper.tips(), null, content, adapter.stage());
    }

    /**
     * 异常窗口
     *
     * @param ex 异常信息
     */
    public static void exception(Throwable ex) {
        ex.printStackTrace();
        exception(ex, null);
    }

    /**
     * 异常窗口
     *
     * @param ex    异常信息
     * @param title 标题
     */
    public static void exception(Throwable ex, String title) {
        String err;
        if (Exception_Parser != null) {
            err = Exception_Parser.apply(ex);
        } else {
            err = ex.getMessage();
        }
        title = title == null ? I18nHelper.tips() : title;
        alert(Alert.AlertType.WARNING, title, null, err);
    }

    /**
     * 信息窗口
     *
     * @param content 文本信息
     */
    public static void info(String content) {
        alert(Alert.AlertType.INFORMATION, I18nHelper.tips(), null, content);
    }

    /**
     * 错误窗口
     *
     * @param content 文本信息
     */
    public static void error(String content) {
        alert(Alert.AlertType.ERROR, I18nHelper.tips(), null, content);
    }

    /**
     * 默认窗口
     *
     * @param content 文本信息
     */
    public static void none(String content) {
        alert(Alert.AlertType.NONE, I18nHelper.tips(), null, content);
    }

    /**
     * 窗口控件
     *
     * @param type    类型
     * @param title   标题
     * @param header  提示头
     * @param content 文本信息
     */
    public static void alert(Alert.AlertType type, String title, String header, String content) {
        alert(type, title, header, content, StageManager.getFrontWindow());
    }

    /**
     * 窗口控件
     *
     * @param type    类型
     * @param title   标题
     * @param header  提示头
     * @param content 文本信息
     * @param owner   父窗口
     */
    public static void alert(Alert.AlertType type, String title, String header, String content, Window owner) {
        // 使用fx消息框
        if (FXUtil.isInitialized()) {
            FXUtil.runWait(() -> {
                Alert alert = new Alert(type);
                alert.setTitle(title);
                alert.setHeaderText(header);
                alert.setContentText(content);
                alert.initOwner(owner);
                alert.showAndWait();
            });
        } else {// 使用swing消息框
            int msgType = switch (type) {
                case NONE -> JOptionPane.NO_OPTION;
                case INFORMATION -> JOptionPane.INFORMATION_MESSAGE;
                case WARNING -> JOptionPane.WARNING_MESSAGE;
                case CONFIRMATION -> JOptionPane.YES_NO_OPTION;
                case ERROR -> JOptionPane.ERROR_MESSAGE;
            };
            JOptionPane.showMessageDialog(null, content, title, msgType);
        }
    }

    /**
     * 窗口控件
     *
     * @param type        类型
     * @param title       标题
     * @param header      提示头
     * @param content     文本信息
     * @param buttonTypes 按钮
     * @return 点击的按钮
     */
    public static ButtonType alert(Alert.AlertType type, String title, String header, String content, ButtonType... buttonTypes) {
        Alert alert = new Alert(type, content, buttonTypes);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        Optional<ButtonType> optional = alert.showAndWait();
        return optional.orElse(null);
    }

    /**
     * 提示窗口
     *
     * @param content 文本信息
     */
    public static void dialog(String content) {
        dialog(I18nHelper.tips(), content);
    }

    /**
     * 提示窗口
     *
     * @param title   标题
     * @param content 文本信息
     */
    public static void dialog(String title, String content) {
        String finalTitle = title == null ? "" : title;
        String finalContent = content == null ? "" : content;
        FXUtil.runWait(() -> {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle(finalTitle);
            dialog.setContentText(finalContent);
            dialog.show();
        });
    }

    /**
     * 输入窗口
     *
     * @param title 标题
     */
    public static String prompt(String title) {
        return prompt(title, null);
    }

    /**
     * 输入窗口
     *
     * @param title    标题
     * @param initText 初始值
     */
    public static String prompt(String title, String initText) {
        title = title == null ? I18nHelper.tips() : title;
        initText = initText == null ? "" : initText;
        TextInputDialog dialog = new TextInputDialog(initText);
        dialog.setTitle(title);
        dialog.setGraphic(null);
        dialog.setHeaderText(null);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * 提示消息
     *
     * @param tipMsg 提示消息
     * @param node   节点
     * @return FXTooltip
     */
    public static TooltipExt tipMsg(String tipMsg, Node node) {
        return tipMsg(tipMsg, node, 1500);
    }

    /**
     * 提示消息
     *
     * @param tipMsg   提示消息
     * @param node     节点
     * @param liveTime 存活时间
     * @return FXTooltip
     */
    public static TooltipExt tipMsg(String tipMsg, Node node, Integer liveTime) {
        TooltipExt tooltip = new TooltipExt();
        try {
            // 隐藏旧工具条
            TooltipExt old = (TooltipExt) node.getProperties().remove("tipTool");
            if (old != null) {
                old.hide();
            }
            // 设置标志位
            node.getProperties().put("tipTool", tooltip);
            // 获取字体
            Font font = FontUtil.getFont(node);
            // 初始化提示条
            TooltipUtil.initStyle(tooltip);
            // 安装提示条
            Tooltip.install(node, tooltip);
            // 设置消息内容
            tooltip.setText(tipMsg);
            // 设置字体
            tooltip.setFont(font);
            // 自动隐藏
            if (liveTime != null) {
                tooltip.setOnShown(e -> ExecutorUtil.start(tooltip::hide, liveTime));
            }
            // 提示条隐藏事件
            tooltip.setOnHidden(windowEvent -> {
                tooltip.hide();
                node.getProperties().remove("tipTool");
                Tooltip.uninstall(node, tooltip);
            });
            FXUtil.runLater(() -> {
                // 使组件获取焦点
                node.requestFocus();
                // 显示提示条
                tooltip.show(node);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return tooltip;
    }

    /**
     * ok提示
     *
     * @param msg 消息
     */
    public static void okToast(String msg) {
        okToast(msg, StageManager.getFrontWindow());
//        okToast(msg, null);
    }

    /**
     * ok提示
     *
     * @param msg   消息
     * @param owner 父窗口
     */
    public static void okToast(String msg, Window owner) {
        showToast(msg, new SVGGlyph("/fx-svg/check-circle.svg", Color.GREEN), owner);
    }

    /**
     * 警告提示
     *
     * @param msg 消息
     */
    public static void warnToast(String msg) {
        warnToast(msg, null);
    }

    /**
     * 警告提示
     *
     * @param msg   消息
     * @param owner 父窗口
     */
    public static void warnToast(String msg, Window owner) {
        showToast(msg, new SVGGlyph("/fx-svg/warning-circle.svg", Color.ORANGE), owner);
    }

    /**
     * 询问提示
     *
     * @param msg 消息
     */
    public static void questionToast(String msg) {
        questionToast(msg, null);
    }

    /**
     * 询问提示
     *
     * @param msg   消息
     * @param owner 父窗口
     */
    public static void questionToast(String msg, Window owner) {
        showToast(msg, new SVGGlyph("/fx-svg/question-circle.svg", Color.ORANGE), owner);
    }

    /**
     * 显示提示
     *
     * @param msg   消息
     * @param icon  图标
     * @param owner 父窗口
     */
    private static void showToast(String msg, SVGGlyph icon, Window owner) {
        Toast toast = new Toast(msg);
        // 边框
        Border border = new Border(new BorderStroke(Color.valueOf("#CCCCCC"), BorderStrokeStyle.SOLID, new CornerRadii(3), BorderStroke.THIN));
        // 背景
        Background background = new Background(new BackgroundFill(Color.valueOf("#FFFAFA"), new CornerRadii(3), null));
        // 设置参数
        toast.setIcon(icon);
        toast.setBorder(border);
        toast.setTextFill(Color.valueOf("#242424"));
        toast.setBackground(background);
        // 显示组件
        FXUtil.runLater(() -> toast.show(owner));
    }
}
