//package cn.oyzh.fx.plus.information;
//
//import cn.oyzh.fx.plus.util.FXUtil;
//import javafx.scene.control.Dialog;
//import javafx.scene.control.TextInputDialog;
//import lombok.experimental.UtilityClass;
//
//import java.util.Optional;
//
///**
// * 对话窗口
// *
// * @author oyzh
// * @since 2020/10/28
// */
//@Deprecated
//@UtilityClass
//public class FXDialogUtil {
//
//    /**
//     * 提示窗口
//     *
//     * @param content 文本信息
//     */
//    public static void dialog(String content) {
//        dialog("提示信息", content);
//    }
//
//    /**
//     * 提示窗口
//     *
//     * @param title   标题
//     * @param content 文本信息
//     */
//    public static void dialog(String title, String content) {
//        String finalTitle = title == null ? "" : title;
//        String finalContent = content == null ? "" : content;
//        FXUtil.runWait(() -> {
//            Dialog<String> dialog = new Dialog<>();
//            dialog.setTitle(finalTitle);
//            dialog.setContentText(finalContent);
//            dialog.show();
//        });
//    }
//
//    /**
//     * 输入窗口
//     *
//     * @param title 标题
//     */
//    public static String prompt(String title) {
//        return prompt(title, null);
//    }
//
//    /**
//     * 输入窗口
//     *
//     * @param title    标题
//     * @param initText 初始值
//     */
//    public static String prompt(String title, String initText) {
//        title = title == null ? "提示信息" : title;
//        initText = initText == null ? "" : initText;
//        TextInputDialog dialog = new TextInputDialog(initText);
//        dialog.setTitle(title);
//        dialog.setGraphic(null);
//        dialog.setHeaderText(null);
//        Optional<String> result = dialog.showAndWait();
//        return result.orElse(null);
//    }
//}
