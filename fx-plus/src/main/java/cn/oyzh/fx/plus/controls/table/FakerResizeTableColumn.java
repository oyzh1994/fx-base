package cn.oyzh.fx.plus.controls.table;


import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.ControlUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

import java.util.function.Consumer;

/**
 * 模拟拉伸的TableColumn，解决部分场景下拉伸失效问题
 *
 * @author oyzh
 * @since 2026/05/30
 */
public class FakerResizeTableColumn<S, T> extends FXTableColumn<S, T> {

    public FakerResizeTableColumn() {
        super();
    }

    public FakerResizeTableColumn(String text) {
        super(text);
    }

    /**
     * 初始化图形
     *
     * @param text 文本
     * @return 结果
     */
    protected FXHBox initGraphic(String text) {
        FXLabel label = new FXLabel(text);
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        label.setFont(FontUtil.newFontByWeight(label.getFont(), FontWeight.BOLD));
        return this.initGraphic(label);
    }

    /**
     * 初始化图形
     *
     * @param content 节点
     * @return 结果
     */
    protected FXHBox initGraphic(Node content) {
        if (content instanceof Region region) {
            region.setPadding(Insets.EMPTY);
        }

        // 拖拽把手：一个透明的窄条
        Region handle = new Region();
        handle.setMinWidth(3);
        handle.setMaxWidth(3);
        handle.setPrefWidth(3);
        handle.setPadding(Insets.EMPTY);
        handle.setCursor(Cursor.H_RESIZE);
        handle.setBackground(ControlUtil.background(Color.TRANSPARENT));

        // 把手悬停时改变背景色以便视觉提示（可选）
        handle.setOnMouseEntered(e -> {
            handle.setBackground(ControlUtil.background(ThemeManager.currentForegroundColor()));
        });
        handle.setOnMouseExited(e -> {
            handle.setBackground(ControlUtil.background(Color.TRANSPARENT));
        });
        // 把手拖拽逻辑
        handle.setOnMousePressed(e -> {
            // 记录起始宽度，用于计算增量
            handle.setUserData(new double[]{e.getSceneX(), this.getWidth()});
            e.consume();
        });
        handle.setOnMouseDragged(e -> {
            double[] data = (double[]) handle.getUserData();
            if (data == null) {
                return;
            }
            double startSceneX = data[0];
            double startWidth = data[1];
            double delta = e.getSceneX() - startSceneX;
            double newWidth = startWidth + delta;
            // 应用宽度限制
            if (newWidth < this.getMinWidth()) {
                newWidth = this.getMinWidth();
            }
            if (newWidth > this.getMaxWidth()) {
                newWidth = this.getMaxWidth();
            }
            this.setPrefWidth(newWidth);
            e.consume();
        });
        handle.setOnMouseReleased(e -> {
            handle.setUserData(null);
            e.consume();
        });

        // 把标签和把手放入 HBox
        FXHBox hBox = new FXHBox(content, handle);

        // 标签占用剩余空间，把手始终在右侧
        HBox.setHgrow(content, Priority.ALWAYS);
        hBox.setAlignment(Pos.CENTER_LEFT);
        // 关键：禁用原生列宽调整，避免冲突
        this.setResizable(false);
        // 绑定宽度
        hBox.prefWidthProperty().bind(this.widthProperty());
        // 宽度函数
        Consumer<Double> widthFunc = w -> {
            if (w != null) {
                // 组件宽
                double w1 = NodeUtil.getWidth(handle);
                // 设置宽度
                NodeUtil.setWidth(content, w - w1);
                // 字符宽
                double w2 = FontUtil.textWidth("a", FontManager.currentFont());
                // 偏移量
                double translateX = w2 + w1 / 2;
                // 设置偏移量
                handle.setTranslateX(translateX);
            }
        };
        // 触发一次
        widthFunc.accept(this.getWidth());
        // 监听宽度改变
        this.widthProperty().addListener((observable, oldWidth, newWidth) -> {
            if (newWidth != null) {
                widthFunc.accept(newWidth.doubleValue());
            }
        });
        return hBox;
    }

    @Override
    public void initNode() {
        if (this.autoInitGraphic()) {
            this.textProperty().subscribe(text -> {
                if (text != null) {
                    FXHBox graphic = this.initGraphic(text);
                    this.setGraphic(graphic);
                    super.setText(null);
                }
            });
        }
        super.initNode();
    }

    protected boolean autoInitGraphic() {
        return true;
    }
}
