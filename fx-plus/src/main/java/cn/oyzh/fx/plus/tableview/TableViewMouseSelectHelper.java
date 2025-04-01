package cn.oyzh.fx.plus.tableview;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
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
                this.onMouseSelection(rectangle);
            }
        });

        // 监听鼠标释放事件
        tableView.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY) {
                Rectangle rectangle = this.findRectangle();
                if (rectangle != null) {
                    this.onMouseSelection(rectangle);
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
        // 行高
        double rowHeight = TableViewUtil.getRowHeight(tableView);
        // 选区开始和结束
        Point2D rectanglePoint = rectangle.localToScene(0, 0);
        double selectionStart = rectanglePoint.getY() + rectangle.getLayoutBounds().getMinY();
        double selectionEnd = rectangle.getLayoutBounds().getMaxY();
        // 选中节点
        List<Integer> selected = new ArrayList<>();
        // 计算位置
        List<TableRow<?>> rows = TableViewUtil.getRows(tableView);
        for (TableRow<?> row : rows) {
            Point2D point = row.localToScene(0, 0);
            double rowStart = point.getY() + row.getLayoutBounds().getMinY();
            double rowEnd = row.getLayoutBounds().getMaxY();
            // 判断是否在选区内
            if (rowStart + rowHeight > selectionStart && rowEnd + 5 < selectionEnd) {
                selected.add(row.getIndex());
            }
        }
        // 清除选择
        tableView.getSelectionModel().clearSelection();
        // 进行排序
        selected.sort(Integer::compareTo);
        // 选择多个
        if (selected.size() > 1) {
            int startIndex = selected.getFirst();
            int endIndex = selected.getLast();
            tableView.getSelectionModel().selectRange(startIndex, endIndex + 1);
        } else if (selected.size() == 1) {// 选择单个
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
