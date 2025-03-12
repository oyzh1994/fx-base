package cn.oyzh.fx.plus.tableview;

import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class TableViewMouseSelectHelper {

    private final Reference<TableView<?>> reference;

    public TableViewMouseSelectHelper(TableView<?> tableView) {
        this.reference = new WeakReference<>(tableView);
        this.initEvent();
    }

    /**
     * 获取tableview
     *
     * @return tableview
     */
    protected TableView<?> getTableView() {
        return this.reference.get();
    }

    /**
     * 获取根节点
     *
     * @return 根节点
     */
    protected Pane getRoot() {
        TableView<?> tableView = this.getTableView();
        if (tableView != null && tableView.getScene() != null && tableView.getScene().getRoot() instanceof Pane pane) {
            return pane;
        }
        return null;
    }

    /**
     * 初始化矩形
     *
     * @return 矩形
     */
    protected Rectangle initRectangle() {
        Pane pane = this.getRoot();
        if (pane != null) {
            Rectangle selectionRect = (Rectangle) pane.lookup("#table_view_selection_rect");
            if (selectionRect == null) {
                selectionRect = new Rectangle(0, 0, 0, 0);
                selectionRect.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.3));
                selectionRect.managedProperty().bind(selectionRect.visibleProperty());
                selectionRect.setId("table_view_selection_rect");
                pane.getChildren().add(selectionRect);
                selectionRect.setVisible(true);
            }
            return selectionRect;
        }
        return null;
    }

    /**
     * 清除矩形
     */
    protected void clearRectangle() {
        Pane pane = this.getRoot();
        if (pane != null) {
            Node selectionRect = pane.lookup("#table_view_selection_rect");
            if (selectionRect != null) {
                pane.getChildren().remove(selectionRect);
            }
        }
    }

    /**
     * 寻找矩形
     *
     * @return 矩形
     */
    protected Rectangle findRectangle() {
        Pane pane = this.getRoot();
        if (pane != null) {
            return (Rectangle) pane.lookup("#table_view_selection_rect");
        }
        return null;
    }

    /**
     * 初始化事件
     */
    protected void initEvent() {
        TableView<?> tableView = this.getTableView();
        if (tableView == null) {
            return;
        }

        // 起始位置
        AtomicReference<Double> startX = new AtomicReference<>(0D);
        AtomicReference<Double> startY = new AtomicReference<>(0D);

        // 监听鼠标按下事件
        tableView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY) {
                startX.set(event.getSceneX());
                startY.set(event.getSceneY());
            }
        });

        // 监听鼠标拖动事件
        tableView.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY) {
                if (startX.get() == 0 && startY.get() == 0) {
                    return;
                }
                double endX = event.getSceneX();
                double endY = event.getSceneY();
                Rectangle rectangle = this.findRectangle();
                if (rectangle == null) {
                    rectangle = this.initRectangle();
                }
                rectangle.setX(Math.min(startX.get(), endX));
                rectangle.setY(Math.min(startY.get(), endY));
                rectangle.setWidth(Math.abs(endX - startX.get()));
                rectangle.setHeight(Math.abs(endY - startY.get()));
//                selectionRect.setVisible(true);
                this.onMouseSelection(rectangle);
            }
        });

        // 监听鼠标释放事件
        tableView.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY) {
                Rectangle rectangle = this.findRectangle();
                if (rectangle != null) {
                    this.onMouseSelection(rectangle);
//                    selectionRect.setVisible(false);
                    this.clearRectangle();
                }
            }
        });
    }

    /**
     * 鼠标选中事件
     *
     * @param rectangle 矩形
     */
    protected void onMouseSelection(Rectangle rectangle) {
        TableView<?> tableView = this.getTableView();
        if (tableView == null || rectangle == null) {
            return;
        }
        double rowHeight = TableViewUtil.getRowHeight(tableView);
        double headerRowHeight = TableViewUtil.getHeaderRowHeight(tableView);
        double rowSpacing = TableViewUtil.getRowSpacing(tableView);
        double startY = tableView.getLayoutBounds().getMinY();
        double endY = tableView.getLayoutBounds().getMaxY();
        double selectionStart = rectangle.getLayoutBounds().getMinY();
        double selectionEnd = rectangle.getLayoutBounds().getMaxY();
        List<Integer> selected = new ArrayList<>();
        double indexY = startY + headerRowHeight + rowSpacing;
        for (int i = 0; i < tableView.getItems().size(); i++) {
            if (indexY >= endY || indexY >= selectionEnd) {
                break;
            }
            indexY += rowHeight + rowSpacing;
            if (indexY >= selectionStart - rowHeight - rowSpacing) {
                selected.add(i);
            }
        }
        tableView.getSelectionModel().clearSelection();
        if (selected.size() > 1) {
            int startIndex = selected.getFirst();
            int endIndex = selected.getLast();
            tableView.getSelectionModel().selectRange(startIndex, endIndex + 1);
        } else if (selected.size() == 1) {
            int startIndex = selected.getFirst();
            tableView.getSelectionModel().select(startIndex);
        }
    }

    public static void install(TableView<?> tableView) {
        if (tableView != null) {
            new TableViewMouseSelectHelper(tableView);
        }
    }
}
