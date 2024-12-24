package cn.oyzh.fx.plus.titlebar;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.image.FXImageView;
import cn.oyzh.fx.plus.controls.pane.FlexPane;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.util.IconUtil;
import cn.oyzh.fx.plus.util.MouseUtil;
import cn.oyzh.fx.plus.util.NodeUtil;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 标题栏
 *
 * @author oyzh
 * @since 2024/12/14
 */
public class TitleBar extends FlexPane {

    /**
     * 是否有内容
     */
    @Getter
    private boolean hasContent;

    /**
     * 原始x
     */
    private AtomicReference<Double> originalX;

    /**
     * 原始y
     */
    private AtomicReference<Double> originalY;

    protected void setOriginalX(double x) {
        if (this.originalX == null) {
            this.originalX = new AtomicReference<>();
        }
        this.originalX.set(x);
    }

    protected Double getOriginalX() {
        return this.originalX == null ? null : this.originalX.get();
    }

    protected void setOriginalY(double y) {
        if (this.originalY == null) {
            this.originalY = new AtomicReference<>();
        }
        this.originalY.set(y);
    }

    protected Double getOriginalY() {
        return this.originalY == null ? null : this.originalY.get();
    }

    public TitleBar(TitleBarConfig config) {
        this.init(config);
    }

    @Override
    public void resize(double width, double height) {
        super.resize(width, height);
        this.updateNodeLocation();
    }

    /**
     * 初始化
     *
     * @param config 标题栏配置
     */
    protected void init(TitleBarConfig config) {
        this.setId("titleBar");
        this.realHeight(30);
        this.setMaxHeight(30);
        this.setFlexWidth("100%");
        this.setBorder(null);
        this.setPadding(Insets.EMPTY);
        this.initNodes(config);
        this.initEvents();
    }

    /**
     * 初始化组件
     *
     * @param config 标题栏配置类
     */
    protected void initNodes(TitleBarConfig config) {
        List<Node> nodes = new ArrayList<>();

        // 图标
        if (config.getIcon() != null) {
            Image image = IconUtil.getIcon(config.getIcon());
            if (image != null) {
                FXImageView imageView = new FXImageView(image, 16);
                imageView.setId("icon");
                nodes.add(imageView);
            } else {
                JulLog.warn("load icon failed");
            }
        }

        // 置顶
        if (config.isAlwaysOnTop()) {
            TitleBarTopSVGGlyph alwaysOnTop = new TitleBarTopSVGGlyph("14");
            alwaysOnTop.setId("alwaysOnTop");
            alwaysOnTop.setOnMousePrimaryClicked(e -> this.alwaysOnTop());
            nodes.add(alwaysOnTop);
        }

        // 全屏
        if (config.isFullScreen()) {
            TitleBarFullScreenSVGPane fullScreen = new TitleBarFullScreenSVGPane("14");
            fullScreen.setId("fullScreen");
            fullScreen.setOnMousePrimaryClicked(e -> this.fullScreen());
            nodes.add(fullScreen);
        }

        // 最小化
        TitleBarMinimumSVGGlyph minimum = new TitleBarMinimumSVGGlyph("14");
        minimum.setId("minimum");
        if (config.isMinimum()) {
            minimum.setOnMousePrimaryClicked(e -> this.minimum());
        } else {
            minimum.disable();
        }
        nodes.add(minimum);

        // 最大化
        TitleBarMaximumSVGPane maximum = new TitleBarMaximumSVGPane("14");
        maximum.setId("maximum");
        if (config.isMaximum()) {
            maximum.setOnMousePrimaryClicked(e -> this.maximum());
        } else {
            maximum.disable();
        }
        nodes.add(maximum);

        // 关闭
        TitleBarCloseSVGGlyph close = new TitleBarCloseSVGGlyph("14");
        close.setId("close");
        if (config.isClose()) {
            close.setOnMousePrimaryClicked(e -> this.close());
        } else {
            close.disable();
        }
        nodes.add(close);
        // 设置子节点
        this.setChild(nodes);
    }

