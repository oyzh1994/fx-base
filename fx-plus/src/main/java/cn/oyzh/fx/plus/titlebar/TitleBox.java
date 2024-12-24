package cn.oyzh.fx.plus.titlebar;

import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.MouseUtil;
import cn.oyzh.fx.plus.util.NodeUtil;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 标题组件
 *
 * @author oyzh
 * @since 2024/12/14
 */
public class TitleBox extends FXVBox {

    /**
     * 触发阈值
     */
    public static byte Threshold = 1;

    public TitleBox() {
        this.init();
    }

    public TitleBox(TitleBar titleBar, Node content) {
        this.initChild(titleBar, content);
        this.init();
    }

    /**
     * 初始化子节点
     *
     * @param titleBar 标题组件
     * @param content  内容组件
     */
    public void initChild(TitleBar titleBar, Node content) {
        this.setChild(0, titleBar);
        this.setChild(1, content);
    }

    /**
     * 设置标题组件
     *
     * @param titleBar 标题组件
     */
    public void setTitleBar(TitleBar titleBar) {
        this.setChild(0, titleBar);
    }

    /**
     * 获取标题组件
     *
     * @return 标题组件
     */
    public TitleBar getTitleBar() {
        return (TitleBar) this.getChild(0);
    }

    /**
     * 设置内容组件
     *
     * @param content 内容组件
     */
    public void setContent(Node content) {
        this.setChild(1, content);
    }

    /**
     * 获取内容组件
     *
     * @return 内容组件
     */
    public Node getContent() {
        return this.getChild(1);
    }

    /**
     * x轴变化标志位
     */
    private boolean xChange;

    /**
     * y轴变化标志位
     */
    private boolean yChange;

    /**
     * 宽度拉伸标志位
     */
    private boolean widthResize;

    /**
     * 高度拉伸标志位
     */
    private boolean heightResize;

    /**
     * 原始x
     */
    private AtomicReference<Double> originalX;

    /**
     * 原始y
     */
    private AtomicReference<Double> originalY;

    /**
     * 原始宽
     */
    private AtomicReference<Double> originalW;

    /**
     * 原始高
     */
    private AtomicReference<Double> originalH;

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

    protected void setOriginalW(double w) {
        if (this.originalW == null) {
            this.originalW = new AtomicReference<>();
        }
        this.originalW.set(w);
    }

    protected Double getOriginalW() {
        return this.originalW == null ? null : this.originalW.get();
    }

    protected void setOriginalH(double h) {
        if (this.originalH == null) {
            this.originalH = new AtomicReference<>();
        }
        this.originalH.set(h);
    }

    protected Double getOriginalH() {
        return this.originalH == null ? null : this.originalH.get();
    }

    /**
     * 初始化
     */
    protected void init() {
        this.setPadding(Insets.EMPTY);
        this.initBorder();
        this.initEvents();
    }

    /**
     * 初始化边框
     */
    protected void initBorder() {
        Color color = ThemeManager.currentAccentColor();
        CornerRadii radii = new CornerRadii(3);
        BorderStroke stroke = new BorderStroke(color, BorderStrokeStyle.SOLID, radii, new BorderWidths(1));
        Border border = new Border(stroke);
        this.setBorder(border);
    }

