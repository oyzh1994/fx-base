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

    protected TableView<?> getTableView() {
        return this.reference.get();
    }

    protected Rectangle initRectangle() {
        TableView<?> tableView = this.getTableView();
        if (tableView != null && tableView.getScene().getRoot() instanceof Pane pane) {
            Rectangle selectionRect = (Rectangle) pane.lookup("#table_view_selection_rect");
            if (selectionRect == null) {
                selectionRect = new Rectangle(0, 0, 0, 0);
                selectionRect.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.3));
                selectionRect.managedProperty().bind(selectionRect.visibleProperty());
                selectionRect.setId("table_view_selection_rect");
                selectionRect.setVisible(false);
                pane.getChildren().add(selectionRect);
            }
            return selectionRect;
        }
        return null;
    }

    protected void clearRectangle() {
        TableView<?> tableView = this.getTableView();
        if (tableView != null && tableView.getScene().getRoot() instanceof Pane pane) {
            Node selectionRect = pane.lookup("#table_view_selection_rect");
            if (selectionRect != null) {
                pane.getChildren().remove(selectionRect);
            }
        }
    }

    protected Rectangle findRectangle() {
        TableView<?> tableView = this.getTableView();
        if (tableView != null && tableView.getScene().getRoot() instanceof Pane pane) {
            return (Rectangle) pane.lookup("#table_view_selection_rect");
        }
        return null;
    }

    protected void initEvent() {
        TableView<?> tableView = this.getTableView();
        // 起始位置
        AtomicReference<Double> startX = new AtomicReference<>(0D);
        AtomicReference<Double> startY = new AtomicReference<>(0D);
        // 监听鼠标按下事件
        tableView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
//            if (event.getButton() == MouseButton.PRIMARY || event.getButton() == MouseButton.SECONDARY) {
                startX.set(event.getSceneX());
                startY.set(event.getSceneY());
//                Rectangle selectionRect = this.findRectangle();
//                if (selectionRect == null) {
//                    selectionRect = this.initRectangle();
//                }
//                selectionRect.setX(startX.get());
//                selectionRect.setY(startY.get());
//                selectionRect.setWidth(0);
//                selectionRect.setHeight(0);
//                selectionRect.setVisible(true);
            }
        });

        // 监听鼠标拖动事件
        tableView.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
//            if (event.getButton() == MouseButton.PRIMARY || event.getButton() == MouseButton.SECONDARY) {
                double endX = event.getSceneX();
                double endY = event.getSceneY();
                Rectangle selectionRect = this.findRectangle();
                if (selectionRect == null) {
                    selectionRect = this.initRectangle();
                }
                selectionRect.setX(Math.min(startX.get(), endX));
                selectionRect.setY(Math.min(startY.get(), endY));
                selectionRect.setWidth(Math.abs(endX - startX.get()));
                selectionRect.setHeight(Math.abs(endY - startY.get()));
                selectionRect.setVisible(true);
                this.onMouseSelection(event);
            }
        });

        // 监听鼠标释放事件
        tableView.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
//            if (event.getButton() == MouseButton.PRIMARY || event.getButton() == MouseButton.SECONDARY) {
                Rectangle selectionRect = this.findRectangle();
                if (selectionRect != null) {
                    this.onMouseSelection(event);
                    selectionRect.setVisible(false);
                    this.clearRectangle();
                }
            }
        });
    }

    protected void onMouseSelection(MouseEvent event) {
        TableView<?> tableView = this.getTableView();
        if (tableView == null) {
            return;
        }
        Rectangle rectangle = this.findRectangle();
        double rowHeight = TableViewUtil.getRowHeight(tableView);
        double headerRowHeight = TableViewUtil.getHeaderRowHeight(tableView);
        double rowSpacing = TableViewUtil.getRowSpacing(tableView);
        double startY = tableView.getLayoutBounds().getMinY();
        double endY = tableView.getLayoutBounds().getMaxY();
        double selectionStart = rectangle.getLayoutBounds().getMinY();
        double selectionEnd = rectangle.getLayoutBounds().getMaxY();
        List<Integer> selected = new ArrayList<>();
        double indexY = startY + headerRowHeight + rowSpacing;
//        System.out.println("indexY:" + indexY);
//        System.out.println("startY:" + startY);
//        System.out.println("endY:" + endY);
//        System.out.println("selectionStart:" + selectionStart);
//        System.out.println("selectionEnd:" + selectionEnd);
//        System.out.println("------->");
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
//        if (event.getEventType() == MouseEvent.MOUSE_RELEASED && event.getButton() == MouseButton.SECONDARY) {
//            tableView.fireEvent(new MouseEvent(
//                    MouseEvent.MOUSE_CLICKED, event.getX(), event.getY(), event.getX(), event.getY(), MouseButton.SECONDARY,
//                    1, true, false, false, false, false,
//                    false, true, true, true, true, null
//            ));
//        }
    }
}