    /**
     * 加载内容
     *
     * @param fxmlUrl fxml地址
     */
    public void loadContent(String fxmlUrl) {
        // 初始化加载器
        FXMLLoaderExt loader = new FXMLLoaderExt();
        // 加载根节点
        Parent header = loader.load(fxmlUrl);
        // 设置内容
        this.setContent(header);
    }

    /**
     * 设置内容
     *
     * @param content 内容
     */
    public void setContent(Node content) {
        Node node = this.lookup("#content");
        this.hasContent = content != null;
        if (content != null) {
            content.setId("content");
            Node title = this.lookup("#title");
            // 覆盖旧内容组件或者标题组件
            if (node != null || title != null) {
                this.setChild(1, content);
            } else {// 添加组件
                this.addChild(1, content);
            }
        } else if (node != null) {// 移除内容组件
            this.removeChild(node);
        }
        // 更新节点位置
        this.updateNodeLocation();
    }

    /**
     * 初始化标题
     */
    public void initTitle() {
        Stage stage = this.stage();
        if (stage == null || this.hasContent) {
            return;
        }
        FXText text = (FXText) this.lookup("#title");
        if (stage.getTitle() != null) {
            String title = stage.getTitle();
            // 创建
            if (text == null) {
                text = new FXText(title);
                text.setFontSize(12);
                text.setId("title");
                this.addChild(1, text);
            } else if (StringUtil.equals(text.getText(), title)) {// 更新
                text.setText(title);
            }
        } else if (text != null) {// 移除
            this.removeChild(text);
        }
    }

    /**
     * 更新节点位置
     */
    protected void updateNodeLocation() {
        double width = this.realWidth();
        double height = this.realHeight();
        double layoutX = 10;
        double actionW = 0;
        Node close = null;
        Node maximum = null;
        Node minimum = null;
        Node fullScreen = null;
        Node alwaysOnTop = null;
        for (Node child : this.getChildren()) {
            if ("icon".equals(child.getId())) {
                double nWidth = NodeUtil.getWidth(child);
                double nHeight = NodeUtil.getHeight(child);
                child.setLayoutY((height - nHeight) / 2);
                child.setLayoutX(layoutX);
                layoutX += nWidth + 5;
                continue;
            }
            if ("content".equals(child.getId())) {
                double nWidth = NodeUtil.getWidth(child);
                double nHeight = NodeUtil.getHeight(child);
                child.setLayoutY((height - nHeight) / 2);
                child.setLayoutX(layoutX);
                layoutX += nWidth;
                continue;
            }
            if ("title".equals(child.getId())) {
                FXText text = (FXText) child;
                double nWidth = FontUtil.stringWidth(text.getText());
                child.setLayoutY(20);
                child.setLayoutX(layoutX);
                layoutX += nWidth + 5;
                continue;
            }
            if ("fullScreen".equals(child.getId())) {
                fullScreen = child;
                double nHeight = NodeUtil.getHeight(child);
                child.setLayoutY((height - nHeight) / 2);
                continue;
            }
            if ("alwaysOnTop".equals(child.getId())) {
                alwaysOnTop = child;
                double nHeight = NodeUtil.getHeight(child);
                child.setLayoutY((height - nHeight) / 2);
                continue;
            }
            if ("minimum".equals(child.getId())) {
                minimum = child;
                double nHeight = NodeUtil.getHeight(child);
                child.setLayoutY((height - nHeight) / 2);
                continue;
            }
            if ("maximum".equals(child.getId())) {
                maximum = child;
                double nHeight = NodeUtil.getHeight(child);
                child.setLayoutY((height - nHeight) / 2);
                continue;
            }
            if ("close".equals(child.getId())) {
                close = child;
                double nHeight = NodeUtil.getHeight(child);
                child.setLayoutY((height - nHeight) / 2);
            }
        }

        // 关闭按钮处理
        if (close != null) {
            double nWidth = NodeUtil.getWidth(close);
            close.setLayoutX(width - nWidth - 10);
            actionW = nWidth + 10;
        }
        // 最大化按钮处理
        if (maximum != null) {
            double nWidth = NodeUtil.getWidth(maximum);
            maximum.setLayoutX(width - actionW - nWidth - 10);
            actionW = actionW + nWidth + 10;
        }
        // 最小化按钮处理
        if (minimum != null) {
            double nWidth = NodeUtil.getWidth(minimum);
            minimum.setLayoutX(width - actionW - nWidth - 10);
            actionW = actionW + nWidth + 10;
        }
        // 全屏按钮处理
        if (fullScreen != null) {
            double nWidth = NodeUtil.getWidth(fullScreen);
            fullScreen.setLayoutX(width - actionW - nWidth - 10);
            actionW = actionW + nWidth + 10;
        }
        // 置顶按钮处理
        if (alwaysOnTop != null) {
            double nWidth = NodeUtil.getWidth(alwaysOnTop);
            alwaysOnTop.setLayoutX(width - actionW - nWidth - 10);
        }
    }

