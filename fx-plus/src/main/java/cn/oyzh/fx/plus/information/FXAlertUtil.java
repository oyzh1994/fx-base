//package cn.oyzh.fx.plus.information;
//
//import cn.oyzh.fx.common.Parser;
//import cn.oyzh.fx.plus.util.FXUtil;
//import javafx.scene.control.Alert;
//import javafx.scene.control.ButtonType;
//import lombok.NonNull;
//import lombok.experimental.UtilityClass;
//
//import java.util.Optional;
//import java.util.concurrent.atomic.AtomicReference;
//
///**
// * 弹出窗口工具类
// *
// * @author oyzh
// * @since 2022/02/08
// */
//@Deprecated
//@UtilityClass
//public class FXAlertUtil {
//
//    /**
//     * 确认窗口
//     *
//     * @param content 文本信息
//     */
//    public static boolean confirm(String content) {
//        return confirm("提示信息", content);
//    }
//
//    /**
//     * 确认窗口
//     *
//     * @param title   标题信息
//     * @param content 文本信息
//     */
//    public static boolean confirm(@NonNull String title, String content) {
//        content = content == null ? "" : content;
//        ButtonType button1 = new ButtonType("确定");
//        ButtonType button2 = new ButtonType("取消");
//        AtomicReference<Alert> reference = new AtomicReference<>();
//        String finalContent = content;
//        FXUtil.runWait(() -> reference.set(new Alert(Alert.AlertType.CONFIRMATION, finalContent, button1, button2)));
//        reference.get().setTitle(title);
//        reference.get().setHeaderText(null);
//        Optional<ButtonType> optional = reference.get().showAndWait();
//        return optional.map(b -> b.equals(button1)).orElse(false);
//    }
//
//    /**
//     * 警告窗口
//     *
//     * @param content 文本信息
//     */
//    public static void warn(@NonNull String content) {
//        alert(Alert.AlertType.WARNING, "提示信息", null, content);
//    }
//
//    /**
//     * 警告窗口
//     *
//     * @param ex 异常信息
//     */
//    public static void warn(@NonNull Exception ex, @NonNull Parser<Exception, String> parser) {
//        alert(Alert.AlertType.WARNING, "提示信息", null, parser.parse(ex));
//    }
//
//    /**
//     * 信息窗口
//     *
//     * @param content 文本信息
//     */
//    public static void info(@NonNull String content) {
//        alert(Alert.AlertType.INFORMATION, "提示信息", null, content);
//    }
//
//    /**
//     * 错误窗口
//     *
//     * @param content 文本信息
//     */
//    public static void error(@NonNull String content) {
//        alert(Alert.AlertType.ERROR, "提示信息", null, content);
//    }
//
//    /**
//     * 默认窗口
//     *
//     * @param content 文本信息
//     */
//    public static void none(@NonNull String content) {
//        alert(Alert.AlertType.NONE, "提示信息", null, content);
//    }
//
//    /**
//     * 窗口控件
//     *
//     * @param type    类型
//     * @param title   标题
//     * @param header  提示头
//     * @param content 文本信息
//     */
//    public static void alert(@NonNull Alert.AlertType type, @NonNull String title, String header, String content) {
//        FXUtil.runLater(() -> {
//            Alert alert = new Alert(type);
//            alert.setTitle(title);
//            alert.setHeaderText(header);
//            alert.setContentText(content);
//            alert.show();
//        });
//    }
//
//    /**
//     * 窗口控件
//     *
//     * @param type        类型
//     * @param title       标题
//     * @param header      提示头
//     * @param content     文本信息
//     * @param buttonTypes 按钮
//     * @return 点击的按钮
//     */
//    public static ButtonType alert(@NonNull Alert.AlertType type, @NonNull String title, String header, String content, ButtonType... buttonTypes) {
//        Alert alert = new Alert(type, content, buttonTypes);
//        alert.setTitle(title);
//        alert.setHeaderText(header);
//        alert.setContentText(content);
//        Optional<ButtonType> optional = alert.showAndWait();
//        return optional.orElse(null);
//    }
//}
