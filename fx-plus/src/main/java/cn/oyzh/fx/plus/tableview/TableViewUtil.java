package cn.oyzh.fx.plus.tableview;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.plus.controls.table.FXTableCell;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 表格工具类
 *
 * @author oyzh
 * @since 2023/8/11
 */

public class TableViewUtil {

    public <S, T> FXTableCell<S, T> newCell(double lineHeight) {
        FXTableCell<S, T> cell = new FXTableCell<>();
        cell.setLineHeight(lineHeight);
        return cell;
    }

    public <S, T> FXTableCell<S, T> newCell(double lineHeight, Pos pos) {
        FXTableCell<S, T> cell = new FXTableCell<>();
        cell.setAlignment(pos);
        cell.setLineHeight(lineHeight);
        return cell;
    }

    /**
     * 上移行
     *
     * @param tableView 组件
     */
    public static void moveUp(TableView<?> tableView) {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index == -1) {
            return;
        }
        ObservableList<?> list = tableView.getItems();
        Object object = CollectionUtil.get(list, index - 1);
        if (object != null) {
            Collections.swap(list, index, index - 1);
            tableView.getSelectionModel().select(index - 1);
        }
    }

    /**
     * 下移行
     *
     * @param tableView 组件
     */
    public static void moveDown(TableView<?> tableView) {
        if (tableView == null) {
            return;
        }
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index == -1) {
            return;
        }
        ObservableList<?> list = tableView.getItems();
        Object object = CollectionUtil.get(list, index + 1);
        if (object != null) {
            Collections.swap(list, index, index + 1);
            tableView.getSelectionModel().select(index + 1);
        }
    }

    /**
     * 双击时，复制列数据到粘贴板
     *
     * @param tableView 表格组件
     * @return EventHandler<MouseEvent>
     */
    public static EventHandler<MouseEvent> copyCellDataOnDoubleClicked(TableView<?> tableView) {
        EventHandler<MouseEvent> handler = event -> {
            if (MouseUtil.isDubboClick(event) && MouseUtil.isPrimaryButton(event)) {
                Object object = getSelectCellData(tableView);
                if (object instanceof String || object instanceof Number) {
                    ClipboardUtil.setStringAndTip(object.toString());
                }
            }
        };
        // 双击时，复制数据到粘贴板
        tableView.addEventFilter(MouseEvent.MOUSE_CLICKED, handler);
        return handler;
    }

    /**
     * 获取当前选中列的数据
     *
     * @param tableView 表格组件
     * @return 列数据
     */
    public static Object getSelectCellData(TableView<?> tableView) {
        ObservableList<TablePosition> positions = tableView.getSelectionModel().getSelectedCells();
        if (CollectionUtil.isNotEmpty(positions)) {
            TablePosition<?, ?> position = positions.getFirst();
            TableColumn<?, ?> column = position.getTableColumn();
            if (column != null) {
                return column.getCellData(position.getRow());
            }
        }
        return null;
    }

    public static TableRow<?> findTableRow(Node node) {
        if (node != null) {
            if (node instanceof TableRow<?>) {
                return (TableRow<?>) node;
            }
            while (true) {
                Parent parent = node.getParent();
                if (parent == null) {
                    return null;
                }
                if (parent instanceof TableRow<?> tableRow) {
                    return tableRow;
                }
                node = parent;
            }
        }
        return null;
    }

    /**
     * 鼠标点击时，选中表单行
     *
     * @param node 组件
     */
    public static void selectRowOnMouseClicked(Node node) {
        if (node != null) {
            node.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                TableRow<?> tableRow = findTableRow(node);
                if (tableRow != null && tableRow.getTableView() != null) {
                    tableRow.getTableView().getSelectionModel().select(tableRow.getIndex());
                }
            });
        }
    }

    /**
     * ctrl+s事件
     *
     * @param node 组件
     */
    public static void rowOnCtrlS(Node node) {
        if (node != null) {
            node.setOnKeyPressed(event -> {
                if (KeyboardUtil.isCtrlS(event)) {
                    TableRow<?> tableRow = findTableRow(node);
                    if (tableRow != null && tableRow.getTableView() instanceof FXTableView<?> tableView) {
                        tableView.onCtrl_S();
                    }
                }
            });
        }
    }

    public static double getRowHeight(TableView<?> tableView) {
        Set<Node> rows = tableView.lookupAll(".table-row-cell");
        for (Node row : rows) {
            if (row instanceof TableRow<?> tableRow) {
                double rowHeight = tableRow.getHeight();
                if (!Double.isNaN(rowHeight) && rowHeight > 0) {
                    return rowHeight;
                }
            }
        }
        return -1;
    }

    /**
     * 获取行列表
     *
     * @param tableView 表格
     * @return 行列表
     */
    public static List<TableRow<?>> getRows(TableView<?> tableView) {
        Set<Node> rows = tableView.lookupAll(".table-row-cell");
        List<TableRow<?>> rowList = new ArrayList<>();
        for (Node row : rows) {
            if (row instanceof TableRow<?> tableRow) {
                rowList.add(tableRow);
            }
        }
        return rowList;
    }

    public static double getHeaderRowHeight(TableView<?> tableView) {
        // 获取 TableHeaderRow
        TableHeaderRow headerRow = (TableHeaderRow) tableView.lookup("TableHeaderRow");
        if (headerRow != null) {
            return headerRow.getHeight();
        }
        return 0;
    }

    public static double getRowSpacing(TableView<?> tableView) {
        Set<Node> rows = tableView.lookupAll(".table-row-cell");
        // 获取前两行的 TableRow
        Optional<Node> firstRowNode = rows.stream().findFirst();
        Optional<Node> secondRowNode = rows.stream().skip(1).findFirst();
        if (firstRowNode.isPresent() && secondRowNode.isPresent()) {
            TableRow<?> firstRow = (TableRow<?>) firstRowNode.get();
            TableRow<?> secondRow = (TableRow<?>) secondRowNode.get();
            // 计算行间距
            double rowSpacing = secondRow.getLayoutY() - (firstRow.getLayoutY() + firstRow.getHeight());
            if (rowSpacing > 0) {
                return rowSpacing;
            }
        }
        return 0;
    }

}