    /**
     * 初始化事件
     */
    protected void initEvents() {
        // 鼠标按下事件
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (event.getClickCount() == 1) {
                    // 记录位置
                    if (this.checkNotInvalid()) {
                        this.doRecordLocation();
                    }
                } else if (event.getClickCount() == 2) {
                    // 最大化
                    this.maximum();
                }
            }
        });
        // 鼠标拖动事件
        this.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // 更新位置
                if (this.checkNotInvalid()) {
                    this.doUpdateLocation();
                }
            }
        });
        // 鼠标释放事件
        this.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // 清除位置
                if (this.checkNotInvalid()) {
                    this.doClearLocation();
                }
            }
        });
    }

    /**
     * 记录位置
     */
    protected void doRecordLocation() {
        // 记录原始位置
        double[] originalPosition = MouseUtil.getMousePosition();
        this.setOriginalX(originalPosition[0]);
        this.setOriginalY(originalPosition[1]);
    }

    /**
     * 清除位置
     */
    protected void doClearLocation() {
        if (this.originalX != null) {
            this.originalX.set(null);
        }
        if (this.originalY != null) {
            this.originalY.set(null);
        }
    }

    /**
     * 更新位置
     */
    private void doUpdateLocation() {
        try {
            Double originalX = this.getOriginalX();
            Double originalY = this.getOriginalY();
            if (originalX == null || originalY == null) {
                // JulLog.warn("originalX or originalY is null!");
                return;
            }
            Window window = this.window();
            double[] position = MouseUtil.getMousePosition();
            double mouseX = position[0];
            double mouseY = position[1];
            double nodeX = window.getX();
            double nodeY = window.getY();

            // 更新x位置
            // 计算x差值，不等于0时则更新组件x位置，并更新x值
            double x1 = mouseX - this.getOriginalX();
            if (x1 != 0) {
                window.setX(nodeX + x1);
                this.setOriginalX(mouseX);
            }
            // 更新y位置
            // 计算y差值，不等于0时则更新组件x位置，并更新y值
            double y1 = mouseY - originalY;
            if (y1 != 0) {
                window.setY(nodeY + y1);
                this.setOriginalY(mouseY);
            }
            // JulLog.debug("doUpdateLocation mouseX:{} mouseY:{} nodeX:{} nodeY:{}", mouseX, mouseY, nodeX, nodeY);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 检查非异常
     *
     * @return 结果
     */
    protected boolean checkNotInvalid() {
        Stage stage = this.stage();
        // 最大化、最小化、全屏情况下不执行操作
        return stage != null && !stage.isMaximized() && !stage.isIconified() && !stage.isFullScreen();
    }

    /**
     * 关闭窗口
     */
    public void close() {
        Window window = this.window();
        if (window != null) {
            window.hide();
        } else {
            JulLog.warn("window is null!");
        }
    }

    /**
     * 最大化窗口
     */
    public void maximum() {
        Stage stage = this.stage();
        if (stage != null && stage.isResizable()) {
            this.maximum(!stage.isMaximized());
        } else {
            JulLog.warn("stage is null!");
        }
    }

    /**
     * 最大化窗口
     *
     * @param maximum 是否最大化
     */
    public void maximum(boolean maximum) {
        Stage stage = this.stage();
        if (stage != null) {
            stage.setMaximized(maximum);
            this.doMaximum(maximum);
        } else {
            JulLog.warn("stage is null!");
        }
    }

    /**
     * 执行最大化窗口业务
     *
     * @param maximum 是否最大化
     */
    public void doMaximum(boolean maximum) {
        Stage stage = this.stage();
        if (stage != null) {
            // 处理按钮
            TitleBarMaximumSVGPane pane = (TitleBarMaximumSVGPane) this.lookup("#maximum");
            if (pane != null) {
                pane.setMaximize(!maximum);
            }
            // 最大化设置高度不超过显示边界
            if (maximum) {
                // 是否更新
                boolean update = false;
                double xPos = stage.getX() + stage.getWidth();
                double yPos = stage.getY();
                // 获取所有屏幕的列表
                ObservableList<Screen> screens = Screen.getScreens();
                // 遍历屏幕列表，检查舞台是否在当前屏幕的边界内
                for (Screen screen : screens) {
                    Rectangle2D screenBounds = screen.getVisualBounds();
                    if (screenBounds.contains(xPos, yPos)) {
                        stage.setHeight(screenBounds.getHeight());
                        update = true;
                        break;
                    }
                }
                // 兜底设置
                if (!update) {
                    Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                    stage.setHeight(screenBounds.getHeight());
                }
            }
        } else {
            JulLog.warn("stage is null!");
        }
    }

    public void minimum() {
        Stage stage = this.stage();
        if (stage != null) {
            this.minimum(!stage.isIconified());
        } else {
            JulLog.warn("stage is null!");
        }
    }

    public void minimum(boolean minimum) {
        Stage stage = this.stage();
        if (stage != null) {
            stage.setIconified(minimum);
        } else {
            JulLog.warn("stage is null!");
        }
    }

    public void fullScreen() {
        Stage stage = this.stage();
        if (stage != null) {
            this.fullScreen(!stage.isFullScreen());
        } else {
            JulLog.warn("stage is null!");
        }
    }

    public void doFullScreen(boolean fullScreen) {
        Stage stage = this.stage();
        if (stage != null) {
            // 处理按钮
            TitleBarFullScreenSVGPane pane = (TitleBarFullScreenSVGPane) this.lookup("#fullScreen");
            if (pane != null) {
                pane.setFullScreen(!fullScreen);
            }
        } else {
            JulLog.warn("stage is null!");
        }
    }

    public void fullScreen(boolean fullScreen) {
        Stage stage = this.stage();
        if (stage != null) {
            stage.setFullScreen(fullScreen);
            this.doFullScreen(fullScreen);
        } else {
            JulLog.warn("stage is null!");
        }
    }

    public void alwaysOnTop() {
        Stage stage = this.stage();
        if (stage != null) {
            this.alwaysOnTop(!stage.isAlwaysOnTop());
        } else {
            JulLog.warn("stage is null!");
        }
    }

    public void doAlwaysOnTop(boolean alwaysOnTop) {
        Stage stage = this.stage();
        if (stage != null) {
            // 处理按钮
            TitleBarTopSVGGlyph glyph = (TitleBarTopSVGGlyph) this.lookup("#alwaysOnTop");
            if (glyph != null) {
                glyph.setActive(alwaysOnTop);
            }
        } else {
            JulLog.warn("stage is null!");
        }
    }

    public void alwaysOnTop(boolean alwaysOnTop) {
        Stage stage = this.stage();
        if (stage != null) {
            stage.setAlwaysOnTop(alwaysOnTop);
            this.doAlwaysOnTop(alwaysOnTop);
        } else {
            JulLog.warn("stage is null!");
        }
    }

    /**
     * 标题栏配置
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class TitleBarConfig {

        /**
         * 图标
         */
        private String icon;

        /**
         * 关闭
         */
        private boolean close = true;

        /**
         * 最大化
         */
        private boolean maximum = true;

        /**
         * 最小化
         */
        private boolean minimum = true;

        /**
         * 全屏
         */
        private boolean fullScreen;

        /**
         * 置顶
         */
        private boolean alwaysOnTop;
    }
}
