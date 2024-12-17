package cn.oyzh.fx.plus.titlebar;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.image.FXImageView;
import cn.oyzh.fx.plus.controls.pane.FlexPane;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.util.IconUtil;
import cn.oyzh.fx.plus.util.MouseUtil;
import cn.oyzh.fx.plus.util.NodeUtil;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
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

public class TitleBar extends FlexPane {

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

    protected void init(TitleBarConfig config) {
        this.realHeight(30);
        this.setMaxHeight(30);
        this.setFlexWidth("100%");
        this.setBorder(null);
        this.setPadding(new Insets(0));
        this.initNodes(config);
        this.initEvents();
    }

    protected void initNodes(TitleBarConfig config) {
        List<Node> nodes = config.getActions();
        if (nodes == null) {
            nodes = new ArrayList<>();
        }
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
        if (config.isShowMinimum()) {
            TitleBarMinimumSVGGlyph glyph = new TitleBarMinimumSVGGlyph("18");
            glyph.setOnMousePrimaryClicked(e -> this.minimize());
            glyph.setId("minimize");
            nodes.add(glyph);
        }
        if (config.isShowMaximum()) {
            TitleBarMaximumSVGPane pane = new TitleBarMaximumSVGPane("18");
            pane.setId("maximize");
            pane.setOnMousePrimaryClicked(e -> {
                this.maximize();
                pane.setMaximize(!this.stage().isMaximized());
            });
            nodes.add(pane);
        }
        if (config.isShowClose()) {
            TitleBarCloseSVGGlyph glyph = new TitleBarCloseSVGGlyph("18");
            glyph.setId("close");
            glyph.setOnMousePrimaryClicked(e -> this.close());
            nodes.add(glyph);
        }
        this.addChild(nodes);
    }

    /**
     * 初始化标题
     */
    public void initTitle() {
        Stage stage = this.stage();
        if (stage != null) {
            if (stage.getTitle() != null) {
                String title = stage.getTitle();
                FXText text = (FXText) this.lookup("#title");
                // 创建
                if (text == null) {
                    text = new FXText(title);
                    text.setFontSize(12);
                    text.setId("title");
                    this.addChild(1, text);
                } else if (StringUtil.equals(text.getText(), title)) {// 更新
                    text.setText(title);
                }
            } else {
                FXText text = (FXText) this.lookup("#title");
                if (text != null) {// 移除
                    this.removeChild(text);
                }
            }
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
        Node maximize = null;
        Node minimize = null;
        for (Node child : this.getChildren()) {
            if ("icon".equals(child.getId())) {
                double nWidth = NodeUtil.getWidth(child);
                double nHeight = NodeUtil.getHeight(child);
                child.setLayoutY((height - nHeight) / 2);
                child.setLayoutX(layoutX);
                layoutX += nWidth + 5;
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
            if ("minimize".equals(child.getId())) {
                minimize = child;
                double nHeight = NodeUtil.getHeight(child);
                child.setLayoutY((height - nHeight) / 2);
                continue;
            }
            if ("maximize".equals(child.getId())) {
                maximize = child;
                double nHeight = NodeUtil.getHeight(child);
                child.setLayoutY((height - nHeight) / 2);
                continue;
            }
            if ("close".equals(child.getId())) {
                close = child;
                double nHeight = NodeUtil.getHeight(child);
                child.setLayoutY((height - nHeight) / 2);
                continue;
            }
        }

        if (close != null) {
            double nWidth = NodeUtil.getWidth(close);
            close.setLayoutX(width - nWidth - 10);
            actionW = nWidth + 10;
        }
        if (maximize != null) {
            double nWidth = NodeUtil.getWidth(maximize);
            maximize.setLayoutX(width - actionW - nWidth - 10);
            actionW = actionW + nWidth + 10;
        }
        if (minimize != null) {
            double nWidth = NodeUtil.getWidth(minimize);
            minimize.setLayoutX(width - actionW - nWidth - 10);
            actionW = actionW + nWidth + 10;
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
                        // event.consume();
                    }
                } else if (event.getClickCount() == 2) {
                    // 最大化
                    this.maximize();
                    // event.consume();
                }
            }
        });
        // 鼠标拖动事件
        this.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // 更新位置
                if (this.checkNotInvalid()) {
                    this.doUpdateLocation();
                    // event.consume();
                }
            }
        });
        // 鼠标释放事件
        this.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // 清除位置
                if (this.checkNotInvalid()) {
                    this.doClearLocation();
                    // event.consume();
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

    protected void doClearLocation() {
        if (this.originalX != null) {
            this.originalX.set(null);
        }
        if (this.originalY != null) {
            this.originalY.set(null);
        }
    }

    private void doUpdateLocation() {
        try {
            if (this.originalX.get() == null || this.originalY.get() == null) {
                JulLog.warn("originalX or originalY is null!");
                return;
            }
            Window window = this.window();
            double[] position = MouseUtil.getMousePosition();
            double mouseX = position[0];
            double mouseY = position[1];
            double nodeX = window.getX();
            double nodeY = window.getY();
            Double originalX = this.getOriginalX();
            Double originalY = this.getOriginalY();
            // 更新x位置
            if (originalX != null) {
                // 计算x差值，不等于0时则更新组件x位置，并更新x值
                double x1 = mouseX - this.getOriginalX();
                if (x1 != 0) {
                    window.setX(nodeX + x1);
                    this.setOriginalX(mouseX);
                }
            }
            // 更新y位置
            if (originalY != null) {
                // 计算y差值，不等于0时则更新组件x位置，并更新y值
                double y1 = mouseY - originalY;
                if (y1 != 0) {
                    window.setY(nodeY + y1);
                    this.setOriginalY(mouseY);
                }
            }
            // JulLog.debug("doUpdateLocation mouseX:{} mouseY:{} nodeX:{} nodeY:{}", mouseX, mouseY, nodeX, nodeY);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected boolean checkNotInvalid() {
        Stage stage = this.stage();
        // 最大化、最小化、全屏情况下不执行操作
        return stage != null && !stage.isMaximized() && !stage.isIconified() && !stage.isFullScreen();
    }

    public void close() {
        Window window = this.window();
        if (window != null) {
            window.hide();
        } else {
            JulLog.warn("window is null!");
        }
    }

    public void maximize() {
        Stage stage = this.stage();
        if (stage != null) {
            this.maximum(!stage.isMaximized());
        } else {
            JulLog.warn("stage is null!");
        }
    }

    public void maximum(boolean maximum) {
        Stage stage = this.stage();
        if (stage != null) {
            stage.setMaximized(maximum);
            // 处理按钮
            TitleBarMaximumSVGPane maximizePane = (TitleBarMaximumSVGPane) this.lookup("#maximize");
            if (maximizePane != null) {
                maximizePane.setMaximize(!maximum);
            }
            // 最大化设置高度不超过显示边界
            if (maximum) {
                Rectangle2D rectangle2D = Screen.getPrimary().getVisualBounds();
                stage.setHeight(rectangle2D.getHeight());
            }
        } else {
            JulLog.warn("stage is null!");
        }
    }

    public void minimize() {
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
         * 当前是否最大化
         */
        private boolean maximized;

        /**
         * 操作
         */
        private List<Node> actions;

        /**
         * 显示close
         */
        private boolean showClose = true;

        /**
         * 显示最大化
         */
        private boolean showMaximum = true;

        /**
         * 显示最小化
         */
        private boolean showMinimum = true;
    }
}
