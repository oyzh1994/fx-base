package cn.oyzh.fx.plus.util;

import cn.oyzh.fx.common.util.CollectionUtil;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.controls.table.LineHeightTableCell;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import lombok.experimental.UtilityClass;

import java.util.Collections;

/**
 * 表格工具类
 *
 * @author oyzh
 * @since 2023/8/11
 */
@UtilityClass
public class TableViewUtil {

    public <S, T> LineHeightTableCell<S, T> lineHeightCell(double lineHeight) {
        LineHeightTableCell<S, T> cell = new LineHeightTableCell<>();
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
     */
    public static void copyCellOnDoubleClicked(TableView<?> tableView) {
        // 双击时，复制数据到粘贴板
        tableView.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (cn.oyzh.fx.plus.util.MouseUtil.isDubboClick(event) && MouseUtil.isPrimaryButton(event)) {
                ClipboardUtil.setStringAndTip((String) getSelectCellData(tableView));
            }
        });
    }

    /**
     * 获取当前选中列的数据
     *
     * @param tableView 表格组件
     * @return 列数据
     */
    public Object getSelectCellData(TableView<?> tableView) {
        ObservableList<TablePosition> positions = tableView.getSelectionModel().getSelectedCells();
        if (CollectionUtil.isNotEmpty(positions)) {
            TablePosition<?, ?> position = positions.get(0);
            TableColumn<?, ?> column = position.getTableColumn();
            return column.getCellData(position.getRow());
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
}
