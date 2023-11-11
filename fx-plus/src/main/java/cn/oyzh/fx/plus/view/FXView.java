//package cn.oyzh.fx.plus.view;
//
//import cn.hutool.core.util.ArrayUtil;
//import cn.hutool.core.util.StrUtil;
//import cn.oyzh.fx.common.thread.ExecutorUtil;
//import cn.oyzh.fx.common.util.SystemUtil;
//import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
//import cn.oyzh.fx.plus.theme.ThemeAdapter;
//import cn.oyzh.fx.plus.util.FXUtil;
//import cn.oyzh.fx.plus.util.IconUtil;
//import cn.oyzh.fx.plus.util.StyleUtil;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//import javafx.stage.Window;
//import jfxtras.styles.jmetro.JMetro;
//import jfxtras.styles.jmetro.JMetroStyleClass;
//import jfxtras.styles.jmetro.Style;
//import lombok.NonNull;
//import lombok.ToString;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.concurrent.atomic.AtomicReference;
//
//
///**
// * fx页面
// *
// * @author oyzh
// * @since 2021/8/19
// */
//@Deprecated
//@Slf4j
//@ToString
//public class FXView extends FXStage implements ThemeAdapter {
//
//    public FXView(@NonNull cn.oyzh.fx.plus.view.FXWindow window, Window owner) {
//        super();
//        this.initView(window, owner);
//    }
//
//    /**
//     * 创建stage
//     *
//     * @param window 窗口注解
//     * @return stage
//     */
//    private Stage createStage(@NonNull cn.oyzh.fx.plus.view.FXWindow window) {
//        AtomicReference<Stage> stage = new AtomicReference<>();
//        // 使用原始stage
//        if (window.usePrimary()) {
//            FXPrimaryStage primaryStage = FXViewUtil.getPrimaryStage();
//            if (primaryStage == null) {
//                throw new RuntimeException("PrimaryStage is null, please set PrimaryStage.");
//            }
//            if (primaryStage.getStage() == null) {
//                throw new RuntimeException("PrimaryStage.getStage() is null!");
//            }
//            if (primaryStage.isShowing()) {
//                throw new RuntimeException("PrimaryStage is showing.");
//            }
//            stage.set(primaryStage.getStage());
//            // 设置窗口样式
//            if (!primaryStage.isUsed()) {
//                stage.get().initStyle(window.stageStyle());
//            }
//        } else {// 创建stage
//            FXUtil.runWait(() -> stage.set(new Stage()));
//            // 初始化模态
//            stage.get().initModality(window.modality());
//            // 设置窗口样式
//            stage.get().initStyle(window.stageStyle());
//        }
//        // 初始化stage
//        stage.get().setTitle(window.title());
//        stage.get().setMaximized(window.maximized());
//        stage.get().setResizable(window.resizeable());
//
//        // 设置icon
//        if (ArrayUtil.isNotEmpty(window.iconUrls())) {
//            stage.get().getIcons().addAll(IconUtil.getIcons(window.iconUrls()));
//        }
//        return stage.get();
//    }
//
//    /**
//     * 初始化窗口
//     *
//     * @param window 窗口注解
//     * @param owner  父页面
//     */
//    private void initView(@NonNull FXWindow window, Window owner) {
//        // 创建stage
//        Stage stage = this.createStage(window);
//        // 初始化stage
//        super.initStage(stage);
//        // 初始化加载器
//        FXMLLoaderExt loader = new FXMLLoaderExt();
//        // 加载根节点
//        Parent root = loader.load(window.value());
//        if (root == null) {
//            log.error("load root fail.");
//            return;
//        }
//        // 设置controller
//        this.setProp("_controller", loader.getController());
//        // 设置scene
//        FXUtil.runWait(() -> stage.setScene(new Scene(root)));
//        // 初始化父窗口
//        if (!window.usePrimary() && owner != null) {
//            stage.initOwner(owner);
//        }
//        // 设置美化css类
//        root.getStyleClass().addAll(
//                JMetroStyleClass.BACKGROUND,
//                JMetroStyleClass.LIGHT_BUTTONS,
//                JMetroStyleClass.TABLE_GRID_LINES,
//                JMetroStyleClass.UNDERLINE_TAB_PANE,
//                JMetroStyleClass.ALTERNATING_ROW_COLORS
//        );
//        // 扩展风格
//        new JMetro(this.root(), Style.LIGHT);
//        // 加载自定义css文件
//        if (ArrayUtil.isNotEmpty(window.cssUrls())) {
//            root.getStylesheets().addAll(StyleUtil.split(window.cssUrls()));
//        }
//        // if (ThemeManager.Current_Theme == Theme.DARK) {
//        //     root.getStylesheets().add(FXStyle.EASY_FX_DARK);
//        // } else {
//        //     root.getStylesheets().add(FXStyle.EASY_FX);
//        // }
//        // 触发页面已加载事件
//        if (this.controller() instanceof FXViewListener viewListener) {
//            viewListener.onViewInitialize(this);
//            // 设置事件
//            this.setOnShown(viewListener::onViewShown);
//            this.setOnHiding(viewListener::onViewHiding);
//            this.setOnHidden(viewListener::onViewHidden);
//            this.setOnShowing(viewListener::onViewShowing);
//            this.setOnCloseRequest(viewListener::onViewCloseRequest);
//        }
//    }
//
//    @Override
//    protected void clear() {
//        super.clear();
//        // 取消eas监听
//        this.unHideOnEscape();
//        // 延迟gc
//        SystemUtil.gcLater();
//    }
//
//    /**
//     * 追加标题
//     *
//     * @param append 追加内容
//     */
//    public void appendTitle(String append) {
//        this.appendTitle(append, -1);
//    }
//
//    /**
//     * 追加标题
//     *
//     * @param append   追加内容
//     * @param liveTime 追加内容存活时间
//     */
//    public void appendTitle(String append, int liveTime) {
//        if (StrUtil.isNotBlank(append) && this.getStage() != null) {
//            String title = this.getProp("_title");
//            if (title == null) {
//                title = this.getStage().getTitle();
//                this.setProp("_title", title);
//            }
//            String newTitle = title + append;
//            this.setTitle(newTitle);
//            if (liveTime >= 0) {
//                ExecutorUtil.start(this::restoreTitle, liveTime);
//            }
//        }
//    }
//
//    /**
//     * 恢复标题
//     */
//    public void restoreTitle() {
//        this.setTitle(this.getProp("_title"));
//    }
//
//    /**
//     * 设置标题
//     *
//     * @param title 标题
//     */
//    public void setTitle(String title) {
//        if (title != null && this.getStage() != null) {
//            FXUtil.runWait(() -> this.getStage().setTitle(title));
//        }
//    }
//
//    /**
//     * 获取controller
//     *
//     * @return controller
//     */
//    public Object controller() {
//        return this.getProp("_controller");
//    }
//
//    /**
//     * 获取controller类
//     *
//     * @return controller类
//     */
//    public Class<?> controllerClass() {
//        Object controller = this.controller();
//        return controller == null ? null : controller.getClass();
//    }
//}