    /**
     * 初始化事件
     */
    protected void initEvents() {
        // 鼠标移动事件
        this.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            // 检查状态
            if (this.checkNotInvalid()) {
                this.doUpdateCursor(event.getX(), event.getY());
            }
        });
        // 鼠标按下事件
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                // 检查状态
                if (this.checkNotInvalid()) {
                    this.doRecordLocationAndSize();
                }
            }
        });
        // 鼠标拖动事件
        this.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            // 位置追踪
            if (event.getButton() == MouseButton.PRIMARY) {
                // 检查状态
                if (this.checkNotInvalid()) {
                    this.doUpdateLocationAndSize();
                }
            }
        });
        // 鼠标释放事件
        this.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // 检查状态
                if (this.checkNotInvalid()) {
                    this.doClearLocationAndSize();
                }
            }
        });
    }

    /**
     * 更新光标
     *
     * @param x 鼠标x位置
     * @param y 鼠标y位置
     */
    protected void doUpdateCursor(double x, double y) {
        double w = this.realWidth();
        double h = this.realHeight();
        // // 左上
        // if (x <= Threshold && y <= Threshold) {
        //     this.setCursor(Cursor.NW_RESIZE);
        //     this.widthResize = this.heightResize = this.xChange = this.yChange = true;
        //     return;
        // }
        // // 左下
        // if (x <= Threshold && Math.abs(h - y) <= Threshold) {
        //     this.setCursor(Cursor.SW_RESIZE);
        //     this.widthResize = this.heightResize = this.xChange = this.yChange = true;
        //     return;
        // // }
        // // 右上
        // if (Math.abs(w - x) <= Threshold && y <= Threshold) {
        //     this.setCursor(Cursor.SW_RESIZE);
        //     this.widthResize = this.heightResize = this.xChange = this.yChange = true;
        //     return;
        // }
        // 右下
        if (Math.abs(w - x) <= Threshold && Math.abs(h - y) <= Threshold) {
            this.setCursor(Cursor.NW_RESIZE);
            this.widthResize = this.heightResize = true;
            this.xChange = this.yChange = false;
            return;
        }
        // 右边
        if (Math.abs(w - x) <= Threshold) {
            this.setCursor(Cursor.W_RESIZE);
            this.widthResize = true;
            this.heightResize = this.xChange = this.yChange = false;
            return;
        }
        // 左边
        if (x <= Threshold) {
            this.setCursor(Cursor.W_RESIZE);
            this.widthResize = this.xChange = true;
            this.heightResize = this.yChange = false;
            return;
        }
        // 上边
        if (y <= Threshold) {
            this.setCursor(Cursor.S_RESIZE);
            this.heightResize = this.yChange = true;
            this.widthResize = this.xChange = false;
            return;
        }
        // 下边，要多一点判断边距
        if (Math.abs(h - y) <= (Threshold + 5)) {
            this.setCursor(Cursor.S_RESIZE);
            this.heightResize = true;
            this.widthResize = this.xChange = this.yChange = false;
            return;
        }
        // 正常
        this.widthResize = this.heightResize = this.xChange = this.yChange = false;
        // 默认光标
        this.setCursor(Cursor.DEFAULT);
    }

    /**
     * 记录位置和大小
     */
    protected void doRecordLocationAndSize() {
        double[] position = MouseUtil.getMousePosition();
        // 记录原始位置
        this.setOriginalX(position[0]);
        this.setOriginalY(position[1]);
        // 记录原始大小
        Window window = this.window();
        this.setOriginalW(window.getWidth());
        this.setOriginalH(window.getHeight());
    }

    /**
     * 清除位置和大小
     */
    protected void doClearLocationAndSize() {
        if (this.originalH != null) {
            this.originalH.set(null);
        }
        if (this.originalW != null) {
            this.originalW.set(null);
        }
        if (this.originalX != null) {
            this.originalX.set(null);
        }
        if (this.originalY != null) {
            this.originalY.set(null);
        }
    }

    /**
     * 更新位置和大小
     */
    protected void doUpdateLocationAndSize() {
        Window window = this.window();
        // 窗口位置
        Double originalX = this.getOriginalX();
        Double originalY = this.getOriginalY();
        Double originalW = this.getOriginalW();
        Double originalH = this.getOriginalH();
        double[] position = MouseUtil.getMousePosition();
        // // 鼠标位置
        double mouseX = position[0];
        double mouseY = position[1];
        // 更新宽度
        if (this.widthResize && originalX != null && originalW != null) {
            double x1 = mouseX - originalX;
            if (x1 != 0) {
                if (this.xChange) {
                    window.setWidth(originalW - x1);
                } else {
                    window.setWidth(originalW + x1);
                }
            }
        }
        // 更新高度
        if (this.heightResize && originalY != null && originalH != null) {
            double y1 = mouseY - originalY;
            if (y1 != 0) {
                if (this.yChange) {
                    window.setHeight(originalH - y1);
                } else {
                    window.setHeight(originalH + y1);
                }
            }
        }
        // 更新x坐标
        if (this.xChange && originalX != null) {
            double x1 = mouseX - originalX;
            if (x1 != 0) {
                window.setX(originalX + x1);
            }
        }
        // 更新y坐标
        if (this.yChange && originalY != null) {
            double y1 = mouseY - originalY;
            if (y1 != 0) {
                window.setY(originalY + y1);
            }
        }
    }

    /**
     * 检查非异常
     *
     * @return 结果
     */
    protected boolean checkNotInvalid() {
        Stage stage = this.stage();
        // 不可拉伸、最大化、最小化、全屏情况下不执行操作
        return stage != null && stage.isResizable() && !stage.isMaximized() && !stage.isIconified() && !stage.isFullScreen();
    }

    @Override
    public void resize(double width, double height) {
        super.resize(width, height);
        // 更新内容的宽高
        Node node = this.getContent();
        if (node != null) {
            NodeUtil.setSize(node, width - 4, height - 34);
        }
    }

    /**
     * 更新内容
     */
    public void updateContent() {
        Node node = this.getContent();
        if (node != null) {
            double width = this.realWidth();
            double height = this.getHeight();
            // 先减再加，因为全屏和最大化这个宽高已经最大了
            this.resize(width - 1, height - 1);
            this.resize(width + 1, height + 1);
        }
    }
}
