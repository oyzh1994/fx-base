//package cn.oyzh.fx.plus.information;
//
//import cn.oyzh.fx.common.Parser;
//import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
//import cn.oyzh.fx.plus.util.FXUtil;
//import cn.oyzh.fx.plus.view.FXView;
//import javafx.scene.paint.Color;
//import javafx.stage.Window;
//import lombok.NonNull;
//import lombok.experimental.UtilityClass;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.concurrent.atomic.AtomicReference;
//
//
///**
// * 消息提示工具类
// *
// * @author oyzh
// * @since 2023/01/04
// */
//@Deprecated
//@Slf4j
//@UtilityClass
//public class FXToastUtil {
//
//    /**
//     * ok图标
//     */
//    private static SVGGlyph OK;
//
//    /**
//     * 警告图标
//     */
//    private static SVGGlyph WARN;
//
//    /**
//     * 询问图标
//     */
//    private static SVGGlyph QUESTION;
//
//    /**
//     * ok图标
//     *
//     * @return ok图标
//     */
//    private static SVGGlyph okIcon() {
//        if (OK == null) {
//            OK = new SVGGlyph("/fx-plus/font/check-circle.svg", Color.GREEN);
//        }
//        return OK;
//    }
//
//    /**
//     * 警告图标
//     *
//     * @return 警告图标
//     */
//    private static SVGGlyph warnIcon() {
//        if (WARN == null) {
//            WARN = new SVGGlyph("/fx-plus/font/warning-circle.svg", Color.ORANGE);
//        }
//        return WARN;
//    }
//
//    /**
//     * 询问图标
//     *
//     * @return 询问图标
//     */
//    private static SVGGlyph questionIcon() {
//        if (QUESTION == null) {
//            QUESTION = new SVGGlyph("/fx-plus/font/question-circle.svg", Color.ORANGE);
//        }
//        return QUESTION;
//    }
//
//    /**
//     * 最近一次消息提示
//     */
//    private static FXToast last;
//
//    /**
//     * 构建控件
//     *
//     * @param msg   消息
//     * @param icon  图标
//     * @param owner 父窗口
//     * @return FXToast
//     */
//    public static FXToast build(@NonNull String msg, SVGGlyph icon, Window owner) {
//        AtomicReference<FXToast> reference = new AtomicReference<>();
//        FXUtil.runWait(() -> {
//            FXToast toast = new FXToast(msg);
//            toast.icon(icon)
//                    .textColor("#242424")
//                    .backgroundColor("#FFFAFA")
//                    .borderColor("#4F4F4F")
//                    .borderColor("#CCCCCC")
//                    .borderWidth(1)
//                    .borderStyle("solid");
//            toast.init(owner);
//            reference.set(toast);
//        });
//        return reference.get();
//    }
//
//    /**
//     * ok提示
//     *
//     * @param msg 消息
//     */
//    public static void ok(@NonNull String msg) {
//        ok(msg, (Window) null);
//    }
//
//    /**
//     * ok提示
//     *
//     * @param msg    消息
//     * @param fxView 父页面
//     */
//    public static void ok(@NonNull String msg, FXView fxView) {
//        Window owner = fxView == null ? null : fxView.getStage();
//        ok(msg, owner);
//    }
//
//    /**
//     * ok提示
//     *
//     * @param msg   消息
//     * @param owner 父窗口
//     */
//    public static void ok(@NonNull String msg, Window owner) {
//        show(build(msg, okIcon(), owner));
//    }
//
//    /**
//     * 异常提示
//     *
//     * @param ex 异常信息
//     */
//    public static void exception(@NonNull Exception ex, @NonNull Parser<Exception, String> parser) {
//        warn(parser.parse(ex), (Window) null);
//    }
//
//    /**
//     * 警告提示
//     *
//     * @param msg 消息
//     */
//    public static void warn(@NonNull String msg) {
//        warn(msg, (Window) null);
//    }
//
//    /**
//     * 警告提示
//     *
//     * @param msg    消息
//     * @param fxView 父页面
//     */
//    public static void warn(@NonNull String msg, FXView fxView) {
//        Window owner = fxView == null ? null : fxView.getStage();
//        warn(msg, owner);
//    }
//
//    /**
//     * 警告提示
//     *
//     * @param msg   消息
//     * @param owner 父窗口
//     */
//    public static void warn(@NonNull String msg, Window owner) {
//        show(build(msg, warnIcon(), owner));
//    }
//
//    /**
//     * 询问提示
//     *
//     * @param msg 消息
//     */
//    public static void question(@NonNull String msg) {
//        question(msg, null);
//    }
//
//    /**
//     * 询问提示
//     *
//     * @param msg   消息
//     * @param owner 父窗口
//     */
//    public static void question(@NonNull String msg, Window owner) {
//        show(build(msg, questionIcon(), owner));
//    }
//
//    /**
//     * 显示
//     *
//     * @param toast 消息提示
//     */
//    private static void show(FXToast toast) {
//        // 关闭旧组件
//        if (last != null && last.isShowing()) {
//            last.close();
//        }
//        // 设置最后显示的图标
//        last = toast;
//        // 关闭时，检查是否最后显示的提示
//        if (toast != null) {
//            toast.setOnHidden((e) -> {
//                if (toast == last) {
//                    last = null;
//                }
//            });
//            // 显示组件
//            FXUtil.runLater(toast::show);
//        }
//    }
//}
